package social.godmode;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import social.godmode.commands.ExecuteCommand;
import social.godmode.commands.PingCommand;
import social.nickrest.command.Command;
import social.nickrest.command.CommandManager;
import social.nickrest.command.EmptySubCommand;
import social.nickrest.command.SubCommand;
import social.nickrest.command.data.CommandOption;

import java.util.Collection;

@Getter
public class Discord {

    private final JDA jda;

    private final CommandManager commandManager;

    public Discord(String token) {
        Collection<GatewayIntent> allIntents = GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS);
        this.jda = JDABuilder
                .createDefault(token)
                .enableIntents(allIntents)
                .build();

        try {
            this.jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.commandManager = new CommandManager(this.jda);
        this.commandManager.register(
                new PingCommand(),
                new ExecuteCommand().subCommand(new EmptySubCommand().option(CommandOption.of(OptionType.STRING, "query", "The query to execute", true)))
                // * add more commands since this can only be called once do like , new Command(), new Command(), new Command()
        );

        this.jda.getPresence().setActivity(Activity.watching("you."));
//        String response = Main.getInstance().getOpenAI().sendRequest("Say hi to every text channel 'hi ${channel.name}'");
//        Main.getLogger().info(response);
//
//        new JavaScriptEngine(response, jda, jda.getGuilds().get(0), jda.getGuilds().get(0).getChannels().get(0), jda.getGuilds().get(0).getMembers().get(0));
    }
}
