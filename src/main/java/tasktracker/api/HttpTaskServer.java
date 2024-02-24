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

    private final FileBackedTasksManager manager = Managers.getDefaultFileBackedTasksManager();

    public FileBackedTasksManager getManager() {
        return manager;
    }

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
        try {
            String requestMethod = exchange.getRequestMethod();
            switch (requestMethod) {
                case "GET":
                    get(exchange);
                    return;
                case "DELETE":
                    delete(exchange);
                    return;
                case "POST":
                    post(exchange);
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

    private void get(HttpExchange exchange) throws IOException {
        switch (getEndpoint(exchange)) {
            case TASKS:
                writeResponse(exchange, gson.toJson(manager.getTasks()), 200);
                break;
            case SUBS:
                writeResponse(exchange, gson.toJson(manager.getSubs()), 200);
                break;
            case EPICS:
                writeResponse(exchange, gson.toJson(manager.getEpics()), 200);
                break;
            case HISTORY:
                if (manager.getHistory().isEmpty()) {
                    writeResponse(exchange, "The history is empty.", 200);
                } else {
                    writeResponse(exchange, gson.toJson(manager.getHistory()), 200);
                }
                break;
            case PRIORITY:
                writeResponse(exchange, gson.toJson(manager.getPrioritizedTasks()), 200);
                break;
            case TASK:
                String pathTaskId = exchange.getRequestURI().getQuery().replaceFirst("id=", "");
                int taskId = parsePathId(pathTaskId);
                if (taskId != -1) {
                    if (manager.getTaskById(taskId) != null) {
                        writeResponse(exchange, gson.toJson(manager.getTaskById(taskId)), 200);
                    } else {
                        writeResponse(exchange, "There is no task with id " + pathTaskId, 405);
                    }
                }
                break;
            case SUB:
                String pathSubId = exchange.getRequestURI().getQuery().replaceFirst("id=", "");
                int subId = parsePathId(pathSubId);
                if (subId != -1) {
                    if (manager.getSubById(subId) != null) {
                        writeResponse(exchange, gson.toJson(manager.getSubById(subId)), 200);
                    } else {
                        writeResponse(exchange, "There is no sub with id " + pathSubId, 405);
                    }
                }
                break;
            case EPIC:
                String pathEpicId = exchange.getRequestURI().getQuery().replaceFirst("id=", "");
                int epicId = parsePathId(pathEpicId);
                if (epicId != -1) {
                    if (manager.getEpicById(epicId) != null) {
                        writeResponse(exchange, gson.toJson(manager.getEpicById(epicId)), 200);
                    } else {
                        writeResponse(exchange, "There is no epic with id " + pathEpicId, 405);
                    }
                }
                break;
            default:
                writeResponse(exchange, "invalid request", 405);
                break;
        }
    }

    private void delete(HttpExchange exchange) throws IOException {
        switch (getEndpoint(exchange)) {
            case TASKS:
                manager.clearAllTasks();
                writeResponse(exchange, "All tasks have been deleted", 200);
                break;
            case SUBS:
                manager.clearAllSubTasks();
                writeResponse(exchange, "All sub-tasks have been deleted", 200);
                break;
            case EPICS:
                manager.clearAllEpics();
                writeResponse(exchange, "All epic-tasks have been deleted", 200);
                break;
            case CLEAR:
                manager.clearAllTasks();
                manager.clearAllEpics();
                writeResponse(exchange, "All kinds tasks have been deleted", 200);
                break;
            case TASK:
                String pathTaskId = exchange.getRequestURI().getQuery().replaceFirst("id=", "");
                int id = parsePathId(pathTaskId);
                if (id != -1) {
                    if (manager.getTaskById(id) != null) {
                        manager.delTaskById(id);
                        writeResponse(exchange,
                                "Task with id " + id
                                        + " has been deleted", 200);
                    } else {
                        writeResponse(exchange, "There is no task with id " + pathTaskId, 405);
                    }
                }
                break;
            case SUB:
                String pathSubId = exchange.getRequestURI().getQuery().replaceFirst("id=", "");
                int subId = parsePathId(pathSubId);
                if (subId != -1) {
                    if (manager.getSubById(subId) != null) {
                        manager.delSubTaskById(subId);
                        writeResponse(exchange, "Sub-task with id " + subId + " has been deleted", 200);
                    } else {
                        writeResponse(exchange, "There is no sub with id " + pathSubId, 405);
                    }
                }
                break;
            case EPIC:
                String pathEpicId = exchange.getRequestURI().getQuery().replaceFirst("id=", "");
                int epicId = parsePathId(pathEpicId);
                if (epicId != -1) {
                    if (manager.getEpicById(epicId) != null) {
                        manager.delEpicById(epicId);
                        writeResponse(exchange,
                                "Epic-task with id "
                                        + epicId
                                        + " has been deleted", 200);
                    } else {
                        writeResponse(exchange,
                                "There is no epic with id "
                                        + pathEpicId, 405);
                    }
                }
                break;
            default:
                writeResponse(exchange, "invalid request", 405);
                break;
        }
    }

    private void post(HttpExchange exchange) throws IOException {
        switch (getEndpoint(exchange)) {
            case TASK:
                String jsonTask = new String(exchange.getRequestBody().readAllBytes());
                try {
                    gson.fromJson(jsonTask, Task.class);
                } catch (JsonSyntaxException e) {
                    writeResponse(exchange, "Invalid json format", 400);
                }
                Task taskFromGson = gson.fromJson(jsonTask, Task.class);
                int tasksSizeBeforeAdd = manager.getTasks().size();
                manager.save(taskFromGson);
                int tasksSizeAfterAdd = manager.getTasks().size();
                if (tasksSizeAfterAdd > tasksSizeBeforeAdd) {
                    writeResponse(exchange,
                            "Task named \""
                                    + taskFromGson.getName()
                                    + "\" be saved", 201);
                } else {
                    writeResponse(exchange,
                            "Task named \""
                                    + taskFromGson.getName()
                                    + "\" can't be saved, cause not valid", 200);
                }
                break;
            case SUB:
                String jsonSub = new String(exchange.getRequestBody().readAllBytes());
                try {
                    gson.fromJson(jsonSub, Sub.class);
                } catch (JsonSyntaxException e) {
                    writeResponse(exchange, "Invalid json format", 400);
                }
                Sub subFromGson = gson.fromJson(jsonSub, Sub.class);
                int subsSizeBeforeAdd = manager.getSubs().size();
                manager.save(subFromGson);
                int subsSizeAfterAdd = manager.getSubs().size();
                if (subsSizeAfterAdd > subsSizeBeforeAdd) {
                    writeResponse(exchange,
                            "Sub named \""
                                    + subFromGson.getName()
                                    + "\" be saved", 201);
                } else {
                    writeResponse(exchange,
                            "Sub named \""
                                    + subFromGson.getName()
                                    + "\" can't be saved, cause not valid", 200);
                }
                break;
            case EPIC:
                String jsonEpic = new String(exchange.getRequestBody().readAllBytes());
                try {
                    gson.fromJson(jsonEpic, Epic.class);
                } catch (JsonSyntaxException exception) {
                    writeResponse(exchange, "Invalid json format", 400);
                }
                Epic epicFromGson = gson.fromJson(jsonEpic, Epic.class);
                manager.save(epicFromGson);
                writeResponse(exchange,
                        "Epic named \""
                                + epicFromGson.getName()
                                + "\" be saved", 201);
                break;
            default:
                writeResponse(exchange, "invalid request", 405);
                break;
        }
    }

    private int parsePathId(String pathId) {
        try {
            return Integer.parseInt(pathId);
        } catch (NullPointerException exception) {
            return -1;
        }
    }

    private void writeResponse(HttpExchange exchange, String response, int code) throws IOException {
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

    private Endpoint getEndpoint(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        if (Pattern.matches("^/tasks/tasks$", path)) {
            return Endpoint.TASKS;
        } else if (Pattern.matches("^/tasks/subs$", path)) {
            return Endpoint.SUBS;
        } else if (Pattern.matches("^/tasks/epics$", path)) {
            return Endpoint.EPICS;
        } else if (Pattern.matches("^/tasks/task/", path)
                || Pattern.matches("^/tasks/task$", path)) {
            return Endpoint.TASK;
        } else if (Pattern.matches("^/tasks/sub/", path)
                || Pattern.matches("^/tasks/sub$", path)) {
            return Endpoint.SUB;
        } else if (Pattern.matches("^/tasks/epic/", path)
                || Pattern.matches("^/tasks/epic$", path)) {
            return Endpoint.EPIC;
        } else if (Pattern.matches("^/tasks/history$", path)) {
            return Endpoint.HISTORY;
        } else if (Pattern.matches("^/tasks/priority$", path)) {
            return Endpoint.PRIORITY;
        } else if (Pattern.matches("^/tasks/clear$", path)) {
            return Endpoint.CLEAR;
        } else {
            return Endpoint.UNKNOWN;
        }
    }

    enum Endpoint {
        TASKS, SUBS, EPICS, TASK, SUB, EPIC, HISTORY, PRIORITY, CLEAR, UNKNOWN
    }

}
