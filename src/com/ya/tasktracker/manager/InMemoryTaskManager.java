package com.ya.tasktracker.manager;

import com.ya.tasktracker.history.HistoryManager;
import com.ya.tasktracker.model.Epic;
import com.ya.tasktracker.model.Status;
import com.ya.tasktracker.model.SubTask;
import com.ya.tasktracker.model.Task;
/* Как раз при нажатии Ctrl + Alt + O у меня IDEA
импорты меняет на java.util.*, вручную поправил: */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, SubTask> subTasks = new HashMap<>();
    protected final Map<Integer, Epic> epicTasks = new HashMap<>();

    public final HistoryManager historyManager = Managers.getDefaultHistory();

    protected int nextId = 1;


    private int generateId() {
        return nextId++;
    }

    @Override
    public void save(Task task) {
        int newId = generateId();
        task.setId(newId);
        tasks.put(newId, task);
    }

    @Override
    public void save(Epic epic) {
        int newId = generateId();
        epic.setId(newId);
        epicTasks.put(newId, epic);
    }

    @Override
    public void save(SubTask subTask) {
        int newtId = generateId();
        subTask.setId(newtId);
        subTasks.put(newtId, subTask);
        Epic epic = epicTasks.get(subTask.getEpicId());
        epic.addToSubTaskIds(newtId);
        setEpicStatus(epic);
    }

    @Override
    public void update(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void update(Epic epic) {
        epicTasks.put(epic.getId(), epic);
    }

    @Override
    public void update(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);
        setEpicStatus(epicTasks.get(subTask.getEpicId()));
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
        }
        tasks.clear();

    }

    @Override
    public void clearAllSubTasks() {
        for (Integer id : subTasks.keySet()) {
            historyManager.remove(id);
        }
        subTasks.clear();
        epicTasks.forEach((key, value) -> value.getSubTaskIds().clear());
        epicTasks.forEach((key, value) -> setEpicStatus(value));
    }

    @Override
    public void clearAllEpics() {
        for (Integer id : subTasks.keySet()) {
            historyManager.remove(id);
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
        tasks.remove(id);
    }

    @Override
    public void delSubTaskById(int id) {
        Epic epic = getEpicById(subTasks.get(id).getEpicId());
        epic.getSubTaskIds().removeIf(value -> (value == id));
        setEpicStatus(epic);
        historyManager.remove(id);
        subTasks.remove(id);
    }

    @Override
    public void delEpicById(int id) {
        List<Integer> subTaskIds = epicTasks.get(id).getSubTaskIds();
        for (Integer subTaskId : subTaskIds) {
            historyManager.remove(subTaskId);
            subTasks.remove(subTaskId);
        }
        historyManager.remove(id);
        epicTasks.remove(id);
    }

    @Override
    public void setEpicStatus(Epic epic) {
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

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

}