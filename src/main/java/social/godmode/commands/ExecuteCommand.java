package social.godmode.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@CommandInfo(
        name = "execute",
        description = "Executes GPT-3.5",
        guildOnly = true
)
public class ExecuteCommand extends Command {

    private final HashMap<String, String> cache = new HashMap<>();

    @Override
    public void handle(@NotNull SlashCommandInteractionEvent event) {
        new Thread(() -> {
            event.deferReply().queue();

            String query = Objects.requireNonNull(event.getOption("query")).getAsString();
            Main.getLogger().info(query);
            long startResponse = System.currentTimeMillis();
            String response = null;
            if (!cache.containsKey(query)) {
                int times = 0;
                while (response == null && times < 5) {
                    response = OpenAI.sendRequest(query + "\nCreate this in djs.");
                    times++;
                }
            } else {
                response = cache.get(query);
            }
            long endResponse = System.currentTimeMillis();
            long responseTime = endResponse - startResponse; // in milliseconds

            if(response == null && !cache.containsKey(query)) {
                EmbedBuilder errorEmbed = EmbedGenerator.errorEmbed("Failed to get response from OpenAI Proxy.", "Response time: " + responseTime + "ms");
                event.getHook().editOriginalEmbeds(errorEmbed.build()).queue();
                return;
            }

            cache.put(query, response);

            EmbedBuilder warningEmbed = EmbedGenerator.warningEmbed(response, "Response time: " + responseTime + "ms");
            event.getHook().editOriginalEmbeds(warningEmbed.build()).queue();

            Main.getLogger().info(response);

            JDA jda = event.getJDA();
            Guild guild = event.getGuild();
            GuildChannel channel = event.getChannel().asTextChannel();
            Member member = event.getMember();

            long executionStart = System.currentTimeMillis(), executionEnd, executionTime = -1;
            try {
                assert response != null;
                JavaScriptEngine engine = new JavaScriptEngine(response, jda, guild, channel, member);
                executionEnd = System.currentTimeMillis();
                executionTime = executionEnd - executionStart; // in milliseconds

                if (engine.invalidPrompt != null) {
                    EmbedBuilder errorEmbed = EmbedGenerator.errorEmbed(engine.invalidPrompt, "Response time: " + responseTime + "ms — Execution time: " + executionTime + "ms");
                    event.getHook().editOriginalEmbeds(errorEmbed.build()).queue();
                    return;
                }

                List<MessageEmbed> embeds = new ArrayList<>();
                EmbedBuilder doneEmbed = EmbedGenerator.doneEmbed(response, "Response time: " + responseTime + "ms — Execution time: " + executionTime + "ms");
                embeds.add(doneEmbed.build());
                if (engine.logs.size() > 0) {
                    StringBuilder logs = new StringBuilder();
                    for (String log : engine.logs) {
                        logs.append(log).append("\n");
                    }
                    EmbedBuilder logsEmbed = EmbedGenerator.logsEmbed(logs.toString());
                    embeds.add(logsEmbed.build());
                }
                try {
                    event.getHook().editOriginalEmbeds(embeds).queue();
                } catch (Exception ignored) {
                    assert member != null;
                    member.getUser().openPrivateChannel().queue(privateChannel -> {
                        privateChannel.sendMessageEmbeds(embeds).queue();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                List<MessageEmbed> embeds = new ArrayList<>();
                if (!response.contains("```")) {
                    cache.remove(query);
                    EmbedBuilder errorEmbed = EmbedGenerator.errorEmbed("Open AI returned an invalid response.", "Response time: " + responseTime + "ms");
                    embeds.add(errorEmbed.build());
                }
                EmbedBuilder doneEmbed = EmbedGenerator.doneEmbed(response, "Response time: " + responseTime + "ms — Execution time: " + executionTime + "ms");
                EmbedBuilder errorEmbed = EmbedGenerator.errorEmbed(e.getMessage(), "Response time: " + responseTime + "ms");
                embeds.add(errorEmbed.build());
                if(executionTime != -1) embeds.add(doneEmbed.build());
                try {
                    event.getHook().editOriginalEmbeds(embeds).queue();
                } catch (Exception ignored) {
                    assert member != null;
                    member.getUser().openPrivateChannel().queue(privateChannel -> {
                        privateChannel.sendMessageEmbeds(embeds).queue();
                    });
                }
            }
        }).start();
    }
}
