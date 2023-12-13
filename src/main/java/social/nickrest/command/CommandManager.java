package social.nickrest.command;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import social.godmode.Main;
import social.nickrest.command.data.CommandOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager {

    @Getter
    private final List<Command> commands = new ArrayList<>();

    @Getter
    private final JDA jda;

    @Getter
    private final CommandListener listener;

    public CommandManager(@NotNull JDA jda) {
        this.jda = jda;
        this.listener = new CommandListener(this);
        this.jda.addEventListener(listener);
    }

    /**
     * Registers a command.
     * @param command The command to register.
     */
    public void register(@NotNull Command... command) {
        commands.addAll(Arrays.asList(command));

        List<SlashCommandData> commandData = new ArrayList<>();
        commands.forEach(c -> {
            SlashCommandData data = Commands.slash(c.getName(), c.getDescription())
                    .setDefaultPermissions(c.getDefaultMemberPermissions())
                    .setGuildOnly(c.isGuildOnly());

            c.getSubCommands().forEach(subCommand -> {
                CommandOption option = subCommand.getOption();
                data.addOption(option.type(), option.name(), option.description(), option.required());
            });

            commandData.add(data);

            Main.getLogger().info("Registered command " + c.getName());
        });
        jda.updateCommands().addCommands(commandData).queue();
    }

    /**
     * Gets a command by name.
     * @param name The name of the command.
     * @return The command.
     */
    public Command getCommand(@NotNull String name) {
        return commands.stream().filter(command -> command.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

}
