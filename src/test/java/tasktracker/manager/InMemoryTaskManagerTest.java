package tasktracker.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.model.Status;
import tasktracker.model.Task;

import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    public void setManager() {
        manager = new InMemoryTaskManager();
    }

    @Test
    public void isValid_shouldSaveOnlyValidTimeTasks() {
        Task validTaskOne = new Task(
                "Task",
                "for test",
                Status.NEW,
                LocalDateTime.of(2024, 2, 10, 10, 0),
                30);
        Task validTaskTwo = new Task(
                "Task",
                "for test",
                Status.NEW,
                LocalDateTime.of(2024, 2, 10, 10, 30),
                30);
        manager.save(validTaskOne);
        manager.save(validTaskTwo);
        assertEquals(2, manager.getTasks().size());
        Task notValidTask = new Task(
                "Task",
                "for test",
                Status.NEW,
                LocalDateTime.of(2024, 2, 10, 10, 30),
                30);
        manager.save(notValidTask);
        assertNull(manager.getTaskById(3));
        assertNotEquals(3, manager.getTasks().size());
    }
}
