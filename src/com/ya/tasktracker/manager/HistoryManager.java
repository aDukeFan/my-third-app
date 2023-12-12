package com.ya.tasktracker.manager;

import com.ya.tasktracker.model.Task;

import java.util.LinkedList;
import java.util.List;

public interface HistoryManager {
    void add(Task task);
    LinkedList<Task> getHistory();
}
