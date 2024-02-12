package tasktracker.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.model.Epic;
import tasktracker.model.Status;
import tasktracker.model.Sub;
import tasktracker.model.Task;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {
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
    private final Sub sub = new Sub(
            "Sub",
            "for test",
            Status.IN_PROGRESS,
            LocalDateTime.of(2024, 1, 2, 1, 1),
            10,
            1);

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

    private int[] makeArrayOfIdsOutOfTaskList(List<Task> list) {
        return list.stream()
                .map(Task::getId).mapToInt(Integer::intValue).toArray();
    }

    private void makeHistory() {
        if (!(manager.getTasks().isEmpty())) {
            for (Task taskIn : manager.getTasks()) {
                manager.getTaskById(taskIn.getId());
            }
        }
        if (!(manager.getSubTasks().isEmpty())) {
            for (Sub subIn : manager.getSubTasks()) {
                manager.getSubTaskById(subIn.getId());
            }
        }
        if (!(manager.getEpicTasks().isEmpty())) {
            for (Epic epicIn : manager.getEpicTasks()) {
                manager.getEpicById(epicIn.getId());
            }
        }
    }

    @Test
    public void shouldCorrectSaveAndLoadToCSVWithEpicEpicSubSimpleTaskAndNotEmptyHistoryList() {
        manager.save(epic);
        manager.save(sub);
        manager.save(task);
        makeHistory();
        FileBackedTasksManager fileBackedTasksManager = FileBackedTasksManager
                .loadFromFile(new File("test.csv"));
        int[] history = makeArrayOfIdsOutOfTaskList(manager.getHistory());
        int[] loadedHistory = makeArrayOfIdsOutOfTaskList(fileBackedTasksManager.getHistory());
        int[] priorityArray = makeArrayOfIdsOutOfTaskList(manager.getPrioritizedTasks());
        int[] loadedPriorityArray = makeArrayOfIdsOutOfTaskList(fileBackedTasksManager.getPrioritizedTasks());
        Epic loadedEpic = fileBackedTasksManager.getEpicTasks().get(0);
        assertFalse(fileBackedTasksManager.getHistory().isEmpty());
        assertTrue(isSameTaskVariables(epic, loadedEpic));
        assertEquals(epic.getSubTaskIds(), loadedEpic.getSubTaskIds());
        assertArrayEquals(history, loadedHistory);
        assertArrayEquals(priorityArray, loadedPriorityArray);
        Task loadedTask = fileBackedTasksManager.getTasks().get(0);
        assertTrue(isSameTaskVariables(task, loadedTask));
        Sub loadedSub = fileBackedTasksManager.getSubTasks().get(0);
        assertTrue(isSameTaskVariables(sub, loadedSub));
        assertEquals(sub.getEpicId(), loadedSub.getEpicId());
    }

    @Test
    public void shouldCorrectSaveAndLoadToCSV2WithTaskAndEpicWithoutSubsAndNotEmptyHistory() {
        manager.save(epic);
        manager.save(task);
        makeHistory();
        FileBackedTasksManager fileBackedTasksManager = FileBackedTasksManager
                .loadFromFile(new File("test.csv"));
        Epic loadedEpic = fileBackedTasksManager.getEpicTasks().get(0);
        int[] history = makeArrayOfIdsOutOfTaskList(manager.getHistory());
        int[] loadedHistory = makeArrayOfIdsOutOfTaskList(fileBackedTasksManager.getHistory());
        int[] priorityArray = makeArrayOfIdsOutOfTaskList(manager.getPrioritizedTasks());
        int[] loadedPriorityArray = makeArrayOfIdsOutOfTaskList(fileBackedTasksManager.getPrioritizedTasks());
        assertTrue(isSameTaskVariables(epic, loadedEpic));
        assertEquals(epic.getSubTaskIds(), loadedEpic.getSubTaskIds());
        assertArrayEquals(history, loadedHistory);
        assertArrayEquals(priorityArray, loadedPriorityArray);
        Task loadedTask = fileBackedTasksManager.getTasks().get(0);
        assertTrue(isSameTaskVariables(task, loadedTask));
        assertTrue(fileBackedTasksManager.getSubTasks().isEmpty());
        assertTrue(loadedEpic.getSubTaskIds().isEmpty());
    }

    @Test
    public void shouldCorrectSaveAndLoadToCSV3WithEpicAndSubButNoSimpleTasksAndEmptyHistory() {
        manager.save(epic);
        manager.save(sub);
        FileBackedTasksManager fileBackedTasksManager = FileBackedTasksManager
                .loadFromFile(new File("test.csv"));
        Epic loadedEpic = fileBackedTasksManager.getEpicTasks().get(0);
        int[] history = makeArrayOfIdsOutOfTaskList(manager.getHistory());
        int[] loadedHistory = makeArrayOfIdsOutOfTaskList(fileBackedTasksManager.getHistory());
        int[] priorityArray = makeArrayOfIdsOutOfTaskList(manager.getPrioritizedTasks());
        int[] loadedPriorityArray = makeArrayOfIdsOutOfTaskList(fileBackedTasksManager.getPrioritizedTasks());
        assertTrue(isSameTaskVariables(epic, loadedEpic));
        assertEquals(epic.getSubTaskIds(), loadedEpic.getSubTaskIds());
        assertArrayEquals(history, loadedHistory);
        assertArrayEquals(priorityArray, loadedPriorityArray);
        assertTrue(fileBackedTasksManager.getTasks().isEmpty());
        Sub loadedSub = fileBackedTasksManager.getSubTasks().get(0);
        assertTrue(isSameTaskVariables(sub, loadedSub));
        assertEquals(sub.getEpicId(), loadedSub.getEpicId());
    }
}
