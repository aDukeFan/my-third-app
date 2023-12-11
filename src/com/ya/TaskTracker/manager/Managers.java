package com.ya.TaskTracker.manager;

import com.ya.TaskTracker.model.Task;

import java.util.List;

public class Manager {
    /* честно говоря, не совсем понял что от меня требуется в задании,
    про утилитарные классы впервые слышу,
    моё решение скорее предположение */
    TaskManager taskManager = new InMemoryTaskManager();

    public TaskManager getDefault() {
        return taskManager;
    }

    // Добавьте в служебный класс Managers статический метод HistoryManager getDefaultHistory().
    // Он должен возвращать объект InMemoryHistoryManager — историю просмотров.

    public static List<Task> getDefaultHistory() {
        return InMemoryHistoryManager.getHistory();
    }
}
