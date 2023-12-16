package social.godmode.nashorn;

import delight.nashornsandbox.NashornSandbox;
import delight.nashornsandbox.NashornSandboxes;
import delight.nashornsandbox.SandboxScriptContext;
import jdk.dynalink.beans.StaticClass;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import social.godmode.Main;
import social.godmode.util.ReflexUtil;

import javax.script.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Getter
public class JavaScriptEngine {

    private final ScriptEngine engine;
    private final NashornSandbox sandbox;

    public String invalidPrompt;
    public List<String> logs = new ArrayList<>();
    public static final ConcurrentHashMap<String, CompiledScript> compiledCache = new ConcurrentHashMap<>();
    public ScriptContext context;

    public JavaScriptEngine(String code, JDA jda, Guild guild, GuildChannel sentChannel, Member sentMember) {
        this.sandbox = NashornSandboxes.create();
        this.engine = (ScriptEngine) ReflexUtil.getField("scriptEngine", sandbox);

        this.sandbox.setMaxMemory(1024 * 1024 * 1024);
        this.sandbox.setMaxCPUTime(100000);
        this.sandbox.disallowAllClasses();
        this.sandbox.setMaxPreparedStatements(9999);
        this.sandbox.setExecutor(Executors.newSingleThreadExecutor());
        assert engine != null;
        context = engine.getContext();

        put("client", new DiscordClientNashorn(jda, guild, sentChannel, sentMember, this));

        DiscordClientNashorn.IMember iMember = new DiscordClientNashorn.IMember(sentMember.getId(), sentMember.getNickname());
        DiscordClientNashorn.IChannel iChannel = new DiscordClientNashorn.IChannel(sentChannel.getId(), sentChannel.getName(), sentChannel.getType().name().toLowerCase());
        put("executedMember", iMember.toJSObject(this));
        put("executedChannel", iChannel.toJSObject(this));

        // get code in between ```djs and ```
        String codeToEval = code.substring(code.indexOf("```djs") + 6, code.lastIndexOf("```"));
        CompiledScript compiled = compiledCache.get(codeToEval);
        if (compiled == null) {
            compiled = compile(codeToEval);
        }
        eval(compiled);
//        eval(codeToEval);
    }

    public Object eval(String evaluate) {
        try {
            sandbox.setExecutor(Executors.newSingleThreadExecutor());
            return sandbox.eval(evaluate);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void eval(CompiledScript evaluate) {
        try {
            sandbox.eval(evaluate, context);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    public void put(String key, Object value) {
        if(engine == null) throw new RuntimeException("JavaScriptEngine has been terminated!");

        engine.put(key, value);
    }

    public void put(String key, Class<?> clazz) {
        put(key, StaticClass.forClass(clazz));
    }

    public void put(Class<?> clazz) {
        put(clazz.getSimpleName(), clazz);
    }

    public Object get(String name) {
        if(engine.get(name) == null) throw new RuntimeException(String.format("No such variable: %s", name));

        return engine.get(name);
    }

    public CompiledScript compile(String code) {
        Main.getLogger().info("Compiling script: " + code);
        try {
            CompiledScript compiled = sandbox.compile(code);
            compiledCache.put(code, compiled);
            return compiled;
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void terminate() {
        Bindings bind = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        Set<String> allAttributes = bind.keySet();
        for (String attr : allAttributes) {
            bind.remove(attr);
        }
    }

}
