package tasktracker.api;

import tasktracker.ManagerSaveException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {

    private final String apiToken;

    private final HttpClient client;

    private static final String LOCAL_HOST = "http://localhost:8078";

    public KVTaskClient(String url) {
        try {
            URI uri = URI.create(url);
            client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Status code: " + response.statusCode());
                System.out.println("Key: " + response.body());
                this.apiToken = response.body();
            } catch (IOException | InterruptedException e) {
                throw new ManagerSaveException("Error, wrong request");
            }
        } catch (IllegalArgumentException e) {
            throw new ManagerSaveException("incorrect URL");
        }

    }

    public void put(String key, String json) {
        try {
            String url = LOCAL_HOST + "/save/" + key + "?API_TOKEN=" + apiToken;
            URI uri = URI.create(url);
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Accept", "application/json")
                    .uri(uri)
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException exception) {
            System.out.println("put is WASTED...");
        }
    }

    public String load(String key) {
        try {
            String url = LOCAL_HOST + "/load/" + key + "?API_TOKEN=" + apiToken;

            URI uri = URI.create(url);
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException exception) {
            System.out.println("load is WASTED...");
            return null;
        }
    }
}
