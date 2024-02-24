package tasktracker.manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.api.KVServer;
import tasktracker.model.Epic;
import tasktracker.model.Status;
import tasktracker.model.Sub;
import tasktracker.model.Task;

import java.io.IOException;
import java.time.LocalDateTime;
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
        this.manager = Managers.getDefault();
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


    @Test
    void loads() {
        manager.save(firstTask);
        manager.save(secondTask);
        manager.save(epic);
        manager.save(sub);

        manager.getTaskById(1);
        manager.getSubById(4);

        manager.saveToServer();

        HttpTaskManager loadManager = HttpTaskManager.loadFromServer("http://localhost:8078/register");
        assertIterableEquals(
                manager.getHistory().stream().map(Task::getId).collect(Collectors.toList()),
                loadManager.getHistory().stream().map(Task::getId).collect(Collectors.toList()));
        assertIterableEquals(
                manager.getPrioritizedTasks().stream().map(Task::getId).collect(Collectors.toList()),
                loadManager.getPrioritizedTasks().stream().map(Task::getId).collect(Collectors.toList()));

        isSameTaskVariables(firstTask, loadManager.getTaskById(1));
        isSameTaskVariables(secondTask, loadManager.getTaskById(2));
        isSameTaskVariables(sub, loadManager.getSubById(4));
        isSameTaskVariables(epic, loadManager.getEpicById(3));
        Task taskFive = new Task("TaskFive", "test ID", Status.IN_PROGRESS, LocalDateTime.of(2025, 1, 1, 1, 1),
                10);
        manager.save(taskFive);
        assertEquals(manager.getTaskById(5).getName(), taskFive.getName());

    }
}