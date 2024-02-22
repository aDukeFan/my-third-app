package tasktracker.manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.Internet.KVServer;
import tasktracker.model.Epic;
import tasktracker.model.Status;
import tasktracker.model.Sub;
import tasktracker.model.Task;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerTest {

    public KVServer server;

    public HttpTaskManager manager;

    @BeforeEach
    public void setServerAndManager() throws IOException {
        this.server = new KVServer();
        server.start();
        this.manager = Managers.getDefaultHttpTaskManager();
    }
    @AfterEach
    public void serverStop() {
        server.stop();

    }

    private final Task firstTask = new Task(
            "Task",
            "for test",
            Status.NEW,
            LocalDateTime.of(2024, 1, 1, 1, 1),
            10);
    private final Task secondTask = new Task(
            "TaskTwo",
            "for test",
            Status.NEW,
            LocalDateTime.of(2022, 1, 1, 1, 1),
            10);
    private final Epic epic = new Epic(
            "Task",
            "for test");
    private final Sub sub = new Sub(
            "Sub",
            "for test",
            Status.IN_PROGRESS,
            LocalDateTime.of(2024, 1, 2, 1, 1),
            10,
            3);

    private boolean isSameTaskVariables(Task o1, Task o2) {
        boolean id = o1.getId() == o2.getId();
        boolean type = o1.getType().equals(o2.getType());
        boolean name = Objects.equals(o1.getName(), o2.getName());
        boolean status = o1.getStatus().equals(o2.getStatus());
        boolean description = Objects.equals(o1.getDescription(), o2.getDescription());
        boolean startTime = Optional.ofNullable(o1.getStartTime()).equals(Optional.ofNullable(o2.getStartTime()));
        boolean duration = o1.getDuration() == o2.getDuration();
        boolean endTime = Optional.ofNullable(o1.getEndTime()).equals(Optional.ofNullable(o2.getEndTime()));
        return id && type && name && status && description && startTime && duration && endTime;
    }


    @Test
    void loads() throws IOException {
        manager.save(firstTask);
        manager.save(secondTask);
        manager.save(epic);
        manager.save(sub);

        manager.getTaskById(1);
        manager.getSubById(4);

        manager.saveToServer();

        HttpTaskManager loadManager = HttpTaskManager.loads("http://localhost:8078/register");
        assertIterableEquals(
                manager.getHistory().stream().map(Task::getId).collect(Collectors.toList()),
                loadManager.getHistory().stream().map(Task::getId).collect(Collectors.toList()));
        assertIterableEquals(
                manager.getPrioritizedTasks().stream().map(Task::getId).collect(Collectors.toList()),
                loadManager.getPrioritizedTasks().stream().map(Task::getId).collect(Collectors.toList()));

        assertTrue(isSameTaskVariables(firstTask, loadManager.getTaskById(1)));
        assertTrue(isSameTaskVariables(secondTask, loadManager.getTaskById(2)));
        assertTrue(isSameTaskVariables(sub, loadManager.getSubById(4)));
        assertTrue(isSameTaskVariables(epic, loadManager.getEpicById(3)));
        Task taskFive = new Task("TaskFive", "test ID", Status.IN_PROGRESS, LocalDateTime.of(2025, 1, 1, 1, 1),
                10);
        manager.save(taskFive);
        assertEquals(manager.getTaskById(5).getName(), taskFive.getName());

    }
}