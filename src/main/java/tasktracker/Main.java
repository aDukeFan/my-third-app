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

    public static void main(String[] args) throws IOException, InterruptedException {
        /* TODO Добавьте запуск KVServer в Main.main и перезапустите пример использования менеджера.
                Убедитесь, что всё работает и состояние задач теперь хранится на сервере.
           Честно говоря, я не помню, чтобы у нас в main были какие-то примеры использования менеджера.
           Написал небольшой пример, все работает.
           В любом случае, у нас ведь есть тесты для этого, или я не правильно понял?*/

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
        Epic epic = new Epic("Epic",
                "test");
        manager.save(epic);
        Sub sub = new Sub("Sub",
                "test",
                Status.NEW,
                LocalDateTime.of(2023, 10, 10, 10, 10), 10, 3);
        manager.save(sub);
        manager.getTaskById(2);
        manager.getSubById(4);

        System.out.println("\n--- Все сохраняется ---");
        manager.saveToServer();

        System.out.println("\n--- Все загружается ---");
        HttpTaskManager loadManager = HttpTaskManager.loadFromServer("http://localhost:8078/register");

        server.stop();
    }
}