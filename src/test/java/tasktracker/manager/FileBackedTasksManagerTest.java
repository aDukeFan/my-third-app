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
        manager = new FileBackedTasksManager("test.csv");
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
        if (!(manager.getSubs().isEmpty())) {
            for (Sub subIn : manager.getSubs()) {
                manager.getSubById(subIn.getId());
            }
        }
        if (!(manager.getEpics().isEmpty())) {
            for (Epic epicIn : manager.getEpics()) {
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
                .load(new File("test.csv"));
        int[] history = makeArrayOfIdsOutOfTaskList(manager.getHistory());
        int[] loadedHistory = makeArrayOfIdsOutOfTaskList(fileBackedTasksManager.getHistory());
        int[] priorityArray = makeArrayOfIdsOutOfTaskList(manager.getPrioritizedTasks());
        int[] loadedPriorityArray = makeArrayOfIdsOutOfTaskList(fileBackedTasksManager.getPrioritizedTasks());
        Epic loadedEpic = fileBackedTasksManager.getEpics().get(0);
        assertFalse(fileBackedTasksManager.getHistory().isEmpty());
        assertTrue(isSameTaskVariables(epic, loadedEpic));
        assertEquals(epic.getSubTaskIds(), loadedEpic.getSubTaskIds());
        assertArrayEquals(history, loadedHistory);
        assertArrayEquals(priorityArray, loadedPriorityArray);
        Task loadedTask = fileBackedTasksManager.getTasks().get(0);
        assertTrue(isSameTaskVariables(task, loadedTask));
        Sub loadedSub = fileBackedTasksManager.getSubs().get(0);
        assertTrue(isSameTaskVariables(sub, loadedSub));
        assertEquals(sub.getEpicId(), loadedSub.getEpicId());
    }

    @Test
    public void shouldCorrectSaveAndLoadToCSV2WithTaskAndEpicWithoutSubsAndNotEmptyHistory() {
        manager.save(epic);
        manager.save(task);
        makeHistory();
        FileBackedTasksManager fileBackedTasksManager = FileBackedTasksManager
                .load(new File("test.csv"));
        Epic loadedEpic = fileBackedTasksManager.getEpics().get(0);
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
        assertTrue(fileBackedTasksManager.getSubs().isEmpty());
        assertTrue(loadedEpic.getSubTaskIds().isEmpty());
    }

    @Test
    public void shouldCorrectSaveAndLoadToCSV3WithEpicAndSubButNoSimpleTasksAndEmptyHistory() {
        manager.save(epic);
        manager.save(sub);
        FileBackedTasksManager fileBackedTasksManager = FileBackedTasksManager
                .load(new File("test.csv"));
        Epic loadedEpic = fileBackedTasksManager.getEpics().get(0);
        int[] history = makeArrayOfIdsOutOfTaskList(manager.getHistory());
        int[] loadedHistory = makeArrayOfIdsOutOfTaskList(fileBackedTasksManager.getHistory());
        int[] priorityArray = makeArrayOfIdsOutOfTaskList(manager.getPrioritizedTasks());
        int[] loadedPriorityArray = makeArrayOfIdsOutOfTaskList(fileBackedTasksManager.getPrioritizedTasks());
        assertTrue(isSameTaskVariables(epic, loadedEpic));
        assertEquals(epic.getSubTaskIds(), loadedEpic.getSubTaskIds());
        assertArrayEquals(history, loadedHistory);
        assertArrayEquals(priorityArray, loadedPriorityArray);
        assertTrue(fileBackedTasksManager.getTasks().isEmpty());
        Sub loadedSub = fileBackedTasksManager.getSubs().get(0);
        assertTrue(isSameTaskVariables(sub, loadedSub));
        assertEquals(sub.getEpicId(), loadedSub.getEpicId());
    }
    @Test
    public void shouldSaveAndLoadTaskWithStartTimeNull() {
        Task savedTask = new Task("T", "without start time", Status.NEW, null, 10);
        manager.save(savedTask);
        FileBackedTasksManager fileBackedTasksManager = FileBackedTasksManager
                .load(new File("test.csv"));
        Task loadedTask = fileBackedTasksManager.getTaskById(1);
        assertTrue(isSameTaskVariables(savedTask, loadedTask));
    }

    @Test
    public void shouldSaveAndLoadSubsWithStartTimeNullAndCorrectCalculateDurationAndTimePointsOfEpic() {
        manager.save(epic);
        manager.save(sub);
        manager.save(new Sub("sub", "without start time", Status.NEW, null, 10, 1));
        manager.save(new Task("sub", "without start time", Status.NEW, null, 10));
        FileBackedTasksManager fileBackedTasksManager = FileBackedTasksManager
                .load(new File("test.csv"));
        Epic loadedEpic = fileBackedTasksManager.getEpicById(1);
        for (Task value : fileBackedTasksManager.tasks.values()) {
            assertNull(value.getStartTime());
        }
        assertEquals(20, loadedEpic.getDuration());
        assertEquals(loadedEpic.getStartTime(), sub.getStartTime());
        assertEquals(loadedEpic.getEndTime(), sub.getEndTime());

    }
}
