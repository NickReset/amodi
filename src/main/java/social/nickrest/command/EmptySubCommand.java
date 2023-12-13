package social.nickrest.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This is used for empty subcommands, so you can access the option through other subCommands
 *
 * @author Nick
 * @since 2/27/23
 * */
public class EmptySubCommand extends SubCommand {

    @Override
    public void handle(@NotNull String name, @NotNull Command command, @NotNull SlashCommandInteractionEvent event) {}
}
