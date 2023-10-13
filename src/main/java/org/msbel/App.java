package org.msbel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        Path postmanFolder = Paths.get("src/main/resources/postman");
        Path generatedFolder = Paths.get("src/main/resources/generated");

        logger.info("Starting the process of converting Postman collections to cURL commands.");

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(postmanFolder, "*.json")) {
            for (Path postmanFile : directoryStream) {
                logger.info("Processing file: " + postmanFile.toString());

                String postmanJson = new String(Files.readAllBytes(postmanFile));
                JSONObject postmanCollection = new JSONObject(postmanJson);
                JSONArray items = postmanCollection.getJSONArray("item");

                // Extract variables from the Postman collection
                Map<String, String> variables = new HashMap<>();
                if (postmanCollection.has("variable")) {
                    JSONArray variablesArray = postmanCollection.getJSONArray("variable");
                    for (int i = 0; i < variablesArray.length(); i++) {
                        JSONObject variable = variablesArray.getJSONObject(i);
                        variables.put(variable.getString("key"), variable.getString("value"));
                    }
                }

                Path generatedFile = generatedFolder.resolve(postmanFile.getFileName().toString().replace(".json", ".txt"));

                try (BufferedWriter writer = Files.newBufferedWriter(generatedFile)) {
                    logger.info("Generating cURL commands for: " + generatedFile.toString());

                    processItems(items, variables, writer);

                    logger.info("cURL commands have been saved to " + generatedFile);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error writing to file: " + generatedFile.toString(), e);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading from Postman folder: " + postmanFolder.toString(), e);
        }

        logger.info("Process completed.");
    }

    private static void processItems(JSONArray items, Map<String, String> variables, BufferedWriter writer) throws IOException {
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (item.has("response")) {
                JSONArray responses = item.getJSONArray("response");
                for (int j = 0; j < responses.length(); j++) {
                    JSONObject response = responses.getJSONObject(j);
                    JSONObject originalRequest = response.getJSONObject("originalRequest");
                    String curlCommand = convertToCurl(originalRequest, variables);
                    writer.write(curlCommand);
                    writer.newLine();
                    writer.newLine();  // Separate cURL commands with an empty line
                }
            } else if (item.has("item")) {
                processItems(item.getJSONArray("item"), variables, writer);
            }
        }
    }

    private static String convertToCurl(JSONObject originalRequest, Map<String, String> variables) {
        String method = originalRequest.getString("method");
        String url = replaceVariables(originalRequest.getJSONObject("url").getString("raw"), variables);

        StringBuilder curlCommand = new StringBuilder("curl -X " + method);

        // Add headers
        if (originalRequest.has("header")) {
            addHeaders(curlCommand, originalRequest.getJSONArray("header"), variables);
        }

        // Add URL
        curlCommand.append(String.format(" '%s'", url));

        // Add request body if exists and method supports it
        if (originalRequest.has("body") && ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method))) {
            JSONObject body = originalRequest.getJSONObject("body");
            if (body.has("raw")) {
                String rawData = replaceVariables(body.getString("raw"), variables);
                curlCommand.append(String.format(" --data-raw '%s'", rawData));
            }
        }

        return curlCommand.toString();
    }

    private static void addHeaders(StringBuilder curlCommand, JSONArray headers, Map<String, String> variables) {
        for (int i = 0; i < headers.length(); i++) {
            JSONObject header = headers.getJSONObject(i);
            if (header.has("key")) {
                String key = header.getString("key");
                String value = replaceVariables(header.getString("value"), variables);
                curlCommand.append(String.format(" -H '%s: %s'", key, value));
            }
        }
    }

    private static String replaceVariables(String str, Map<String, String> variables) {
        String result = str;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
            result = result.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return result;
    }
}
