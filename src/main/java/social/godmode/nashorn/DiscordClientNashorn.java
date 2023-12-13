package social.godmode.nashorn;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Getter
public class DiscordClientNashorn {

    private final JavaScriptEngine engine;

    private final JDA jda;
    private final Guild guild;

    public DiscordClientNashorn(JDA jda, Guild guild) {
        this.jda = jda;
        this.guild = guild;

        this.engine = new JavaScriptEngine();
        this.engine.put("guild", this.guild);
        this.engine.put("engine", this);
    }

    public void createChannel(String name, String type) {
        switch (type) {
            case "text" -> this.guild.createTextChannel(name).queue();
            case "voice" -> this.guild.createVoiceChannel(name).queue();
            case "category" -> this.guild.createCategory(name).queue();
            default -> throw new IllegalArgumentException("Invalid channel type: " + type);
        }

    }

    public Channel getChannel(String name) {
        return this.guild.getChannels().stream().filter(channel -> channel.getName().equals(name)).findFirst().orElse(null);
    }

}
