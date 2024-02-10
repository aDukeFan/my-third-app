package tasktracker.manager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tasktracker.model.Epic;
import tasktracker.model.Status;
import tasktracker.model.SubTask;
import tasktracker.model.Task;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    public T manager;

    //Для подзадач нужно дополнительно проверить наличие эпика, а для эпика — расчёт статуса.
    //Для каждого метода нужно проверить его работу:
    //  a. Со стандартным поведением.
    //  b. С пустым списком задач.
    //  c. С неверным идентификатором задачи (пустой и/или несуществующий идентификатор).

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

    @DisplayName("GIVEN a new Task, "
            + "WHEN a new task be set id, tasks put task with ID, "
            + "priority add task"
            + "THEN manager and priority get same task")
    @Test
    void save_Task_shouldSaveTaskWithRightId() {
        manager.save(task);
        Task savedTask = manager.getTasks().get(0);
        assertEquals(1, savedTask.getId());
        assertEquals(task, savedTask);
        assertEquals(task, manager.getPrioritizedTasks().get(0));
    }

    @DisplayName("GIVEN a new Epic without startTime and duration "
            + "WHEN an epic be set id, epicTasks put task with ID, "
            + "THEN manager get same epic")
    @Test
    void save_Epic_shouldSaveEpicWithoutSubsWithRightIdStatusTimeValues() {
        manager.save(epic);
        Epic savedTask = manager.getEpicTasks().get(0);
        assertEquals(1, savedTask.getId());
        assertEquals(epic, savedTask);
        assertEquals(Status.NEW, savedTask.getStatus());
        assertNull(savedTask.getStartTime());
        assertEquals(0, savedTask.getDuration());
        assertNull(savedTask.getEndTime());
    }

    @DisplayName("GIVEN a new Subtask with startTime, duration, epicId "
            + "WHEN a sub be set id, subtasks put sub with ID, epicTasks put task with ID, "
            + "THEN manager get same Subtask, epicTasks get epic with as time as sub's time")
    @Test
    public void save_Sub_shouldSaveSubtaskInRightWay() {
        manager.save(epic);
        manager.save(sub);
        Epic changedEpic = manager.getEpicById(1);
        SubTask savedSub = manager.getSubTaskById(2);
        assertEquals(2, savedSub.getId());
        assertEquals(Status.IN_PROGRESS, savedSub.getStatus());
        assertEquals(Status.IN_PROGRESS, savedSub.getStatus());
        assertEquals(savedSub.getStatus(), changedEpic.getStatus());
        assertEquals(savedSub.getStartTime(), changedEpic.getStartTime());
        assertEquals(savedSub.getEndTime(), changedEpic.getEndTime());
        assertEquals(savedSub.getDuration(), changedEpic.getDuration());

    }

    @DisplayName("GIVEN changedTask with id, which has been set for another savedTask "
            + "WHEN tasks put changedTask, saved task be removed from tasks "
            + "THEN manager get changedTask, which is different from savedTask in changed parameter")
    @Test
    public void update_task_shouldRePutTaskWithSameId() {
        manager.save(task);
        Status oldStatus = manager.getTaskById(1).getStatus();
        task.setStatus(Status.IN_PROGRESS);
        manager.update(task);
        assertNotEquals(oldStatus, manager.getTaskById(1).getStatus());
        assertEquals(1, manager.getTasks().get(0).getId());
    }

    @DisplayName("GIVEN changedEpic with id, which has been set for another savedEpic "
            + "WHEN epicTasks put changedEpic, savedEpic be removed from epicTasks "
            + "THEN manager get changedEpic, which is different from savedEpic in changed parameter")
    @Test
    public void update_epic_shouldRePutEpicWithSameId() {
        manager.save(epic);
        long oldDuration = manager.getEpicById(1).getDuration();
        epic.setDuration(50);
        manager.update(epic);
        assertNotEquals(50, oldDuration);
        assertEquals(1, manager.getEpicTasks().get(0).getId());
    }

    @DisplayName("GIVEN changedSub with id, which has been set for another savedSub "
            + "WHEN subTasks put changedSub, savedSub be removed from subTasks "
            + "THEN manager get changedSub, which is different from savedSub in changed parameter")
    @Test
    public void update_sub_shouldRePutSubWithSameId() {
        manager.save(epic);
        manager.save(sub);
        Status oldStatus = manager.getSubTaskById(2).getStatus();
        sub.setStatus(Status.DONE);
        manager.update(sub);
        assertNotEquals(oldStatus, manager.getSubTaskById(2).getStatus());
        assertEquals(2, manager.getSubTasks().get(0).getId());
    }

    @DisplayName("GIVEN try to use methods getTask or getSubs or getEpics "
            + "WHEN tasks, subTasks or epics are empty or not "
            + "THEN getTask or getSubs or getEpics must return list with right size")
    @Test
    public void getTasksSubsOrEpics_shouldReturnRightTasksOrSubsOrEpicsList() {
        assertTrue(manager.getTasks().isEmpty());
        assertTrue(manager.getSubTasks().isEmpty());
        assertTrue(manager.getEpicTasks().isEmpty());
        manager.save(epic);
        assertEquals(1, manager.getEpicTasks().size());
        manager.save(sub);
        assertEquals(1, manager.getSubTasks().size());
        manager.save(task);
        assertEquals(1, manager.getTasks().size());
    }

    @DisplayName("GIVEN not empty: tasks, subTasks, epics "
            + "WHEN use clear methods "
            + "THEN maps with same task must be empty")
    @Test
    public void clearAll_shouldClearMaps() {
        manager.save(epic);
        manager.save(sub);
        manager.save(task);
        manager.clearAllTasks();
        assertTrue(manager.getTasks().isEmpty());
        manager.clearAllSubTasks();
        assertTrue(manager.getSubTasks().isEmpty());
        manager.clearAllEpics();
        assertTrue(manager.getEpicTasks().isEmpty());
    }

    @DisplayName("if epics clear it's subs must be clear too")
    @Test
    public void clearAllEpics_mustClearEpicsSubs() {
        manager.save(epic);
        manager.save(sub);
        manager.clearAllEpics();
        assertTrue(manager.getSubTasks().isEmpty());
    }

    @Test
    public void getTaskOrSubOrEpicById_shouldReturnRightTaskOrNull() {
        assertNull(manager.getTaskById(1));
        assertNull(manager.getSubTaskById(1));
        assertNull(manager.getEpicById(1));
        manager.save(epic);
        manager.save(sub);
        manager.save(task);
        assertEquals(task, manager.getTaskById(3));
        assertEquals(sub, manager.getSubTaskById(2));
        assertEquals(epic, manager.getEpicById(1));
    }
    @Test
    public void shouldDelTaskById() {
        manager.save(task);
        manager.delTaskById(1);
        assertNull(manager.getTaskById(1));
    }

    @Test
    public void shouldDelSubTaskById() {
        manager.save(epic);
        manager.save(sub);
        manager.delSubTaskById(2);
        assertNull(manager.getSubTaskById(2));
    }

    @Test
    public void shouldDelEpicById_mustDelSubsOfEpicToo() {
        manager.save(epic);
        manager.save(sub);
        manager.delEpicById(1);
        assertNull(manager.getEpicById(1));
        assertNull(manager.getSubTaskById(2));
    }

    @Test
    public void shouldReturnListOfHistoryWithRightSize() {
        manager.save(epic);
        manager.save(sub);
        manager.save(task);
        manager.getTaskById(3);
        assertEquals(1, manager.getHistory().size());
    }

    @Test
    public void getPrioritizedTasks_shouldReturnRightPriorityList() {
        assertTrue(manager.getPrioritizedTasks().isEmpty());
        manager.save(epic);
        manager.save(sub);
        manager.save(task);
        List<Task> priorityTaskForTest = List.of(task, sub);
        List<Task> priorityTaskFromManager = manager.getPrioritizedTasks();
        assertEquals(priorityTaskForTest, priorityTaskFromManager);
    }
}
