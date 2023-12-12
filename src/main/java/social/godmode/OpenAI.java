package social.godmode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class OpenAI {
    private final String endpoint = "http://localhost:1337/v1/";
    // load prompt.txt from resources
    private final String prompt = Files.readString(Path.of("src/main/resources/prompt.txt"));


    public OpenAI() throws IOException {
    }

    public String sendRequest(String userInput) {
        try {
            JSONObject body = new JSONObject();
            body.put("model", "gpt-4-32k-0613");
            JSONArray messages = new JSONArray();
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", prompt);
            messages.put(systemMessage);
            JSONObject messageObj = new JSONObject();
            messageObj.put("role", "user");
            messageObj.put("content", userInput);
            messages.put(messageObj);
            body.put("messages", messages);
            System.out.println(body);
            URL chatGPT = new URL(endpoint + "chat/completions");
            HttpURLConnection connection = (HttpURLConnection) chatGPT.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            System.out.println(connection.getResponseCode());
            System.out.println(connection.getResponseMessage());
            // get the response
            JSONObject response = new JSONObject(new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            // choices.get(0).message.content
            JSONObject choices = response.getJSONArray("choices").getJSONObject(0);
            String content = choices.getJSONObject("message").getString("content");
            if (content.startsWith("<!DOCTYPE html>")) {
                System.out.println("Trying Again");
                return sendRequest(userInput);
            }
            return content;
//            return response.toString();
        } catch (Exception e) {
            Main.getLogger().error("Failed to send request to OpenAI proxy.");
            e.printStackTrace();
            return null;
        }
    }
}
