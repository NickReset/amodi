package social.godmode.commands;

import net.dv8tion.jda.api.EmbedBuilder;
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
import social.godmode.util.EmbedGenerator;
import social.nickrest.command.Command;
import social.nickrest.command.data.CommandInfo;

import java.util.Date;
import java.util.Objects;

@CommandInfo(
        name = "execute",
        description = "Executes GPT-4",
        guildOnly = true
)
public class ExecuteCommand extends Command {

    @Override
    public void handle(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String query = Objects.requireNonNull(event.getOption("query")).getAsString();
        Main.getLogger().info(query);
        long startResponse = new Date().getTime();
        String response = OpenAI.sendRequest(query);
        long endResponse = new Date().getTime();
        long responseTime = endResponse - startResponse; // in milliseconds

        if(response == null) {
            EmbedBuilder errorEmbed = EmbedGenerator.errorEmbed("Failed to get response from OpenAI Proxy.", "Response time: " + responseTime + "ms");
            event.getHook().editOriginalEmbeds(errorEmbed.build()).queue();
            return;
        }

        EmbedBuilder warningEmbed = EmbedGenerator.warningEmbed(response, "Response time: " + responseTime + "ms");
        event.getHook().editOriginalEmbeds(warningEmbed.build()).queue();

        Main.getLogger().info(response);

        JDA jda = event.getJDA();
        Guild guild = event.getGuild();
        GuildChannel channel = event.getChannel().asTextChannel();
        Member member = event.getMember();

        long executionStart = new Date().getTime();
        JavaScriptEngine engine = new JavaScriptEngine(response, jda, guild, channel, member);
        long executionEnd = new Date().getTime();
        long executionTime = executionEnd - executionStart; // in milliseconds
        if (engine.invalidPrompt != null) {
            EmbedBuilder errorEmbed = EmbedGenerator.errorEmbed(engine.invalidPrompt, "Response time: " + responseTime + "ms — Execution time: " + executionTime + "ms");
            event.getHook().editOriginalEmbeds(errorEmbed.build()).queue();
            return;
        }

        EmbedBuilder doneEmbed = EmbedGenerator.doneEmbed(response, "Response time: " + responseTime + "ms — Execution time: " + executionTime + "ms");
        if (engine.logs.size() > 0) {
            StringBuilder logs = new StringBuilder();
            for (String log : engine.logs) {
                logs.append(log).append("\n");
            }
            EmbedBuilder logsEmbed = EmbedGenerator.logsEmbed(logs.toString());
            event.getHook().editOriginalEmbeds(doneEmbed.build(), logsEmbed.build()).queue();
        } else {
            event.getHook().editOriginalEmbeds(doneEmbed.build()).queue();
        }
    }
}
