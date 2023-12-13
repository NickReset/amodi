package social.nickrest.command;

import lombok.Getter;
import social.nickrest.command.data.CommandOption;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * A class that represents a subcommand
 *
 * @author Nick
 * @since 2/27/23
 * */
public abstract class SubCommand {

    @Getter
    private String name;

    @Getter
    private CommandOption option;

    /**
     * handles execution of the subcommand
     *
     * @param name The name of the subcommand
     * @param command The command that the subcommand is being executed from
     * */
    public abstract void handle(@NotNull String name, @NotNull Command command, @NotNull SlashCommandInteractionEvent event);

    /**
     * Sets the option for the subcommand
     *
     * @param option The option to set
     * */
    public SubCommand option(CommandOption option) {
        this.option = option;
        this.name = option.name();
        return this;
    }
}
