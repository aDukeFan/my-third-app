package tasktracker;

import tasktracker.api.KVServer;
import tasktracker.manager.HttpTaskManager;
import tasktracker.manager.Managers;
import tasktracker.model.Epic;
import tasktracker.model.Status;
import tasktracker.model.Sub;
import tasktracker.model.Task;

import java.io.IOException;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) throws IOException {
        // test of the use
        KVServer server = new KVServer();
        server.start();
        HttpTaskManager manager = Managers.getDefault();

        Task firstTask = new Task("First Task",
                "test",
                Status.NEW,
                LocalDateTime.of(2020, 10, 10, 10, 10), 10);
        manager.save(firstTask);
        Task secondTask = new Task("Second Task",
                "test",
                Status.NEW,
                LocalDateTime.of(2019, 10, 10, 10, 10), 10);
        manager.save(secondTask);
        Epic firstEpic = new Epic("First epic",
                "test");
        manager.save(firstEpic);
        Epic secondEpic = new Epic("Second epic (without subs)",
                "test");
        manager.save(secondEpic);
        Sub sub = new Sub("Sub",
                "test",
                Status.NEW,
                LocalDateTime.of(2023, 10, 10, 10, 10), 10, 3);
        manager.save(sub);

        manager.getTaskById(2);
        manager.getSubById(5);
        manager.getEpicById(4);

        manager.saveToServer();

        HttpTaskManager loadManager = HttpTaskManager.loadFromServer("http://localhost:8078/register");

        System.out.println("\n--- new HttpTaskManager tasks  ---");
        for (Task task : manager.getTasks()) {
            System.out.println(task.getId() + " " + task.getName()
                    + " start in " + task.getStartTime());
        }
        System.out.println("--- loaded HttpTaskManager tasks ---");
        for (Task task : loadManager.getTasks()) {
            System.out.println(task.getId() + " " + task.getName()
                    + " start in " + task.getStartTime());
        }
        System.out.println("\n--- new HttpTaskManager epics ---");
        for (Epic task : manager.getEpics()) {
            System.out.println(task.getId() + " " + task.getName()
                    + " start in " + task.getStartTime());
        }
        System.out.println("--- loaded HttpTaskManager epics ---");
        for (Epic task : loadManager.getEpics()) {
            System.out.println(task.getId() + " " + task.getName()
                    + " start in " + task.getStartTime());
        }
        System.out.println("\n--- new HttpTaskManager subs ---");
        for (Sub task : manager.getSubs()) {
            System.out.println(task.getId() + " " + task.getName() + " " + task.getEpicId()
                    + " start in " + task.getStartTime());
        }
        System.out.println("--- loaded HttpTaskManager subs ---");
        for (Sub task : loadManager.getSubs()) {
            System.out.println(task.getId() + " " + task.getName() + " " + task.getEpicId()
                    + " start in " + task.getStartTime());
        }

        System.out.println("\n--- new HttpTaskManager history ---");
        for (Task task : manager.getHistory()) {
            System.out.println(task.getId() + " " + task.getName());
        }
        System.out.println("--- loaded HttpTaskManager history ---");
        for (Task task : loadManager.getHistory()) {
            System.out.println(task.getId() + " " + task.getName());
        }

        System.out.println("\n--- new HttpTaskManager  priority list ---");
        for (Task task : manager.getPrioritizedTasks()) {
            System.out.println(task.getId() + " " + task.getName());
        }
        System.out.println("--- loaded HttpTaskManager priority list ---");
        for (Task task : loadManager.getPrioritizedTasks()) {
            System.out.println(task.getId() + " " + task.getName());
        }
        server.stop();
    }
}