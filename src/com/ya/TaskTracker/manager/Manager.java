package com.ya.TaskTracker.manager;

import com.ya.TaskTracker.model.Epic;
import com.ya.TaskTracker.model.SubTask;
import com.ya.TaskTracker.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Manager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private HashMap<Integer, Epic> epicTasks = new HashMap<>();

    private int nextId = 0;

    private void generateId(int nextId) {
        this.nextId = ++nextId;
    }

    public void save(Task task) {
        generateId(nextId);
        task.setId(nextId);
        tasks.put(nextId, task);
    }

    public void save(Epic epic) {
        generateId(nextId);
        epic.setId(nextId);
        epicTasks.put(nextId, epic);
    }

    public void save(SubTask subTask, int epicId) {
        generateId(nextId);
        subTask.setId(nextId);
        subTask.setEpicId(epicId);
        subTasks.put(nextId, subTask);
        getEpicById(epicId).addToSubTaskIds(nextId);
        setEpicStatus(getEpicById(epicId));
    }

    public void update(Task task) {
        tasks.put(task.getId(), task);
    }

    public void update(Epic epic) {
        epicTasks.put(epic.getId(), epic);
    }

    public void update(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);
        setEpicStatus(epicTasks.get(subTask.getEpicId()));
    }

    // Получение списка всех задач
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public ArrayList<Epic> getEpicTasks() {
        return new ArrayList<>(epicTasks.values());
    }

    public void clearAllTasks() {
        tasks.clear();
    }

    public void clearAllSubTasks() {
        subTasks.clear();
        epicTasks.forEach((key, value) -> value.getSubTaskIds().clear());
        epicTasks.forEach((key, value) -> setEpicStatus(value));
    }

    public void clearAllEpics() {
        subTasks.clear();
        epicTasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public SubTask getSubTaskById(int id) {
        return subTasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epicTasks.get(id);
    }

    public void delTaskById(int id) {
        tasks.remove(id);
    }

    public void delSubTaskById(int id) {
        int epicId = subTasks.get(id).getEpicId();
        getEpicById(epicId).getSubTaskIds().removeIf(value -> (value == id));
        setEpicStatus(getEpicById(epicId));
        subTasks.remove(id);
    }

    public void delEpicById(int id) {
        epicTasks.remove(id);
        for (SubTask subTask : subTasks.values()) {
            if (subTask.getEpicId() == id) {
                subTasks.remove(id);
            }
        }
    }

    //Управление статусом Epic
    private void setEpicStatus(Epic epic) {
        ArrayList<Integer> epicsSubTaskIds = epic.getSubTaskIds();
        if (epicsSubTaskIds.isEmpty()) {
            epic.setStatus("NEW");
        } else {
            HashSet<String> subStatuses = new HashSet<>();
            for (Integer subTaskId : epic.getSubTaskIds()) {
                subStatuses.add(subTasks.get(subTaskId).getStatus());
            }
            if (subStatuses.size() > 1) {
                epic.setStatus("IN_PROGRESS");
            } else {
                epic.setStatus(String.join("", subStatuses));
            }
        }
    }
}