package social.godmode;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import social.godmode.util.DotEnv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Main {
    @Getter
    public static Discord discord;
    @Getter
    public static Thread proxyProcess;
    @Getter
    public static Logger logger = LoggerFactory.getLogger(Main.class);
    @Getter
    public static OpenAI openAI;

    public static void main(String[] args) {

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

        try {
            openAI = new OpenAI();
        } catch (Exception e) {
            logger.error("Failed to find prompt.txt file.");
            e.printStackTrace();
            System.exit(-1);
        }

        logger.info(openAI.sendRequest("Delete the server."));

        DotEnv.config();

        discord = new Discord(System.getProperty("token"));
    }

    public static void setup() throws IOException, InterruptedException {
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

    public static void readProcessOutput(Process process) throws IOException {
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
}
