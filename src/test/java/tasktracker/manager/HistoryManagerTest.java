package tasktracker.manager;

import org.junit.jupiter.api.BeforeEach;
import tasktracker.model.Status;
import tasktracker.model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class HistoryManagerTest {
    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void createdEpicInManagers() {
        historyManager = new InMemoryHistoryManager();
    }

    private Task makeTestTask(int id) {
        Task task = new Task(
                "Task",
                "for test",
                Status.NEW,
                LocalDateTime.of(2024, 2, 9, 16, 30),
                20);
        task.setId(id);
        return task;
    }

    @Test
    void shouldAddOnlyUniqueTasks() {
        historyManager.add(makeTestTask(1));
        historyManager.add(makeTestTask(1));
        historyManager.add(makeTestTask(2));
        Assertions.assertEquals(2, historyManager.getHistory().size());
    }

    @Test
    void getHistoryShouldReturnEmptyHistoryList() {
        Assertions.assertTrue(historyManager.getHistory().isEmpty());
    }
    @Test
    void shouldRemoveFirstTask() {
        historyManager.add(makeTestTask(1));
        Task taskToRemove = historyManager.getHistory().get(0);
        historyManager.add(makeTestTask(2));
        historyManager.add(makeTestTask(3));
        historyManager.remove(1);
        Assertions.assertFalse(historyManager.getHistory().contains(taskToRemove));
    }

    @Test
    void shouldRemoveMiddleTask() {
        historyManager.add(makeTestTask(1));
        historyManager.add(makeTestTask(2));
        Task taskToRemove = historyManager.getHistory().get(1);
        historyManager.add(makeTestTask(3));
        historyManager.remove(2);
        Assertions.assertFalse(historyManager.getHistory().contains(taskToRemove));
    }

    @Test
    void shouldRemoveLastTask() {
        historyManager.add(makeTestTask(1));
        historyManager.add(makeTestTask(2));
        historyManager.add(makeTestTask(3));
        Task taskToRemove = historyManager.getHistory().get(2);
        historyManager.remove(3);
        Assertions.assertFalse(historyManager.getHistory().contains(taskToRemove));
    }

}
