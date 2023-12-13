package social.nickrest.command;

import lombok.Getter;
import lombok.Setter;
import social.nickrest.command.data.CommandInfo;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class Command {

    private final String name, description;
    private final boolean guildOnly;
    private final List<SubCommand> subCommands;

    private DefaultMemberPermissions defaultMemberPermissions;

    public Command() {
        CommandInfo info = getClass().getAnnotation(CommandInfo.class);

        if(info == null) {
            throw new NullPointerException("CommandInfo annotation not found!");
        }

        name = info.name();
        description = info.description();
        guildOnly = info.guildOnly();
        defaultMemberPermissions = DefaultMemberPermissions.ENABLED;
        subCommands = new ArrayList<>();
    }

    public Command(String name, String description, boolean guildOnly) {
        this.name = name;
        this.description = description;
        this.guildOnly = guildOnly;
        this.defaultMemberPermissions = DefaultMemberPermissions.ENABLED;
        this.subCommands = new ArrayList<>();
    }

    public void handle(@NotNull SlashCommandInteractionEvent event){}

    public Command subCommand(SubCommand subCommand) {

        if(subCommand.getOption() == null) {
            throw new NullPointerException(subCommand.getClass().getName() + " is missing CommandOption class! please add .option(CommandOption) to where you register the subcommand!");
        }

        subCommands.add(subCommand);
        return this;
    }

    public Command defaultMemberPermissions(DefaultMemberPermissions defaultMemberPermissions) {
        this.defaultMemberPermissions = defaultMemberPermissions;
        return this;
    }

    public SubCommand getSubCommand(String name) {
        return subCommands.stream().filter(subCommand -> subCommand.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
