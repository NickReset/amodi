package social.godmode;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import social.godmode.commands.PingCommand;
import social.godmode.nashorn.JavaScriptEngine;
import social.nickrest.command.CommandManager;

@Getter
public class Discord {

    private final JDA jda;

    private final CommandManager commandManager;

    public Discord(String token) {
        this.jda = JDABuilder.createDefault(token).build();

        try {
            this.jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.commandManager = new CommandManager(this.jda);
        this.commandManager.register(
                new PingCommand()
                // * add more commands since this can only be called once do like , new Command(), new Command(), new Command()
        );

        this.jda.getPresence().setActivity(Activity.watching("you."));

        String response = Main.getInstance().openAI.sendRequest("Delete all channels. Make the server themed around chilling in the winter. Do not add any categories or messages.");
        Main.getLogger().info(response);

        new JavaScriptEngine(response, jda, jda.getGuilds().get(0), jda.getGuilds().get(0).getChannels().get(0), jda.getGuilds().get(0).getMembers().get(0));

    }
}
