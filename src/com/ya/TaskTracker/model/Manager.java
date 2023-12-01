package com.ya.TaskTracker;

import com.ya.TaskTracker.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Manager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private HashMap<Integer, Epic> epicTasks = new HashMap<>();

    private int nextId = 0;

    // Добавил set метод:
    private void setId(int nextId) {
        nextId++;
        this.nextId = nextId;
    }

    // Создание. Присваивание ID реализовал через set метод:
    public void make(Task task) {
        setId(nextId);
        task.id = nextId;
        tasks.put(task.id, task);
    }

    public void make(Epic epic) {
        setId(nextId);
        epic.id = nextId;
        epicTasks.put(epic.id, epic);
    }

    public void make(SubTask task) {
        setId(nextId);
        task.id = nextId;
        subTasks.put(task.id, task);
    }

    //Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра
    public void update(Task task) {
        tasks.put(task.id, task);

    }

    public void update(Epic epic) {
        epicTasks.put(epic.id, epic);

    }

    public void update(SubTask task) {
        subTasks.put(task.id, task);

    }

    // Получение списка всех задач
    public ArrayList<Task> getTasks() {
        ArrayList<Task> values = new ArrayList<>();
        tasks.forEach((key, value) -> values.add(value));
        return values;
    }

    public ArrayList<SubTask> getSubTasks() {
        ArrayList<SubTask> values = new ArrayList<>();
        subTasks.forEach((key, value) -> values.add(value));
        return values;
    }

    public ArrayList<Epic> getEpicTasks() {
        ArrayList<Epic> values = new ArrayList<>();
        epicTasks.forEach((key, value) -> values.add(value));
        return values;
    }

    // Удаление всех задач.
    public void clearTasks() {
        tasks.clear();
    }

    public void clearSubTasks() {
        subTasks.clear();
    }

    public void clearEpic() {
        epicTasks.clear();
    }

    // Получение по идентификатору
    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public SubTask getSubTaskById(int id) {
        return subTasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epicTasks.get(id);
    }

    // Удаление по идентификатору
    public Task delSimpleTaskById(int id) {
        return tasks.remove(id);
    }

    public SubTask delSubTaskById(int id) {
        return subTasks.remove(id);
    }

    public Epic delEpicById(int id) {
        return epicTasks.remove(id);
    }

    // Получение списка всех подзадач определённого эпика
    public ArrayList<Integer> getSubTasksFromEpic(int id) {
        return epicTasks.get(id).subTaskIds;
    }

    //Управление статусом Epic
    public String setEpicStatus(Epic epic) {
        if (epic.subTaskIds.isEmpty()) {
            epic.status = "NEW";
        } else if (epic.subTaskIds.size() == 1) {
            epic.status = subTasks.get(epic.subTaskIds.get(0)).status;
        } else {
            HashSet<String> subStatuses = new HashSet<>();
            for (Integer subTaskId : epic.subTaskIds) {
                subStatuses.add(subTasks.get(subTaskId).status);
            }
            if (subStatuses.size() > 1) {
                epic.status = "IN_PROGRESS";
            } else {
                epic.status = String.join("", subStatuses);
            }
        }
        return epic.status;
    }
}