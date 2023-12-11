package com.ya.TaskTracker.manager;

public class Managers {
    /* честно говоря, не совсем понял что от меня требуется в задании,
    про утилитарные классы впервые слышу,
    моё решение скорее предположение */

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
