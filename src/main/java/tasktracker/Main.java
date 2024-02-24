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
        KVServer server = new KVServer();
        server.start();
        HttpTaskManager manager = Managers.getDefault();

        //1. Завел несколько разных задач, эпиков и подзадач.
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

        //2. Запросил некоторые из них, чтобы заполнилась история просмотра.
        manager.getTaskById(2);
        manager.getSubById(5);
        manager.getEpicById(4);

        manager.saveToServer();

        //3. Создал новый HttpTaskManager менеджер методом загрузки с сервера
        HttpTaskManager loadManager = HttpTaskManager.loadFromServer("http://localhost:8078/register");
        //4. Проверяю, что все задачи, эпики, подзадачи, которые были в старом, есть в новом менеджере:
        System.out.println("\n--- Задачи созданного менеджера ---");
        for (Task task : manager.getTasks()) {
            System.out.println(task.getId() + " " + task.getName()
                    + " начинается в " + task.getStartTime());
        }
        System.out.println("--- Задачи загруженного менеджера ---");
        for (Task task : loadManager.getTasks()) {
            System.out.println(task.getId() + " " + task.getName()
                    + " начинается в " + task.getStartTime());
        }
        System.out.println("\n--- Эпики созданного менеджера ---");
        for (Epic task : manager.getEpics()) {
            System.out.println(task.getId() + " " + task.getName()
                    + " начинается в " + task.getStartTime());
        }
        System.out.println("--- Эпики загруженного менеджера ---");
        for (Epic task : loadManager.getEpics()) {
            System.out.println(task.getId() + " " + task.getName()
                    + " начинается в " + task.getStartTime());
        }
        System.out.println("\n--- Подзадачи созданного менеджера ---");
        for (Sub task : manager.getSubs()) {
            System.out.println(task.getId() + " " + task.getName() + " " + task.getEpicId()
                    + " начинается в " + task.getStartTime());
        }
        System.out.println("--- Подзадачи загруженного менеджера ---");
        for (Sub task : loadManager.getSubs()) {
            System.out.println(task.getId() + " " + task.getName() + " " + task.getEpicId()
                    + " начинается в " + task.getStartTime());
        }

        // Проверяю, что история просмотра восстановилась верно:
        System.out.println("\n--- История созданного менеджера ---");
        for (Task task : manager.getHistory()) {
            System.out.println(task.getId() + " " + task.getName());
        }
        System.out.println("--- История загруженного менеджера ---");
        for (Task task : loadManager.getHistory()) {
            System.out.println(task.getId() + " " + task.getName());
        }
        //Проверяю, что лист задач в порядке приоритета восстановился верно:
        System.out.println("\n--- Лист задач в порядке приоритета созданного менеджера ---");
        for (Task task : manager.getPrioritizedTasks()) {
            System.out.println(task.getId() + " " + task.getName());
        }
        System.out.println("--- Лист задач в порядке приоритета загруженного менеджера ---");
        for (Task task : loadManager.getPrioritizedTasks()) {
            System.out.println(task.getId() + " " + task.getName());
        }
        server.stop();
    }
}