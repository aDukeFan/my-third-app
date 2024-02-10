package tasktracker.manager;

import tasktracker.model.Task;

import java.util.Comparator;

public class CompareTasks implements Comparator<Task> {
    @Override
    public int compare(Task o1, Task o2) {
        if (o1.getStartTime() == o2.getStartTime()) {
            return o1.getDescription().compareTo(o2.getDescription());
        } else {
            return o1.getStartTime().compareTo(o2.getStartTime());
        }
    }
}
