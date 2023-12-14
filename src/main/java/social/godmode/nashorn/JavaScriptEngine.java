package social.godmode.nashorn;

import jdk.dynalink.beans.StaticClass;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public class JavaScriptEngine {

    private ScriptEngine engine;
    public String invalidPrompt;
    public List<String> logs = new ArrayList<>();

    public JavaScriptEngine(String code, JDA jda, Guild guild, GuildChannel sentChannel, Member sentMember) {
        this.engine = new NashornScriptEngineFactory().getScriptEngine();
        
        put("client", new DiscordClientNashorn(jda, guild, sentChannel, sentMember, this));
        DiscordClientNashorn.IMember iMember = new DiscordClientNashorn.IMember(sentMember.getId(), sentMember.getNickname());
        DiscordClientNashorn.IChannel iChannel = new DiscordClientNashorn.IChannel(sentChannel.getId(), sentChannel.getName(), sentChannel.getType().name().toLowerCase());
        put("executedMember", iMember.toJSObject(this));
        put("executedChannel", iChannel.toJSObject(this));
        eval(code.substring(code.indexOf("```djs") + 6, code.lastIndexOf("```")));
    }

    public Object eval(String evaluate) {
        try {
            return engine.eval(evaluate);
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
