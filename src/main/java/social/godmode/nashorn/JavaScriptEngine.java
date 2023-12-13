package social.godmode.nashorn;

import jdk.dynalink.beans.StaticClass;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.openjdk.nashorn.api.scripting.JSObject;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

@Getter
public class JavaScriptEngine {

    private final ScriptEngine engine;

    public JavaScriptEngine(String code, JDA jda, Guild guild, GuildChannel sentChannel, Member sentMember) {
        this.engine = new NashornScriptEngineFactory().getScriptEngine();
        
        put("client", new DiscordClientNashorn(jda, guild, sentChannel, sentMember, this));
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
        engine.put(key, value);
    }

    public void put(String key, Class<?> clazz) {
        engine.put(key, StaticClass.forClass(clazz));
    }

    public void put(Class<?> clazz) {
        engine.put(clazz.getSimpleName(), StaticClass.forClass(clazz));
    }

    public Object get(String name) {
        if(engine.get(name) == null)
//            throw new RuntimeException("No such variable: " + name);
            throw new RuntimeException(String.format("No such variable: %s", name));
        return engine.get(name);
    }

    public static void test(String a) {
        System.out.println(a);
    }

}
