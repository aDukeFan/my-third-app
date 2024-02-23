package tasktracker.manager;

import java.io.File;

public class Managers {

    private Managers() {
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static FileBackedTasksManager getDefaultFileBackedTasksManager() {
        return new FileBackedTasksManager("fileBackedTasksManagerData.csv");
    }

    public static HttpTaskManager getDefault() {
        return new HttpTaskManager("http://localhost:8078/register");
    }
}
