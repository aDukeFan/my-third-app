package com.ya.tasktracker.history;

import com.ya.tasktracker.model.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);

    List<Task> getHistory();
}