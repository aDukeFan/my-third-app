package com.ya.tasktracker.manager;

import com.ya.tasktracker.history.HistoryManager;
import com.ya.tasktracker.history.InMemoryHistoryManager;

public class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
