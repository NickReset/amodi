package social.godmode.nashorn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.json.JSONObject;
import social.godmode.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
public class DiscordClientNashorn {
    private final JDA jda;
    private final Guild guild;
    private String invalidPrompt;
    private final GuildChannel sentChannel;
    private final Member sentMember;

    public DiscordClientNashorn(JDA jda, Guild guild, GuildChannel sentChannel, Member sentMember) {
        this.jda = jda;
        this.guild = guild;
        this.sentChannel = sentChannel;
        this.sentMember = sentMember;
    }

    public void createChannel(String name, String type) {
        switch (type) {
            case "text" -> this.guild.createTextChannel(name).queue();
            case "voice" -> this.guild.createVoiceChannel(name).queue();
            case "category" -> this.guild.createCategory(name).queue();
            default -> throw new IllegalArgumentException("Invalid channel type: " + type);
        }
    }

    public void deleteChannel(String nameOrId) {
        IChannel Ichannel = getChannel(nameOrId);
        GuildChannel channel = this.guild.getGuildChannelById(Ichannel.id);
        if (channel == null) {
            sendInvalidPrompt("Channel does not exist.");
            return;
        }
        channel.delete().queue();
    }

    public void sendInvalidPrompt(String str) {
        this.invalidPrompt = str;
    }

    public void kickMember(String id) {
        Member member = this.guild.getMemberById(id);

        if(member == null) {
            sendInvalidPrompt("Member does not exist.");
            return;
        }

        member.kick().queue();
    }

    public void banMember(String id) {
        Member member = this.guild.getMemberById(id);

        if(member == null) {
            sendInvalidPrompt("Member does not exist.");
            return;
        }

        member.ban(0, TimeUnit.SECONDS).queue();
    }

    public void unbanMember(String id) {
        User user = this.jda.getUserById(id);
        if (user == null) {
            sendInvalidPrompt("User does not exist.");
            return;
        }
        this.guild.unban(user).queue();
    }

    public List<IMember> getBannedMembers() {
        Guild.Ban[] bans = this.guild.retrieveBanList().complete().toArray(new Guild.Ban[0]);
        List<IMember> bannedMembers = new ArrayList<>();
        for (Guild.Ban ban : bans) {
            bannedMembers.add(new IMember(ban.getUser().getId(), ban.getUser().getName()));
        }

        return bannedMembers;
    }

    public void messageMember(String id, String message) {
        Member member = this.guild.getMemberById(id);

        if(member == null) {
            sendInvalidPrompt("Member does not exist.");
            return;
        }

        member.getUser().openPrivateChannel().queue((channel) -> channel.sendMessage(message).queue());
    }

    public void log(String str) {
        Main.getLogger().info(str);
    }

    public IMember getMemberWhoExecutedCommand() {
        return new IMember(this.sentMember.getId(), this.sentMember.getUser().getName());
    }

    public IChannel getChannelWhereCommandWasExecuted() {
        return new IChannel(this.sentChannel.getId(), this.sentChannel.getName(), this.sentChannel.getType().toString().toLowerCase());
    }

    public IMessage getMessage(String messageID) {
        Message message = this.guild.getTextChannelById(this.sentChannel.getId()).retrieveMessageById(messageID).complete();

        if(message == null) {
            sendInvalidPrompt("Message does not exist.");
            return null;
        }

        return new IMessage(message.getId(), message.getContentRaw(), message.getAuthor().getId(), message.getChannel().getId());
    }

    public IMessage[] getMessages(String channelID, int limit) {
        List<IMessage> messages = new ArrayList<>();

        // check if channel exists
        TextChannel channel = this.guild.getTextChannelById(channelID);
        if(channel == null) {
            sendInvalidPrompt("Channel does not exist.");
            return null;
        }

        Message[] messagesArray = channel.getHistory().retrievePast(limit).complete().toArray(new Message[0]);

        for (Message message : messagesArray) {
            messages.add(new IMessage(message.getId(), message.getContentRaw(), message.getAuthor().getId(), message.getChannel().getId()));
        }

        return messages.toArray(new IMessage[0]);
    }

    public void sendMessageInChannel(String channelID, String message) {
        TextChannel channel = this.jda.getTextChannelById(channelID);

        if(channel == null) {
            sendInvalidPrompt("Channel does not exist.");
            return;
        }

        channel.sendMessage(message).queue();
    }

    public List<IMember> getMembers() {
        List<IMember> memberArrayLists = new ArrayList<>();

        Member[] members = this.guild.getMembers().toArray(new Member[0]);

        for (Member member : members) {
            memberArrayLists.add(new IMember(member.getId(), member.getUser().getName()));
        }

        return memberArrayLists;
    }

    public IMember getMember(String id) {
        Member member = this.guild.getMemberById(id);

        if(member == null) {
            sendInvalidPrompt("Member " + id + " does not exist.");
            return null;
        }

        return new IMember(member.getId(), member.getUser().getName());
    }

    public IChannel getChannel(String nameOrId) {
//        try name first
        IChannel channel = getChannels().stream().filter(c -> c.name.equals(nameOrId)).findFirst().orElse(null);

        if(channel == null) {
            channel = getChannels().stream().filter(c -> c.id.equals(nameOrId)).findFirst().orElse(null);
        }

        sendInvalidPrompt("Channel " + nameOrId + " does not exist.");
        return channel;
    }

    public List<IChannel> getChannels() {
        List<IChannel> channelArrayLists = new ArrayList<>();

        Channel[] channels = this.guild.getChannels().toArray(new Channel[0]);

        for (Channel channel : channels) {
            channelArrayLists.add(new IChannel(channel.getId(), channel.getName(), channel.getType().toString().toLowerCase()));
        }

        return channelArrayLists;
    }

}

@AllArgsConstructor
class IChannel {
    public String id;
    public String name;
    public String type;
}

@AllArgsConstructor
class IMember {
    public String id;
    public String name;
}

@AllArgsConstructor
class IMessage {
    public String id;
    public String content;
    public String authorID;
    public String channelID;
}