package com.ya.TaskTracker.manager;

import com.ya.TaskTracker.model.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);
    List<Task> getHistory();

}
