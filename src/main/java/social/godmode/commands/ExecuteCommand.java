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
import social.godmode.nashorn.JavaScriptEngine;
import social.godmode.util.EmbedGenerator;
import social.nickrest.command.Command;
import social.nickrest.command.data.CommandInfo;

import javax.script.CompiledScript;
import java.util.HashMap;
import java.util.Objects;

@CommandInfo(
        name = "execute",
        description = "Executes GPT-3.5",
        guildOnly = true
)
public class ExecuteCommand extends Command {

    private final HashMap<String, CompiledScript> cache = new HashMap<>();

    @Override
    public void handle(@NotNull SlashCommandInteractionEvent event) {
        new Thread(() -> {
            event.deferReply().queue();

            String query = Objects.requireNonNull(event.getOption("query")).getAsString();
            Main.getLogger().info(query);
            long startResponse = System.currentTimeMillis();
            String response = null;
            if (!cache.containsKey(query)) {
                response = OpenAI.sendRequest(query);
            }
            long endResponse = System.currentTimeMillis();
            long responseTime = endResponse - startResponse; // in milliseconds

            if(response == null && !cache.containsKey(query)) {
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

            long executionStart = System.currentTimeMillis(), executionEnd = -1, executionTime = -1;
            try {
                JavaScriptEngine engine;
                if (response != null) {
                    engine = new JavaScriptEngine(response, jda, guild, channel, member);
                } else {
                    engine = new JavaScriptEngine(cache.get(query), jda, guild, channel, member);
                }
                if (engine.compiledScript != null) {
                    cache.put(query, engine.compiledScript);
                }
                executionEnd = System.currentTimeMillis();
                executionTime = executionEnd - executionStart; // in milliseconds

                if (engine.invalidPrompt != null) {
                    EmbedBuilder errorEmbed = EmbedGenerator.errorEmbed(engine.invalidPrompt, "Response time: " + responseTime + "ms — Execution time: " + executionTime + "ms");
                    event.getHook().editOriginalEmbeds(errorEmbed.build()).queue();
                    return;
                }

                engine.terminate();

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
            } catch (Exception e) {
                EmbedBuilder doneEmbed = EmbedGenerator.doneEmbed(response, "Response time: " + responseTime + "ms — Execution time: " + executionTime + "ms");
                EmbedBuilder errorEmbed = EmbedGenerator.errorEmbed(e.getMessage(), "Response time: " + responseTime + "ms");
                
                if(executionTime != -1) event.getHook().editOriginalEmbeds(doneEmbed.build(), errorEmbed.build()).queue();
                else event.getHook().editOriginalEmbeds(errorEmbed.build()).queue();
            }
        }).start();
    }
}
