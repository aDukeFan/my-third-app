package com.ya.TaskTracker.model;

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

    public void setStatus(Task task, String newStatus) {
        task.status = newStatus;
    }

    public void setStatus(SubTask subTask, String newStatus) {
        subTask.status = newStatus;
        setEpicStatus(getEpicById(subTask.epicId));
    }

    public int getId(Task task) {
        return task.id;
    }

    public int getId(SubTask subTask) {
        return subTask.id;
    }

    public int getId(Epic epic) {
        return epic.id;
    }

    public String getStatus(Task task) {
        return task.status;
    }

    public String getStatus(SubTask subTask) {
        return subTask.status;
    }

    public String getStatus(Epic epic) {
        return epic.status;
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

    public void make(SubTask subTask, int epicId) {
        setId(nextId);
        subTask.id = nextId;
        subTask.epicId = epicId;
        subTasks.put(subTask.id, subTask);
        getEpicById(epicId).subTaskIds.add(subTask.id);
        setEpicStatus(getEpicById(epicId));
    }

    //Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра
    public void update(Task task) {
        tasks.put(task.id, task);
    }

    public void update(Epic epic) {
        epicTasks.put(epic.id, epic);
    }

    public void update(SubTask subTask) {
        subTasks.put(subTask.id, subTask);
        setEpicStatus(epicTasks.get(subTask.epicId));
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

    // Удаление всех задач
    public void clearTasks() {
        tasks.clear();
    }

    public void clearSubTasks() {
        subTasks.clear();
        epicTasks.forEach((key, value) -> value.subTaskIds.clear());
        epicTasks.forEach((key, value) -> setEpicStatus(value));
    }

    public void clearEpic() {
        subTasks.clear();
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
    public void delTaskById(int id) {
        tasks.remove(id);
    }

    public void delSubTaskById(int id) {
        SubTask subTask = subTasks.get(id);
        getEpicById(subTask.epicId).subTaskIds.removeIf(value -> (value == id));
        setEpicStatus(getEpicById(subTask.epicId));
        subTasks.remove(id);
    }

    public void delEpicById(int id) {
        epicTasks.remove(id);
        for (SubTask subTask : subTasks.values()) {
            if (subTask.epicId == id) {
                subTasks.remove(id);
            }
        }
    }

    // Получение списка всех подзадач определённого эпика
    public ArrayList<Integer> getSubTasksFromEpic(int id) {
        return epicTasks.get(id).subTaskIds;
    }

    //Управление статусом Epic
    private void setEpicStatus(Epic epic) {
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
    }
}