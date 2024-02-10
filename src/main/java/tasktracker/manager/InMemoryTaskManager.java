package tasktracker.manager;

/* Как раз при нажатии Ctrl + Alt + O у меня IDEA
импорты меняет на java.util.*, вручную поправил: */
import tasktracker.model.Epic;
import tasktracker.model.Status;
import tasktracker.model.SubTask;
import tasktracker.model.Task;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, SubTask> subTasks = new HashMap<>();
    protected final Map<Integer, Epic> epicTasks = new HashMap<>();

    protected final Set<Task> priority = new TreeSet<>(new CompareTasks());

    public final HistoryManager historyManager = Managers.getDefaultHistory();

    protected int nextId = 1;


    private int generateId() {
        return nextId++;
    }

    @Override
    public void save(Task task) {
        if (isValid(task)) {
            int newId = generateId();
            task.setId(newId);
            tasks.put(newId, task);
            priority.add(task);
        }
    }

    @Override
    public void save(Epic epic) {
        int newId = generateId();
        epic.setId(newId);
        epicTasks.put(newId, epic);
    }

    @Override
    public void save(SubTask subTask) {
        if (isValid(subTask)) {
            int newtId = generateId();
            subTask.setId(newtId);
            subTasks.put(newtId, subTask);
            Epic epic = epicTasks.get(subTask.getEpicId());
            epic.addToSubTaskIds(newtId);
            setEpicTime(epic);
            setEpicStatus(epic);
            priority.add(subTask);
        }
    }

    @Override
    public void update(Task task) {
        priority.removeIf(taskIn -> (taskIn.getId() == task.getId()));
        if (isValid(task)) {
            tasks.put(task.getId(), task);
            priority.add(task);
        } else {
            priority.add(tasks.get(task.getId()));
        }
    }

    @Override
    public void update(Epic epic) {
        epicTasks.put(epic.getId(), epic);
    }

    @Override
    public void update(SubTask subTask) {
        priority.removeIf(taskIn -> (taskIn.getId() == subTask.getId()));
        if (isValid(subTask)) {
            subTasks.put(subTask.getId(), subTask);
            priority.add(subTask);
            setEpicStatus(epicTasks.get(subTask.getEpicId()));
            setEpicTime(epicTasks.get(subTask.getEpicId()));
        } else {
            priority.add(subTasks.get(subTask.getId()));
        }
    }

    // Получение списка всех задач
    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public List<Epic> getEpicTasks() {
        return new ArrayList<>(epicTasks.values());
    }

    @Override
    public void clearAllTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
            priority.removeIf(taskIn -> (taskIn.getId() == id));
        }
        tasks.clear();

    }

    @Override
    public void clearAllSubTasks() {
        for (Integer id : subTasks.keySet()) {
            historyManager.remove(id);
            priority.removeIf(taskIn -> (taskIn.getId() == id));
        }
        subTasks.clear();
        epicTasks.forEach((key, value) -> value.getSubTaskIds().clear());
        epicTasks.forEach((key, value) -> setEpicStatus(value));
        epicTasks.forEach((key, value) -> value.setDuration(0));
    }

    @Override
    public void clearAllEpics() {
        for (Integer id : subTasks.keySet()) {
            historyManager.remove(id);
            priority.removeIf(taskIn -> (taskIn.getId() == id));
        }
        for (Integer id : epicTasks.keySet()) {
            historyManager.remove(id);
        }
        subTasks.clear();
        epicTasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public SubTask getSubTaskById(int id) {
        historyManager.add(subTasks.get(id));
        return subTasks.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        historyManager.add(epicTasks.get(id));
        return epicTasks.get(id);
    }

    @Override
    public void delTaskById(int id) {
        historyManager.remove(id);
        priority.removeIf(taskIn -> (taskIn.getId() == id));
        tasks.remove(id);
    }

    @Override
    public void delSubTaskById(int id) {
        Epic epic = getEpicById(subTasks.get(id).getEpicId());
        epic.getSubTaskIds().removeIf(value -> (value == id));
        priority.removeIf(taskIn -> (taskIn.getId() == id));
        setEpicStatus(epic);
        setEpicTime(epic);
        historyManager.remove(id);
        subTasks.remove(id);
    }

    @Override
    public void delEpicById(int id) {
        List<Integer> subTaskIds = epicTasks.get(id).getSubTaskIds();
        for (Integer subTaskId : subTaskIds) {
            historyManager.remove(subTaskId);
            subTasks.remove(subTaskId);
            priority.removeIf(taskIn -> (taskIn.getId() == subTaskId));
        }
        historyManager.remove(id);
        epicTasks.remove(id);
    }


    private void setEpicStatus(Epic epic) {
        List<Integer> epicsSubTaskIds = epic.getSubTaskIds();
        if (epicsSubTaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
        } else {
            HashSet<Status> subStatuses = new HashSet<>();
            for (Integer subTaskId : epic.getSubTaskIds()) {
                subStatuses.add(subTasks.get(subTaskId).getStatus());
            }
            if (subStatuses.size() > 1) {
                epic.setStatus(Status.IN_PROGRESS);
            } else {
                subStatuses.forEach(epic::setStatus);
            }
        }
    }

    private void setEpicTime(Epic epic) {
        Set<LocalDateTime> timePointsTreeSet = new TreeSet<>();
        long duration = 0;
        for (Integer subTaskId : epic.getSubTaskIds()) {
            timePointsTreeSet.add(subTasks.get(subTaskId).getStartTime());
            timePointsTreeSet.add(subTasks.get(subTaskId).getEndTime());
            duration += subTasks.get(subTaskId).getDuration();
        }
        List<LocalDateTime> timePointsList = List.copyOf(timePointsTreeSet);
        if (timePointsList.isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(0);
        } else {
            epic.setStartTime(timePointsList.get(0));
            epic.setEndTime(timePointsList.get(timePointsList.size()-1));
            epic.setDuration(duration);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return List.copyOf(priority);
    }

    private boolean isValid(Task task) {
        boolean isPossible = true;
        LocalDateTime expectStart = task.getStartTime();
        LocalDateTime expectEnd = task.getEndTime();
        for (Task existTask : priority) {
            LocalDateTime startOfBusyTime = existTask.getStartTime();
            LocalDateTime endOfBusyTime = existTask.getEndTime();
            boolean isPossibleBefore = expectStart.isBefore(startOfBusyTime) &&
                    (expectEnd.isBefore(startOfBusyTime) || expectEnd.isEqual(startOfBusyTime));
            boolean isPossibleAfter = expectStart.isAfter(endOfBusyTime) || expectStart.isEqual(endOfBusyTime);
            isPossible = isPossible && (isPossibleBefore || isPossibleAfter);
        }
        if (!(isPossible)) System.err.println(
                "WARNING! Time is busy: task named \"" + task.getName() + "\" hasn't be saved.");
        return isPossible;
    }
}
