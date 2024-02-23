package tasktracker.api;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Постман: https://www.getpostman.com/collections/a83b61d9e1c81c10575c
 */
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
        try {
            System.out.println("\n/load");
            if (!isAuthorized(exchange)) {
                System.out.println("Запрос не авторизован, нужен параметр в query API_TOKEN со значением апи-ключа");
                exchange.sendResponseHeaders(403, 0);
                return;
            }
            if ("GET".equals(exchange.getRequestMethod())) {
                String key = exchange.getRequestURI().getPath().substring("/load/".length());
                System.out.println(key);
                if (key.isEmpty()) {
                    System.out.println("Key для загрузки пустой. key указывается в пути: /load/{key}");
                    exchange.sendResponseHeaders(400, 0);
                    return;
                }
                if (data.containsKey(key)) {
                    sendText(exchange, data.get(key));
                    System.out.println("Значение для ключа " + key + " успешно передано!");
                    exchange.sendResponseHeaders(200, 0);
                } else {
                    sendText(exchange, "Нет такого ключа: " + key);
                    exchange.sendResponseHeaders(400, 0);
                }
            } else {
                System.out.println("/load ждёт GET-запрос, а получил: " + exchange.getRequestMethod());
                exchange.sendResponseHeaders(405, 0);
            }
        } finally {
            exchange.close();
        }
    }

    private void save(HttpExchange exchange) throws IOException {
        try {
            System.out.println("\n/save");
            if (!isAuthorized(exchange)) {
                System.out.println("Запрос не авторизован, нужен параметр в query API_TOKEN со значением апи-ключа");
                exchange.sendResponseHeaders(403, 0);
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                String key = exchange.getRequestURI().getPath().substring("/save/".length());
                if (key.isEmpty()) {
                    System.out.println("Key для сохранения пустой. key указывается в пути: /save/{key}");
                    exchange.sendResponseHeaders(400, 0);
                    return;
                }
                String value = readText(exchange);
                if (value.isEmpty()) {
                    System.out.println("Value для сохранения пустой. value указывается в теле запроса");
                    exchange.sendResponseHeaders(400, 0);
                    return;
                }
                data.put(key, value);
                System.out.println("Значение для ключа " + key + " успешно обновлено!");
                exchange.sendResponseHeaders(200, 0);
            } else {
                System.out.println("/save ждёт POST-запрос, а получил: " + exchange.getRequestMethod());
                exchange.sendResponseHeaders(405, 0);
            }
        } finally {
            exchange.close();
        }
    }

    private void register(HttpExchange exchange) throws IOException {
        try {
            System.out.println("\n/register");
            if ("GET".equals(exchange.getRequestMethod())) {
                sendText(exchange, apiToken);
            } else {
                System.out.println("/register ждёт GET-запрос, а получил " + exchange.getRequestMethod());
                exchange.sendResponseHeaders(405, 0);
            }
        } finally {
            exchange.close();
        }
    }

    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        System.out.println("Открой в браузере http://localhost:" + PORT + "/");
        System.out.println("API_TOKEN: " + apiToken);
        server.start();
    }

    public void stop() {
        System.out.println("Отключаем сервер на порту " + PORT);
        server.stop(0);
    }

    private String generateApiToken() {
        return "" + System.currentTimeMillis();
    }

    protected boolean isAuthorized(HttpExchange exchange) {
        String rawQuery = exchange.getRequestURI().getRawQuery();
        return rawQuery != null && (rawQuery.contains("API_TOKEN=" + apiToken) || rawQuery.contains("API_TOKEN=DEBUG"));
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

    public String getApiToken() {
        return apiToken;
    }
}
