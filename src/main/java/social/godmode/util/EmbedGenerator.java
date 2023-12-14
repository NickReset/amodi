package social.godmode.util;

import net.dv8tion.jda.api.EmbedBuilder;

public class EmbedGenerator {
    public static EmbedBuilder doneEmbed(String body) {
        return new EmbedBuilder()
                .setTitle("Done!")
                .setDescription(body)
                .setColor(0x00FF00);
    }

    public static EmbedBuilder doneEmbed(String body, String footer) {
        return doneEmbed(body)
                .setFooter(footer);
    }

    public static EmbedBuilder errorEmbed(String body) {
        return new EmbedBuilder()
                .setTitle("Error!")
                .setDescription(body)
                .setColor(0xFF0000);
    }

    public static EmbedBuilder errorEmbed(String body, String footer) {
        return errorEmbed(body)
                .setFooter(footer);
    }

    public static EmbedBuilder warningEmbed(String body) {
        return new EmbedBuilder()
                .setTitle("Running!")
                .setDescription(body)
                .setColor(0xFFFF00);
    }

    public static EmbedBuilder warningEmbed(String body, String footer) {
        return warningEmbed(body)
                .setFooter(footer);
    }

    public static EmbedBuilder logsEmbed(String body) {
        return new EmbedBuilder()
                .setTitle("Logs!")
                .setDescription(body)
                .setColor(0x00FFFF);
    }
}
