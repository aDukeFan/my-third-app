package com.ya.TaskTracker.manager;

import com.ya.TaskTracker.model.Epic;
import com.ya.TaskTracker.model.SubTask;
import com.ya.TaskTracker.model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    void save(Task task);

    void save(Epic epic);

    void save(SubTask subTask);

    void update(Task task);

    void update(Epic epic);
    void update(SubTask subTask);

    ArrayList<Task> getTasks();

    ArrayList<SubTask> getSubTasks();

    ArrayList<Epic> getEpicTasks();

    void clearAllTasks();

    void clearAllSubTasks();

    void clearAllEpics();

    Task getTaskById(int id);

    SubTask getSubTaskById(int id);

    Epic getEpicById(int id);

    void delTaskById(int id);

    void delSubTaskById(int id);

    void delEpicById(int id);

    void setEpicStatus(Epic epic);

    List<Task> getHistory();

}
