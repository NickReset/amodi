package social.nickrest.command.data;

import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * Used to create command arguments.
 *
 * @author Nick
 * @since 2/26/23
 * */
public record CommandOption(OptionType type, String name, String description, boolean required) {
    public static CommandOption of(OptionType type, String name, String description, boolean required) {
        return new CommandOption(type, name, description, required);
    }
}
