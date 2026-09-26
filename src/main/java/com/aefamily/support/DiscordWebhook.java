package com.aefamily.support;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class DiscordWebhook {
    private DiscordWebhook() {
    }

    public static void send(String webhookUrl,
                            String title,
                            String description,
                            int color,
                            Map<String, String> fields) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(webhookUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String payload = buildPayload(title, description, color, fields);
        byte[] data = payload.getBytes(StandardCharsets.UTF_8);
        connection.setRequestProperty("Content-Length", Integer.toString(data.length));

        try (OutputStream os = connection.getOutputStream()) {
            os.write(data);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode >= 300) {
            throw new IOException("Discord webhook responded with status " + responseCode);
        }
        connection.getInputStream().close();
    }

    private static String buildPayload(String title,
                                       String description,
                                       int color,
                                       Map<String, String> fields) {
        StringBuilder json = new StringBuilder();
        json.append("{\"embeds\":[{");
        boolean hasPrev = false;

        if (title != null && !title.isEmpty()) {
            json.append("\"title\":\"").append(escape(title)).append("\"");
            hasPrev = true;
        }

        if (description != null && !description.isEmpty()) {
            if (hasPrev) {
                json.append(',');
            }
            json.append("\"description\":\"").append(escape(description)).append("\"");
            hasPrev = true;
        }

        if (color >= 0) {
            if (hasPrev) {
                json.append(',');
            }
            json.append("\"color\":").append(color);
            hasPrev = true;
        }

        if (fields != null && !fields.isEmpty()) {
            if (hasPrev) {
                json.append(',');
            }
            json.append("\"fields\":[");
            boolean firstField = true;
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                if (!firstField) {
                    json.append(',');
                }
                json.append("{\"name\":\"")
                        .append(escape(entry.getKey()))
                        .append("\",\"value\":\"")
                        .append(escape(entry.getValue()))
                        .append("\",\"inline\":false}");
                firstField = false;
            }
            json.append(']');
        }

        json.append("}]}");
        return json.toString();
    }

    private static String escape(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder();
        for (char ch : input.toCharArray()) {
            switch (ch) {
                case '\\':
                case '\"':
                    escaped.append('\\').append(ch);
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    escaped.append(ch);
            }
        }
        return escaped.toString();
    }
}
