package social.godmode.nashorn;

import delight.nashornsandbox.NashornSandbox;
import delight.nashornsandbox.NashornSandboxes;
import jdk.dynalink.beans.StaticClass;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import social.godmode.util.ReflexUtil;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

@Getter
public class JavaScriptEngine {

    private final ScriptEngine engine;
    private final NashornSandbox sandbox;

    public String invalidPrompt;
    public List<String> logs = new ArrayList<>();

    public JavaScriptEngine(String code, JDA jda, Guild guild, GuildChannel sentChannel, Member sentMember) {
        this.sandbox = NashornSandboxes.create();
        this.engine = (ScriptEngine) ReflexUtil.getField("scriptEngine", sandbox);

        this.sandbox.setMaxMemory(1024 * 1024 * 1024);
        this.sandbox.setMaxCPUTime(10000);
        this.sandbox.disallowAllClasses();

        put("client", new DiscordClientNashorn(jda, guild, sentChannel, sentMember, this));

        DiscordClientNashorn.IMember iMember = new DiscordClientNashorn.IMember(sentMember.getId(), sentMember.getNickname());
        DiscordClientNashorn.IChannel iChannel = new DiscordClientNashorn.IChannel(sentChannel.getId(), sentChannel.getName(), sentChannel.getType().name().toLowerCase());
        put("executedMember", iMember.toJSObject(this));
        put("executedChannel", iChannel.toJSObject(this));

        eval(code.substring(code.indexOf("```djs") + 6, code.lastIndexOf("```")));
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

    public void terminate() {
        Bindings bind = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        Set<String> allAttributes = bind.keySet();
        for (String attr : allAttributes) {
            bind.remove(attr);
        }
    }

}
