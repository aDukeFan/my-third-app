package tasktracker.manager;

import tasktracker.model.Epic;
import tasktracker.model.Sub;
import tasktracker.model.Task;

import java.util.List;

public interface TaskManager {

    void save(Task task);

    void save(Epic epic);

    void save(Sub sub);

    void update(Task task);

    void update(Epic epic);

    void update(Sub sub);

    List<Task> getTasks();

    List<Sub> getSubTasks();

    List<Epic> getEpicTasks();

    void clearAllTasks();

    void clearAllSubTasks();

    void clearAllEpics();

    Task getTaskById(int id);

    Sub getSubTaskById(int id);

    Epic getEpicById(int id);

    void delTaskById(int id);

    void delSubTaskById(int id);

    void delEpicById(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
