package vg.civcraft.mc.civchat2;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class webhook {

    public static void send(String sender, String message) {
        try {
            String webhookUrl = "";
            String content = "🚨 **Filtered message**\n**Sender:** " + sender + "\n**Message:** " + message;

            // Escape JSON special characters
            content = content.replace("\"", "\\\"").replace("\n", "\\n");

            String json = "{\"content\": \"" + content + "\"}";

            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 204) {
                System.err.println("Webhook failed. Response code: " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
