package social.godmode.util;

import lombok.experimental.UtilityClass;

import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class FileUtil {

    public String readString(Path path) {
        try {
            return Files.readString(path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
