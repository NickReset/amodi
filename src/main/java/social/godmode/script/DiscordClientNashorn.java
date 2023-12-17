package social.godmode.script;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.Nullable;
import social.godmode.util.ScriptUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter @SuppressWarnings("unused")
public class DiscordClientNashorn {
    private final JDA jda;
    private final Guild guild;
    private String invalidPrompt;
    private final GuildChannel sentChannel;
    private final Member sentMember;
    private final JavaScriptEngine engine;

    public DiscordClientNashorn(JDA jda, Guild guild, GuildChannel sentChannel, Member sentMember, JavaScriptEngine engine) {
        this.jda = jda;
        this.guild = guild;
        this.sentChannel = sentChannel;
        this.sentMember = sentMember;
        this.engine = engine;
    }

    public Object createChannel(String name, String type, boolean fromJava) {
        GuildChannel channel = switch (type) {
            case "text" -> this.guild.createTextChannel(name).complete();
            case "voice" -> this.guild.createVoiceChannel(name).complete();
            case "stage" -> this.guild.createStageChannel(name).complete();
            case "news" -> this.guild.createNewsChannel(name).complete();
            case "forum" -> this.guild.createForumChannel(name).complete();
            case "category" -> this.guild.createCategory(name).complete();
            default -> null;
        };

        return checkforInvaildCHannelTypeThenReturnObject(fromJava, channel);
    }

    public Object createChannel(String name, String type, String parentId, boolean fromJava) {
        Category parent = this.guild.getCategoryById(parentId);

        if (parent == null) {
            sendInvalidPrompt("Invalid parent id.", true);
            return null;
        }

        GuildChannel channel = switch (type) {
            case "text" -> this.guild.createTextChannel(name, parent).complete();
            case "voice" -> this.guild.createVoiceChannel(name, parent).complete();
            case "stage" -> this.guild.createStageChannel(name, parent).complete();
            case "news" -> this.guild.createNewsChannel(name, parent).complete();
            case "forum" -> this.guild.createForumChannel(name, parent).complete();
            default -> null;
        };

        return checkforInvaildCHannelTypeThenReturnObject(fromJava, channel);
    }

    public void deleteChannel(String nameOrId, boolean fromJava) {
        IChannel Ichannel = (IChannel) getChannel(nameOrId, true);
        GuildChannel channel = this.guild.getGuildChannelById(Ichannel.id);
        if (channel == null) {
            sendInvalidPrompt("Channel does not exist.", true);
            return;
        }
        channel.delete().queue();
    }

    public void sendInvalidPrompt(String str, boolean fromJava) {
        this.engine.invalidPrompt = str;
    }

    public void kickMember(String id, boolean fromJava) {
        Member member = this.guild.retrieveMemberById(id).complete();

        if(member == null) {
            sendInvalidPrompt("Member does not exist.", true);
            return;
        }

        member.kick().queue();
    }

    public void banMember(String id, boolean fromJava) {
        Member member = this.guild.retrieveMemberById(id).complete();

        if(member == null) {
            sendInvalidPrompt("Member does not exist.", true);
            return;
        }

        member.ban(0, TimeUnit.SECONDS).queue();
    }

    public void unbanMember(String id, boolean fromJava) {
        User user = this.jda.retrieveUserById(id).complete();
        if (user == null) {
            sendInvalidPrompt("User does not exist.", true);
            return;
        }
        this.guild.unban(user).queue();
    }

    public Object getBannedMembers(boolean fromJava) {
        Guild.Ban[] bans = this.guild.retrieveBanList().complete().toArray(new Guild.Ban[0]);
        List<IMember> bannedMembers = new ArrayList<>();
        for (Guild.Ban ban : bans) {
            bannedMembers.add(new IMember(ban.getUser().getId(), ban.getUser().getName()));
        }

        if (fromJava) return bannedMembers;
        else return ScriptUtil.toJSList(this.engine, bannedMembers.stream().map(member -> member.toJSObject(this.engine)).toList());
    }

    public void messageMember(String id, String message, boolean fromJava) {
        Member member = this.guild.retrieveMemberById(id).complete();

        if(member == null) {
            sendInvalidPrompt("Member does not exist.", true);
            return;
        }

        member.getUser().openPrivateChannel().queue((channel) -> channel.sendMessage(message).queue());
    }

    public void log(String str, boolean fromJava) {
        this.engine.logs.add(str);
    }

    public Object getMemberWhoExecutedCommand(boolean fromJava) {
        IMember member = new IMember(this.sentMember.getId(), this.sentMember.getUser().getName());
        if (fromJava) return member;
        else return member.toJSObject(this.engine);
    }

    public Object getChannelWhereCommandWasExecuted(boolean fromJava) {
        IChannel channel = new IChannel(this.sentChannel.getId(), this.sentChannel.getName(), this.sentChannel.getType().toString().toLowerCase());
        if (fromJava) return channel;
        else return channel.toJSObject(this.engine);
    }

    public Object getMessage(String channelId, String messageID, boolean fromJava) {
        TextChannel channel = this.jda.getTextChannelById(channelId);
        if (channel == null) {
            sendInvalidPrompt("Channel does not exist.", true);
            return null;
        }

        Message message = channel.retrieveMessageById(messageID).complete();

        if(message == null) {
            sendInvalidPrompt("Message does not exist.", true);
            return null;
        }

        IMessage iMessage = new IMessage(message.getId(), message.getContentRaw(), message.getAuthor().getId(), message.getChannel().getId());
        if (fromJava) return iMessage;
        else return iMessage.toJSObject(this.engine);
    }

