package tasktracker.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.manager.FileBackedTasksManager;
import tasktracker.model.Epic;
import tasktracker.model.Status;
import tasktracker.model.Sub;
import tasktracker.model.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private HttpTaskServer server;
    private FileBackedTasksManager manager;

    private static final String LOCAL_HOST_TASKS = "http://localhost:8080/tasks/";

    @BeforeEach
    public void makeTasks() {
        Task firstTask = new Task("First Task",
                "test",
                Status.NEW,
                LocalDateTime.of(2020, 10, 10, 10, 10), 10);
        manager.save(firstTask);
        Task secondTask = new Task("Second Task",
                "test",
                Status.NEW,
                LocalDateTime.of(2019, 10, 10, 10, 10), 10);
        manager.save(secondTask);
        Epic epic = new Epic("Epic",
                "test");
        manager.save(epic);
        Sub sub = new Sub("Sub",
                "test",
                Status.NEW,
                LocalDateTime.of(2023, 10, 10, 10, 10), 10, 3);
        manager.save(sub);
        manager.getTaskById(2);
        manager.getSubById(4);
    }

    @BeforeEach
    public void setServer() throws IOException {
        this.server = new HttpTaskServer();
        this.manager = server.getManager();
        server.start();
    }

    @AfterEach
    public void clearAll() {
        manager.clearAllTasks();
        manager.clearAllEpics();
        server.stop();
    }

    private void isSameTaskVariables(Task o1, Task o2) {
        assertEquals(o1.getId(), o2.getId());
        assertEquals(o1.getType(), o2.getType());
        assertEquals(o1.getName(), o2.getName());
        assertEquals(o1.getStatus(), o2.getStatus());
        assertEquals(o1.getDescription(), o2.getDescription());
        assertEquals(Optional.ofNullable(o1.getStartTime()), Optional.ofNullable(o2.getStartTime()));
        assertEquals(o1.getDuration(), o2.getDuration());
        assertEquals(Optional.ofNullable(o1.getEndTime()), Optional.ofNullable(o2.getEndTime()));
    }

    private URI makeUri(String handle) {
        return URI.create(LOCAL_HOST_TASKS + handle);
    }

    @Test
    public void shouldGetTasks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(makeUri("tasks")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonArray jsonTasksArray = JsonParser.parseString(response.body()).getAsJsonArray();
        List<Task> tasksFromClient = new ArrayList<>();
        for (JsonElement element : jsonTasksArray) {
            tasksFromClient.add(gson.fromJson(element, Task.class));
        }
        for (Task task : tasksFromClient) {
            isSameTaskVariables(task, manager.getTaskById(task.getId()));
        }
    }

    @Test
    public void shouldGetSubs() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(makeUri("subs")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonArray jsonTasksArray = JsonParser.parseString(response.body()).getAsJsonArray();
        List<Sub> subsFromClient = new ArrayList<>();
        for (JsonElement element : jsonTasksArray) {
            subsFromClient.add(gson.fromJson(element, Sub.class));
        }
        for (Sub task : subsFromClient) {
            isSameTaskVariables(task, manager.getSubById(task.getId()));
        }
    }

    @Test
    public void shouldGetEpics() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(makeUri("epics")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonArray jsonTasksArray = JsonParser.parseString(response.body()).getAsJsonArray();
        List<Epic> list = new ArrayList<>();
        for (JsonElement element : jsonTasksArray) {
            list.add(gson.fromJson(element, Epic.class));
        }
        for (Epic task : list) {
            isSameTaskVariables(task, manager.getEpicById(task.getId()));
        }
    }

    @Test
    public void shouldGetPriority() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(makeUri("priority")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(JsonParser.parseString(response.body()).getAsJsonArray().size(),
                manager.getPrioritizedTasks().size());

    }

    @Test
    public void shouldGetTaskById() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(makeUri("task/?id=2")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Task taskFromJson = gson.fromJson(JsonParser.parseString(response.body()), Task.class);
        isSameTaskVariables(taskFromJson, manager.getTaskById(2));
    }

    @Test
    public void shouldGetSubById() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(makeUri("sub/?id=4")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Sub taskFromJson = gson.fromJson(JsonParser.parseString(response.body()), Sub.class);
        isSameTaskVariables(taskFromJson, manager.getSubById(4));
    }

    @Test
    public void shouldGetEpicById() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(makeUri("epic/?id=3")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Epic taskFromJson = gson.fromJson(JsonParser.parseString(response.body()), Epic.class);
        isSameTaskVariables(taskFromJson, manager.getEpicById(3));
    }

    @Test
    public void shouldGetHistory() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(makeUri("history")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        int jsonTasksArray = JsonParser.parseString(response.body()).getAsJsonArray().size();
        assertEquals(jsonTasksArray, manager.getHistory().size());
    }

    @Test
    public void shouldDeleteAllTasks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().DELETE().uri(makeUri("tasks")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    public void shouldDeleteAllSubs() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().DELETE().uri(makeUri("subs")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getSubs().isEmpty());
    }

    @Test
    public void shouldDeleteAllEpics() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().DELETE().uri(makeUri("epics")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getEpics().isEmpty());
        assertTrue(manager.getSubs().isEmpty());
    }

    @Test
    public void shouldDeleteTaskById() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().DELETE().uri(makeUri("task/?id=2")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertNull(manager.getTaskById(2));
    }

    @Test
    public void shouldDeleteSubById() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().DELETE().uri(makeUri("sub/?id=4")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertNull(manager.getSubById(4));
    }

    @Test
    public void shouldDeleteEpicById() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().DELETE().uri(makeUri("epic/?id=3")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertNull(manager.getEpicById(3));
        assertNull(manager.getSubById(4));
    }

    @Test
    public void shouldMakeTaskByJsonFromClient() throws IOException, InterruptedException {
        manager.clearAllTasks();
        Task firstTask = new Task("First Task",
                "test",
                Status.NEW,
                LocalDateTime.of(2020, 10, 10, 10, 10), 10);
        firstTask.setId(5);
        String json = gson.toJson(firstTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().POST(body).uri(makeUri("task/make")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        isSameTaskVariables(firstTask, manager.getTaskById(5));
    }

    @Test
    public void shouldMakeSubByJsonFromClient() throws IOException, InterruptedException {
        Sub secondSub = new Sub("Sub",
                "test",
                Status.NEW,
                LocalDateTime.of(2028, 10, 10, 10, 10), 10, 3);
        secondSub.setId(5);
        String json = gson.toJson(secondSub);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().POST(body).uri(makeUri("sub/make")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        isSameTaskVariables(secondSub, manager.getSubById(5));
    }

    @Test
    public void shouldMakeEpicByJsonFromClient() throws IOException, InterruptedException {
        Epic secondEpic = new Epic("EpicTwo",
                "test");
        secondEpic.setId(5);
        String json = gson.toJson(secondEpic);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().POST(body).uri(makeUri("epic/make")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        isSameTaskVariables(secondEpic, manager.getEpicById(5));
    }

}


