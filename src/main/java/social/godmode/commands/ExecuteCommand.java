package social.godmode.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import social.godmode.Main;
import social.godmode.OpenAI;
import social.godmode.nashorn.DiscordClientNashorn;
import social.godmode.nashorn.JavaScriptEngine;
import social.nickrest.command.Command;
import social.nickrest.command.data.CommandInfo;

import java.util.Objects;

@CommandInfo(
        name = "execute",
        description = "Executes GPT-4",
        guildOnly = true
)
public class ExecuteCommand extends Command {

    @Override
    public void handle(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        String query = Objects.requireNonNull(event.getOption("query")).getAsString();
        Main.getLogger().info(query);
        String response = OpenAI.sendRequest(query);

        if(response == null) {
            event.getHook().editOriginal("Failed to send request to OpenAI proxy.").queue();
            return;
        }

        Main.getLogger().info(response);

        JDA jda = event.getJDA();
        Guild guild = event.getGuild();
        GuildChannel channel = event.getChannel().asTextChannel();
        Member member = event.getMember();

        new JavaScriptEngine(response, jda, guild, channel, member);

        event.getHook().editOriginal(response).queue();
    }
}
