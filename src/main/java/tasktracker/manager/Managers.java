package tasktracker.manager;

import java.io.File;

public class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static FileBackedTasksManager getDefaultFileBackedTasksManager() {
        return new FileBackedTasksManager("history.csv");
    }

    public static HttpTaskManager getDefaultHttpTaskManager() {
        return new HttpTaskManager("http://localhost:8078/register");
    }
}
