package com.ya.tasktracker.history;

import com.ya.tasktracker.model.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final LinkedList<Task> viewedTasks = new LinkedList<>();

    private static final int MAX_SIZE_OF_VIEWED_TASKS_LIST = 10;

    @Override
    public void add(Task task) {
        if (task != null) {
            viewedTasks.add(task);
        }
        if (viewedTasks.size() > MAX_SIZE_OF_VIEWED_TASKS_LIST) {
            viewedTasks.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(viewedTasks);
    }
}
