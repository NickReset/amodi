package social.godmode.nashorn;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import social.godmode.Main;

import java.util.concurrent.TimeUnit;

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

    public void deleteChannel(String name) {
        Channel channel = this.getChannel(name);

        if(channel == null) throw new IllegalArgumentException(String.format("Channel %s does not exist", name));

        channel.delete().queue();
    }

    public void sendInvalidPrompt(String str) {
        // TODO
    }

    public void kickMember(String id) {
        Member member = this.getMember(id);

        if(member == null) throw new IllegalArgumentException(String.format("Member %s does not exist", id));

        member.kick().queue();
    }

    public void banMember(String id) {
        // TODO
    }

    public void unbanMember(String id) {
        // TODO
    }

    public Member[] getBannedMembers() {
        // TODO
        return null;
    }

    public void messageMember(String id, String message) {
        // TODO
    }

    public void log(String str) {
        Main.getLogger().info(str);
    }

    public void getMemberWhoExecutedCommand() {
        // TODO
    }

    public Channel getChannelWhereCommandWasExecuted() {
        // TODO
        return null;
    }

    public Message getMessage(String messageID) {
        // TODO
        return null;
    }

    public Message[] getMessages(String channelID, int limit) {
        // TODO
        return null;
    }

    public void deleteMessagesInChannel(String channelID, int limit) {
        // TODO
    }

    public void reactMessage(String messageID, String emoji) {
        // TODO
    }

    public Member[] getReactedUsers(String messageID, String emoji) {
        // TODO
        return null;
    }

    public void sendMessageInChannel(String channelID, String message) {
        TextChannel channel = this.jda.getTextChannelById(channelID);

        if(channel == null) throw new IllegalArgumentException(String.format("Channel %s does not exist", channelID));

        channel.sendMessage(message).queue();
    }

    public Emoji[] getEmojis() {
        // TODO
        return null;
    }

    public Member[] getMembers() {
        return this.guild.getMembers().toArray(new Member[0]);
    }

    public Member getMember(String id) {
        return this.guild.getMemberById(id);
    }

    public Channel getChannel(String name) {
        return this.guild.getChannels().stream().filter(channel -> channel.getName().equals(name)).findFirst().orElse(null);
    }

    public Channel[] getChannels() {
        return this.guild.getChannels().toArray(new Channel[0]);
    }

}
