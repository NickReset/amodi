package social.godmode;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import social.godmode.util.DotEnv;

import java.io.*;

@Getter
public class Main {

    @Getter
    public static Logger logger = LoggerFactory.getLogger(Main.class);
    @Getter
    private static Main instance;

    private final Discord discord;

    private Thread proxyProcess;

    public Main() {
        instance = this;

        try {
            setup();
            logger.info("Successfully setup g4f proxy.");
        } catch (Exception error) {
            if (proxyProcess != null) {
                proxyProcess.interrupt();
            }
            logger.error("Failed to setup g4f proxy.");
            error.printStackTrace();
            System.exit(-1);
        }

        DotEnv.config();
        System.setProperty("nashorn.args", "--language=es6");

        discord = new Discord(System.getProperty("token"));
    }

    public void setup() throws IOException {
        // Replace "your_g4f_api_command" with the actual command to call the "g4f api"
        String[] g4fApiCommand = {"g4f", "api"};

        // Use ProcessBuilder for more control
        ProcessBuilder processBuilder = new ProcessBuilder(g4fApiCommand);
        processBuilder.redirectErrorStream(true); // Redirect error stream to output stream

        // Start the process
        Process process = processBuilder.start();

        // Read and print the output of the process (optional)
        readProcessOutput(process);
        Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));
    }

    public void readProcessOutput(Process process) throws IOException {
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("1337")) {
                break;
            }
        }

        reader.close();
    }

    public static void main(String[] args) {
        new Main();
    }
}
