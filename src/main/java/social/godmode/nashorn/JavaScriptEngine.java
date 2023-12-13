package social.godmode.nashorn;

import jdk.dynalink.beans.StaticClass;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.lang.reflect.Method;

@Getter
public class JavaScriptEngine {

    private final ScriptEngine engine;

    public JavaScriptEngine(String code, JDA jda, Guild guild, GuildChannel sentChannel, Member sentMember) {
        this.engine = new NashornScriptEngineFactory().getScriptEngine();
        this.engine.put("client", new DiscordClientNashorn(jda, guild, sentChannel, sentMember));
        // get middle of ```djs and ``` and eval that
        String eval = code.substring(code.indexOf("```djs") + 5, code.lastIndexOf("```"));
        eval(eval);
    }

    public void eval(String evaluate) {
        try {
            engine.eval(evaluate);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
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
