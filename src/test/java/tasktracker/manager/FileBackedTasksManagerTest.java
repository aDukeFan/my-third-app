package tasktracker.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.model.Epic;
import tasktracker.model.Status;
import tasktracker.model.SubTask;
import tasktracker.model.Task;

import java.io.File;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager>{
    @BeforeEach
    public void setManager() {
        File file = new File("test.csv");
        manager = new FileBackedTasksManager(file);
    }
    private final Task task = new Task(
            "Task",
            "for test",
            Status.NEW,
            LocalDateTime.of(2024, 1, 1, 1, 1),
            10);
    private final Epic epic = new Epic(
            "Task",
            "for test");
    private final SubTask sub = new SubTask(
            "Sub",
            "for test",
            Status.IN_PROGRESS,
            LocalDateTime.of(2024, 1, 2, 1, 1),
            10,
            1);

    @Test
    public void shouldCorrectSaveAndLoadToCSV () {
        manager.save(epic);
        manager.save(sub);
        manager.save(task);
        manager.getTaskById(3);
        manager.getSubTaskById(2);
        FileBackedTasksManager fileBackedTasksManager = FileBackedTasksManager.loadFromFile(new File("test.csv"));
        assertEquals(manager.getTasks().size(), fileBackedTasksManager.getTasks().size());
        assertEquals(manager.getHistory().size(), fileBackedTasksManager.getHistory().size());
    }
}
