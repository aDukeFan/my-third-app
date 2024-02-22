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
    protected final Map<Integer, Sub> subs = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();

    protected final Set<Task> priority = new TreeSet<>(new StartTimeComparator());

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
        epics.put(newId, epic);
    }

    @Override
    public void save(Sub sub) {
        if (isValid(sub)) {
            int newtId = generateId();
            sub.setId(newtId);
            subs.put(newtId, sub);
            Epic epic = epics.get(sub.getEpicId());
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
        Epic savedEpic = epics.get(epic.getId());
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
        epics.put(savedEpic.getId(), savedEpic);
    }

    @Override
    public void update(Sub sub) {
        priority.removeIf(taskIn -> (taskIn.getId() == sub.getId()));
        if (isValid(sub)) {
            subs.put(sub.getId(), sub);
            priority.add(sub);
            setEpicStatus(epics.get(sub.getEpicId()));
            setEpicTime(epics.get(sub.getEpicId()));
        } else {
            priority.add(subs.get(sub.getId()));
        }
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Sub> getSubs() {
        return new ArrayList<>(subs.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
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
        for (Integer id : subs.keySet()) {
            historyManager.remove(id);
            priority.remove(subs.get(id));
        }
        subs.clear();
        epics.forEach((key, value) -> value.getSubTaskIds().clear());
        epics.forEach((key, value) -> setEpicStatus(value));
        epics.forEach((key, value) -> value.setDuration(0));
    }

    @Override
    public void clearAllEpics() {
        for (Integer id : subs.keySet()) {
            historyManager.remove(id);
            priority.remove(subs.get(id));
        }
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
        subs.clear();
        epics.clear();
    }

    @Override
    public Task getTaskById(int id) {
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Sub getSubById(int id) {
        historyManager.add(subs.get(id));
        return subs.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public void delTaskById(int id) {
        historyManager.remove(id);
        priority.removeIf(task -> (task.getId() == id));
        tasks.remove(id);
    }

    @Override
    public void delSubTaskById(int id) {
        Epic epic = getEpicById(subs.get(id).getEpicId());
        epic.getSubTaskIds().removeIf(value -> (value == id));
        priority.removeIf(task -> (task.getId() == id));
        setEpicStatus(epic);
        setEpicTime(epic);
        historyManager.remove(id);
        subs.remove(id);
    }

    @Override
    public void delEpicById(int id) {
        List<Integer> subTaskIds = epics.get(id).getSubTaskIds();
        for (Integer subTaskId : subTaskIds) {
            historyManager.remove(subTaskId);
            subs.remove(subTaskId);
            priority.removeIf(task -> (task.getId() == subTaskId));
        }
        historyManager.remove(id);
        epics.remove(id);
    }


    private void setEpicStatus(Epic epic) {
        List<Integer> epicsSubTaskIds = epic.getSubTaskIds();
        if (epicsSubTaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
        } else {
            HashSet<Status> subStatuses = new HashSet<>();
            for (Integer subTaskId : epic.getSubTaskIds()) {
                subStatuses.add(subs.get(subTaskId).getStatus());
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
            List<Sub> listOfEpicSubs = subs.values()
                    .stream().filter(value -> value.getEpicId() == epic.getId())
                    .collect(Collectors.toList());
            epic.setStartTime(listOfEpicSubs.stream()
                    .map(Task::getStartTime)
                    .filter(startTime -> Optional.ofNullable(startTime).isPresent())
                    .min(LocalDateTime::compareTo)
                    .orElse(null));
            epic.setEndTime(listOfEpicSubs.stream()
                    .map(Task::getEndTime)
                    .filter(epicTime -> Optional.ofNullable(epicTime).isPresent())
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
        if (Optional.ofNullable(task.getStartTime()).isPresent() && !priority.isEmpty()) {
            List<Task> listOfTasksAreSetStartTime = priority.stream()
                    .filter(t -> Optional.ofNullable(t.getStartTime()).isPresent())
                    .collect(Collectors.toList());
            if (!listOfTasksAreSetStartTime.isEmpty()) {
                LocalDateTime expectStart = task.getStartTime();
                LocalDateTime expectEnd = task.getEndTime();
                for (Task existTask : listOfTasksAreSetStartTime) {
                    LocalDateTime startOfBusyTime = existTask.getStartTime();
                    LocalDateTime endOfBusyTime = existTask.getEndTime();
                    boolean isPossibleBefore = expectStart.isBefore(startOfBusyTime) &&
                            (expectEnd.isBefore(startOfBusyTime) || expectEnd.isEqual(startOfBusyTime));
                    boolean isPossibleAfter = expectStart.isAfter(endOfBusyTime) || expectStart.isEqual(endOfBusyTime);
                    if (isPossibleBefore == isPossibleAfter) {
                        System.err.println(
                                "WARNING! Time is busy: task named \"" + task.getName() + "\" hasn't be saved.");
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
