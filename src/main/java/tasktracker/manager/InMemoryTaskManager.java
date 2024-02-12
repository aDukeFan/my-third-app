package tasktracker.manager;

import tasktracker.model.Epic;
import tasktracker.model.Status;
import tasktracker.model.Sub;
import tasktracker.model.Task;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Sub> subTasks = new HashMap<>();
    protected final Map<Integer, Epic> epicTasks = new HashMap<>();

    protected final Set<Task> priority = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    protected final HistoryManager historyManager = Managers.getDefaultHistory();

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
    public void save(Sub sub) {
        if (isValid(sub)) {
            int newtId = generateId();
            sub.setId(newtId);
            subTasks.put(newtId, sub);
            Epic epic = epicTasks.get(sub.getEpicId());
            epic.addToSubTaskIds(newtId);
            setEpicTime(epic);
            setEpicStatus(epic);
            priority.add(sub);
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
        Epic savedEpic = epicTasks.get(epic.getId());
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
        epicTasks.put(savedEpic.getId(), savedEpic);
    }

    @Override
    public void update(Sub sub) {
        priority.removeIf(taskIn -> (taskIn.getId() == sub.getId()));
        if (isValid(sub)) {
            subTasks.put(sub.getId(), sub);
            priority.add(sub);
            setEpicStatus(epicTasks.get(sub.getEpicId()));
            setEpicTime(epicTasks.get(sub.getEpicId()));
        } else {
            priority.add(subTasks.get(sub.getId()));
        }
    }

    // Получение списка всех задач
    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Sub> getSubTasks() {
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
            priority.remove(tasks.get(id));
        }
        tasks.clear();

    }

    @Override
    public void clearAllSubTasks() {
        for (Integer id : subTasks.keySet()) {
            historyManager.remove(id);
            priority.remove(subTasks.get(id));
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
            priority.remove(subTasks.get(id));
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
    public Sub getSubTaskById(int id) {
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
        if (epic.getSubTaskIds().isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(0);
        } else {
            List<Sub> listOfEpicSubs = subTasks.values()
                    .stream().filter(value -> value.getEpicId() == epic.getId())
                    .collect(Collectors.toList());
            epic.setStartTime(listOfEpicSubs.stream()
                    .map(Task::getStartTime)
                    .min(LocalDateTime::compareTo)
                    .orElse(null));
            epic.setEndTime(listOfEpicSubs.stream()
                    .map(Task::getEndTime)
                    .max(LocalDateTime::compareTo)
                    .orElse(null));
            epic.setDuration(listOfEpicSubs.stream()
                    .map(Task::getDuration)
                    .mapToLong(Long::longValue)
                    .sum());
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
            //Если вы нашли пересечение по времени хоть с одной задачей, можно дальше не искать, а делать break цикла
            boolean isPossibleBefore = expectStart.isBefore(startOfBusyTime) &&
                    (expectEnd.isBefore(startOfBusyTime) || expectEnd.isEqual(startOfBusyTime));
            boolean isPossibleAfter = expectStart.isAfter(endOfBusyTime) || expectStart.isEqual(endOfBusyTime);
            if (isPossibleBefore == isPossibleAfter) {
                isPossible = false;
                break;
            }
        }
        if (!(isPossible)) System.err.println(
                "WARNING! Time is busy: task named \"" + task.getName() + "\" hasn't be saved.");
        return isPossible;
    }
}
