package com.ya.TaskTracker.manager;

import com.ya.TaskTracker.model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> viewedTasks = new ArrayList<>();

    @Override
    public void add(Task task) {
        viewedTasks.add(task);
    }
    @Override
    public List<Task> getHistory() {
        while (viewedTasks.size() > 10) {
            viewedTasks.remove(0);
        }
        return viewedTasks;
    }
}
