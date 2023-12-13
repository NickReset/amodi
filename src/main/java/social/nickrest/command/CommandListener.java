package social.nickrest.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import social.godmode.Main;

import java.util.List;

@AllArgsConstructor
public class CommandListener extends ListenerAdapter {

    @Getter
    private final CommandManager commandManager;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Command foundCommand = commandManager.getCommand(event.getName());

        if(foundCommand == null) {
            Main.getLogger().error("Command not found: " + event.getName());
            return;
        }

        List<OptionMapping> args = event.getOptions();
        boolean found = false;
        for (OptionMapping arg : args) {
            SubCommand subCommand = foundCommand.getSubCommand(arg.getName());

            if(subCommand == null) {
                Main.getLogger().error("SubCommand not found: " + arg.getName());
                continue;
            }

            subCommand.handle(arg.getName(), foundCommand, event);
            found = true;
        }

        if(found) return;

        foundCommand.handle(event);
    }
}
