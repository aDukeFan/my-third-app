package tasktracker.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.*;
import tasktracker.ManagerSaveException;
import tasktracker.manager.FileBackedTasksManager;
import tasktracker.manager.Managers;
import tasktracker.model.Epic;
import tasktracker.model.Sub;
import tasktracker.model.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.regex.Pattern;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final Gson gson = new Gson();

    private final HttpServer server;

    public FileBackedTasksManager getManager() {
        return manager;
    }

    private final FileBackedTasksManager manager = Managers.getDefaultFileBackedTasksManager();


    public HttpTaskServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        server.createContext("/tasks", this::handle);
    }

    public void start() {
        System.out.println("HTTP-сервер запущен на " + PORT + " порту.");
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен на " + PORT + " порту.");
    }

    private void handle(HttpExchange exchange) {
        /*TODO Нужно использовать try with resources
          у меня метод handle принимает HttpExchange, который не autocloseable,
          тогда как мне использовать try with resources?
         */
        try {
            String path = exchange.getRequestURI().getPath();
            String requestMethod = exchange.getRequestMethod();
            switch (requestMethod) {
                case "GET":
                    get(exchange, path);
                    return;
                case "DELETE":
                    delete(exchange, path);
                    return;
                case "POST":
                    post(exchange, path);
                    return;
                default:
                    System.out.println("invalid method " + requestMethod);
                    exchange.sendResponseHeaders(405, 0);
            }
        } catch (Exception exception) {
            throw new ManagerSaveException("WASTED...");
        } finally {
            exchange.close();
        }
    }


    private void get(HttpExchange exchange, String path) throws IOException {
        if (Pattern.matches("^/tasks/tasks$", path)) {
            writeResponse(exchange, gson.toJson(manager.getTasks()), 200);
        } else if (Pattern.matches("^/tasks/subs$", path)) {
            writeResponse(exchange, gson.toJson(manager.getSubs()), 200);
        } else if (Pattern.matches("^/tasks/epics$", path)) {
            writeResponse(exchange, gson.toJson(manager.getEpics()), 200);
        } else if (Pattern.matches("^/tasks/history$", path)) {
            if (manager.getHistory().isEmpty()) {
                writeResponse(exchange, "The history is empty.", 200);
            } else {
                writeResponse(exchange, gson.toJson(manager.getHistory()), 200);
            }
        } else if (Pattern.matches("^/tasks/priority$", path)) {
            writeResponse(exchange, gson.toJson(manager.getPrioritizedTasks()), 200);
        } else if (Pattern.matches("^/tasks/task/", path)) {
            String pathId = exchange.getRequestURI().getQuery()
                    .replaceFirst("id=", "");
            int id = parsePathId(pathId);
            if (id != -1) {
                if (manager.getTaskById(id) != null) {
                    writeResponse(exchange, gson.toJson(manager.getTaskById(id)), 200);
                } else {
                    writeResponse(exchange, "There is no task with id " + id, 405);
                }
            }
        } else if (Pattern.matches("^/tasks/sub/", path)) {
            String pathId = exchange.getRequestURI().getQuery()
                    .replaceFirst("id=", "");
            int id = parsePathId(pathId);
            if (id != -1) {
                if (manager.getSubById(id) != null) {
                    writeResponse(exchange, gson.toJson(manager.getSubById(id)), 200);
                } else {
                    writeResponse(exchange, "There is no sub with id " + id, 405);
                }
            }
        } else if (Pattern.matches("^/tasks/epic/", path)) {
            String pathId = exchange.getRequestURI().getQuery()
                    .replaceFirst("id=", "");
            int id = parsePathId(pathId);
            if (id != -1) {
                if (manager.getEpicById(id) != null) {
                    writeResponse(exchange, gson.toJson(manager.getEpicById(id)), 200);
                } else {
                    writeResponse(exchange, "There is no epic with id " + id, 405);
                }
            }
        } else {
            writeResponse(exchange, "invalid request", 405);
        }
    }

    private void delete(HttpExchange exchange, String path) throws IOException {
        if (Pattern.matches("^/tasks/tasks$", path)) {
            manager.clearAllTasks();
            writeResponse(exchange, "All tasks have been deleted", 200);
        } else if (Pattern.matches("^/tasks/subs$", path)) {
            manager.clearAllSubTasks();
            writeResponse(exchange, "All sub-tasks have been deleted", 200);
        } else if (Pattern.matches("^/tasks/epics$", path)) {
            manager.clearAllEpics();
            writeResponse(exchange, "All epic-tasks have been deleted", 200);
        } else if (Pattern.matches("^/tasks$", path)) {
            manager.clearAllTasks();
            manager.clearAllEpics();
            writeResponse(exchange, "All kinds of tasks have been deleted", 200);
        } else if (Pattern.matches("^/tasks/task/", path)) {
            String pathId = exchange.getRequestURI().getQuery()
                    .replaceFirst("id=", "");
            int id = parsePathId(pathId);
            if (id != -1) {
                if (manager.getTaskById(id) != null) {
                    manager.delTaskById(id);
                    writeResponse(exchange,
                            "Task with id " + id + " has been deleted", 200);
                } else {
                    writeResponse(exchange,
                            "There is no task with id " + id, 405);
                }
            }
        } else if (Pattern.matches("^/tasks/sub/", path)) {
            String pathId = exchange.getRequestURI().getQuery()
                    .replaceFirst("id=", "");
            int id = parsePathId(pathId);
            if (id != -1) {
                if (manager.getSubById(id) != null) {
                    manager.delSubTaskById(id);
                    writeResponse(exchange,
                            "Sub-task with id " + id + " has been deleted", 200);
                } else {
                    writeResponse(exchange,
                            "There is no sub with id " + id, 405);
                }
            }
        } else if (Pattern.matches("^/tasks/epic/", path)) {
            String pathId = exchange.getRequestURI().getQuery()
                    .replaceFirst("id=", "");
            int id = parsePathId(pathId);
            if (id != -1) {
                if (manager.getEpicById(id) != null) {
                    manager.delEpicById(id);
                    writeResponse(exchange,
                            "Epic-task with id " + id + " has been deleted",
                            200);
                } else {
                    writeResponse(exchange,
                            "There is no epic with id " + id,
                            405);
                }
            }
        } else {
            writeResponse(exchange,
                    "invalid request",
                    405);
        }
    }

    private void post(HttpExchange exchange, String path) throws IOException {
        if (Pattern.matches("^/tasks/task/make$", path)) {
            String json = new String(exchange.getRequestBody().readAllBytes());
            try {
                gson.fromJson(json, Task.class);
            } catch (JsonSyntaxException e) {
                writeResponse(exchange, "Invalid json format", 400);
            }
            Task taskFromGson = gson.fromJson(json, Task.class);
            int sizeBefore = manager.getTasks().size();
            manager.save(taskFromGson);
            int sizeAfter = manager.getTasks().size();
            if (sizeAfter > sizeBefore) {
                writeResponse(exchange,
                        "Task named \"" + taskFromGson.getName() + "\" be saved",
                        201);
            } else {
                writeResponse(exchange,
                        "Task named \"" + taskFromGson.getName() + "\" can't be saved, cause not valid",
                        200);
            }
        } else if (Pattern.matches("^/tasks/sub/make$", path)) {
            String json = new String(exchange.getRequestBody().readAllBytes());
            try {
                gson.fromJson(json, Sub.class);
            } catch (JsonSyntaxException e) {
                writeResponse(exchange, "Invalid json format", 400);
            }
            Sub taskFromGson = gson.fromJson(json, Sub.class);
            int sizeBefore = manager.getSubs().size();
            manager.save(taskFromGson);
            int sizeAfter = manager.getSubs().size();
            if (sizeAfter > sizeBefore) {
                writeResponse(exchange,
                        "Sub named \"" + taskFromGson.getName() + "\" be saved",
                        201);
            } else {
                writeResponse(exchange,
                        "Sub named \"" + taskFromGson.getName() + "\" can't be saved, cause not valid",
                        200);
            }
        } else if (Pattern.matches("^/tasks/epic/make$", path)) {
            String json = new String(exchange.getRequestBody().readAllBytes());
            try {
                gson.fromJson(json, Epic.class);
            } catch (JsonSyntaxException exception) {
                writeResponse(exchange,
                        "Invalid json format",
                        400);
            }
            Epic taskFromGson = gson.fromJson(json, Epic.class);
            manager.save(taskFromGson);
            writeResponse(exchange,
                    "Epic named \"" + taskFromGson.getName() + "\" be saved",
                    201);
        }
    }

    private int parsePathId(String pathId) {
        try {
            return Integer.parseInt(pathId);
        } catch (NullPointerException exception) {
            return -1;
        }
    }

    private void writeResponse(HttpExchange exchange, String response,
                               int code) throws IOException {
        if (response.isBlank()) {
            exchange.sendResponseHeaders(code, 0);
        } else {
            byte[] bytes = response.getBytes();
            exchange.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
        exchange.close();
    }
}
