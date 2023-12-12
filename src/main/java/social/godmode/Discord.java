package social.godmode;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

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

    }
}
