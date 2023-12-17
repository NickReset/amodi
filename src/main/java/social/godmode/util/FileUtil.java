package social.godmode.util;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

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

    public String readInputStream(InputStream stream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines().collect(Collectors.joining("\n"));
    }
}
