package social.godmode.script;

import jdk.dynalink.beans.StaticClass;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import social.godmode.Main;

import javax.script.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Getter
public class JavaScriptEngine {

    public static final ConcurrentHashMap<String, CompiledScript> compiledCache = new ConcurrentHashMap<>();

    private final Context context;
    private final Engine engine;

    public String invalidPrompt;
    public List<String> logs = new ArrayList<>();

    public JavaScriptEngine(String code, JDA jda, Guild guild, GuildChannel sentChannel, Member sentMember) {
        this.context = Context.newBuilder("js")
                .allowHostAccess(HostAccess.ALL)
                .build();

        this.engine = context.getEngine();

        put("client", new DiscordClientNashorn(jda, guild, sentChannel, sentMember, this));

        DiscordClientNashorn.IMember iMember = new DiscordClientNashorn.IMember(sentMember.getId(), sentMember.getNickname());
        DiscordClientNashorn.IChannel iChannel = new DiscordClientNashorn.IChannel(sentChannel.getId(), sentChannel.getName(), sentChannel.getType().name().toLowerCase());
        put("executedMember", iMember.toJSObject(this));
        put("executedChannel", iChannel.toJSObject(this));

        eval(code.substring(code.indexOf("```djs") + 6, code.lastIndexOf("```")));
    }

    public void eval(String evaluate) {
        if(context == null) throw new RuntimeException("JavaScriptEngine has been terminated!");

        context.eval("js", evaluate);
    }

    public void put(String key, Object value) {
        if(context == null) throw new RuntimeException("JavaScriptEngine has been terminated!");

        context.getBindings("js").putMember(key, value);
    }

    public void put(String key, Class<?> clazz) {
        put(key, StaticClass.forClass(clazz));
    }

    public void put(Class<?> clazz) {
        put(clazz.getSimpleName(), clazz);
    }

    public Object get(String name) {
        if(context.getBindings("js").getMember(name) == null) throw new RuntimeException(String.format("No such variable: %s", name));

        return context.getBindings("js").getMember(name);
    }

}
