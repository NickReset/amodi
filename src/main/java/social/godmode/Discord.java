package social.godmode;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import social.godmode.nashorn.JavaScriptEngine;

@Getter
public class Discord {

    private final JDA jda;

    public Discord(String token) {
        this.jda = JDABuilder.createDefault(token).build();

        try {
            this.jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.jda.getPresence().setActivity(Activity.watching("you."));

        String response = Main.getInstance().openAI.sendRequest("Delete all channels. Make the server themed around chilling in the winter. Do not add any categories or messages.");
        Main.getLogger().info(response);

        new JavaScriptEngine(response, jda, jda.getGuilds().get(0), jda.getGuilds().get(0).getChannels().get(0), jda.getGuilds().get(0).getMembers().get(0));

    }
}