    public Object getMessages(String channelID, int limit, boolean fromJava) {
        List<IMessage> messages = new ArrayList<>();

        // check if channel exists
        TextChannel channel = this.guild.getTextChannelById(channelID);
        if(channel == null) {
            sendInvalidPrompt("Channel does not exist.", true);
            return null;
        }

        Message[] messagesArray = channel.getHistory().retrievePast(limit).complete().toArray(new Message[0]);

        for (Message message : messagesArray) {
            messages.add(new IMessage(message.getId(), message.getContentRaw(), message.getAuthor().getId(), message.getChannel().getId()));
        }

        List<IMessage> messageList = List.of(messages.toArray(new IMessage[0]));
        if (fromJava) return messageList;
        else return ScriptUtil.toJSList(this.engine, messageList.stream().map(iMessage -> iMessage.toJSObject(this.engine)).toList());
    }

    public void sendMessageInChannel(String channelID, String message, boolean fromJava) {
        TextChannel channel = this.jda.getTextChannelById(channelID);

        if(channel == null) {
            sendInvalidPrompt("Channel does not exist.", true);
            return;
        }

        channel.sendMessage(message).queue();
    }

    public void getMessagesInChannel(String channelID, int limit, boolean fromJava) {
        // TODO
        sendInvalidPrompt("This method is not implemented yet.", true);
    }

    public Object getMembers(boolean fromJava) {
        List<IMember> memberArrayLists = new ArrayList<>();
        List<Member> members = this.guild.loadMembers().get();
        for (Member member : members) {
            if (member.getUser().isBot()) continue;
            memberArrayLists.add(new IMember(member.getId(), member.getUser().getName()));
        }
        if (memberArrayLists.isEmpty()) {
            sendInvalidPrompt("Could not load members.", true);
            return null;
        }
        if (fromJava) return memberArrayLists;
        else return ScriptUtil.toJSList(this.engine, memberArrayLists.stream().map(member -> member.toJSObject(this.engine)).toList());
    }

    public Object getMember(String id, boolean fromJava) {
        Member member = this.guild.retrieveMemberById(id).complete();

        if(member == null) {
            sendInvalidPrompt("Member " + id + " does not exist.", true);
            return null;
        }

        IMember iMember = new IMember(member.getId(), member.getUser().getName());
        if (fromJava) return iMember;
        else return iMember.toJSObject(this.engine);
    }

    @SuppressWarnings("unchecked")
    public Object getChannel(String nameOrId, boolean fromJava) {
//        try name first
        List<IChannel> channels = (List<IChannel>) getChannels(true);

        IChannel channel = channels.stream().filter(c -> c.name.equals(nameOrId)).findFirst().orElse(null);

        if(channel == null) {
            channel = channels.stream().filter(c -> c.id.equals(nameOrId)).findFirst().orElse(null);
        }
        if (channel == null) {
            sendInvalidPrompt("Channel " + nameOrId + " does not exist.", true);
            return null;
        }

        if (fromJava) return channel;
        else return channel.toJSObject(this.engine);
    }

    public void moveChannelIntoCategory(String nameOrId, String categoryID, boolean fromJava) {
        nameOrId = nameOrId.trim().replace("-", "");
        IChannel channel = (IChannel) getChannel(nameOrId, true);
        IChannel category = (IChannel) getChannel(categoryID, true);

        if (category == null) {
            sendInvalidPrompt("Category does not exist.", true);
            return;
        }

        if (channel == null) {
            sendInvalidPrompt("Channel does not exist.", true);
            return;
        }

        TextChannel guildChannel = this.guild.getTextChannelById(channel.id);
        GuildChannel guildCategory = this.guild.getGuildChannelById(category.id);

        if (!(guildCategory instanceof Category)) {
            sendInvalidPrompt("Channel is not a category.", true);
            return;
        }

        if (guildChannel == null) {
            sendInvalidPrompt("Channel does not exist.", true);
            return;
        }

        guildChannel.getManager()
                .setParent((Category) guildCategory)
                .queue();
    }

    public Object getChannels(boolean fromJava) {
        List<IChannel> channelArrayLists = new ArrayList<>();

        Channel[] channels = this.guild.getChannels().toArray(new Channel[0]);

        for (Channel channel : channels) {
            channelArrayLists.add(new IChannel(channel.getId(), channel.getName(), channel.getType().toString().toLowerCase()));
        }
        if (fromJava) return channelArrayLists;
        else return ScriptUtil.toJSList(this.engine, channelArrayLists.stream().map(c -> c.toJSObject(this.engine)).toList());
    }

    @Nullable
    private Object checkforInvaildCHannelTypeThenReturnObject(boolean fromJava, GuildChannel channel) {
        if(channel == null) {
            sendInvalidPrompt("Invalid channel type.", true);
            return null;
        }

        IChannel Ichannel = new IChannel(channel.getId(), channel.getName(), channel.getType().toString().toLowerCase());

        if (fromJava) return Ichannel;
        else return Ichannel.toJSObject(this.engine);
    }

    static class IBase {
        public Object toJSObject(JavaScriptEngine engine) {
            Field[] fields = this.getClass().getFields();
            Map<String, Object> map = new HashMap<>();
            for (Field field : fields) {
                try {
                    map.put(field.getName(), field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            return engine.getContext().asValue(map);
        }
    }

    @AllArgsConstructor
    static class IChannel extends IBase {
        public String id;
        public String name;
        public String type;
    }

    @AllArgsConstructor
    static class IMember extends IBase {
        public String id;
        public String name;
    }

    @AllArgsConstructor
    static class IMessage extends IBase {
        public String id;
        public String content;
        public String authorID;
        public String channelID;
    }

}