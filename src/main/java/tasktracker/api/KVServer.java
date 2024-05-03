package tasktracker.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KVServer {
    private static final int PORT = 8078;
    private final String apiToken;
    private final HttpServer server;
    private final Map<String, String> data = new HashMap<>();

    public KVServer() throws IOException {
        apiToken = generateApiToken();
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        server.createContext("/register", this::register);
        server.createContext("/save", this::save);
        server.createContext("/load", this::load);
    }

    private void load(HttpExchange exchange) throws IOException {
        try (exchange) {
            System.out.println("\n/load");
            if (isAuthorized(exchange)) {
                System.out.println("parameter of query API_TOKEN is not found");
                exchange.sendResponseHeaders(403, 0);
                return;
            }
            if ("GET".equals(exchange.getRequestMethod())) {
                String key = exchange.getRequestURI().getPath().substring("/load/".length());
                System.out.println(key);
                if (key.isEmpty()) {
                    System.out.println("Key is empty, please, write key: /load/{key}");
                    exchange.sendResponseHeaders(400, 0);
                    return;
                }
                if (data.containsKey(key)) {
                    sendText(exchange, data.get(key));
                    System.out.println("the value for the key has been successfully transmitted: " + key);
                    exchange.sendResponseHeaders(200, 0);
                } else {
                    sendText(exchange, "No such key: " + key);
                    exchange.sendResponseHeaders(400, 0);
                }
            } else {
                System.out.println("/load request must be GET, but " + exchange.getRequestMethod());
                exchange.sendResponseHeaders(405, 0);
            }
        }
    }

    private void save(HttpExchange exchange) throws IOException {
        try (exchange) {
            System.out.println("\n/save");
            if (isAuthorized(exchange)) {
                System.out.println("parameter of query API_TOKEN is not found");
                exchange.sendResponseHeaders(403, 0);
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                String key = exchange.getRequestURI().getPath().substring("/save/".length());
                if (key.isEmpty()) {
                    System.out.println("Key is empty, please, write key: /save/{key}");
                    exchange.sendResponseHeaders(400, 0);
                    return;
                }
                String value = readText(exchange);
                if (value.isEmpty()) {
                    System.out.println("Value is empty. value must be in request's body");
                    exchange.sendResponseHeaders(400, 0);
                    return;
                }
                data.put(key, value);
                System.out.println("the value for the key has been successfully updated: " + key);
                exchange.sendResponseHeaders(200, 0);
            } else {
                System.out.println("/save request must be POST, but " + exchange.getRequestMethod());
                exchange.sendResponseHeaders(405, 0);
            }
        }
    }

    private void register(HttpExchange exchange) throws IOException {
        try (exchange) {
            System.out.println("\n/register");
            if ("GET".equals(exchange.getRequestMethod())) {
                sendText(exchange, apiToken);
            } else {
                System.out.println("/register request must be GET, but " + exchange.getRequestMethod());
                exchange.sendResponseHeaders(405, 0);
            }
        }
    }

    public void start() {
        System.out.println("KVServer has been started on port " + PORT);
        System.out.println("Open it on http://localhost:" + PORT + "/");
        System.out.println("API_TOKEN: " + apiToken);
        server.start();
    }

    public void stop() {
        System.out.println("KVServer has been stopped on port " + PORT);
        server.stop(0);
    }

    private String generateApiToken() {
        return "" + System.currentTimeMillis();
    }

    protected boolean isAuthorized(HttpExchange exchange) {
        String rawQuery = exchange.getRequestURI().getRawQuery();
        return rawQuery == null
                || (!rawQuery.contains("API_TOKEN=" + apiToken)
                && !rawQuery.contains("API_TOKEN=DEBUG"));
    }

    protected String readText(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), UTF_8);
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] resp = text.getBytes(UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
    }
}
