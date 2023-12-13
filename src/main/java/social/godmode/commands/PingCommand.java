package social.godmode.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import social.nickrest.command.Command;
import social.nickrest.command.data.CommandInfo;

/**
 * Gets the ping of the bot
 *
 * @author Nick
 * @since 2/26/23
 * */
@CommandInfo(
        name = "ping",
        description = "Pong!"
)
public class PingCommand extends Command {

    /**
     * Handles the command
     *
     * @param event The slash command event
     */
    @Override
    public void handle(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Pong!")
                .addField("Rest Ping", (event.getJDA().getRestPing().complete()) + "ms", true)
                .addField("Gateway Ping", (event.getJDA().getGatewayPing()) + "ms", true);

        MessageEmbed embed = embedBuilder.build();

        event.getHook().editOriginalEmbeds(embed).queue();

    }

}
