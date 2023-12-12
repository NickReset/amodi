package social.godmode.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class DotEnv {

    private DotEnv(String path) {
        loadEnv(path);
    }

    private void loadEnv(String path) {
        File file = new File(path);

        if (!file.exists()) {
            throw new RuntimeException("Env File does not exist");
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split("=");
                System.getProperties().put(split[0], split[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void config(String path) {
        new DotEnv(path);
    }

    public static void config() {
        config(".env");
    }

}
