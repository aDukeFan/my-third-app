package com.ya.tasktracker;

import com.ya.tasktracker.manager.Managers;
import com.ya.tasktracker.manager.TaskManager;
import com.ya.tasktracker.model.Epic;
import com.ya.tasktracker.model.Status;
import com.ya.tasktracker.model.SubTask;
import com.ya.tasktracker.model.Task;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = Managers.getDefault();
        //Тестирование
        //создайте 2 задачи
        Task task1 = new Task("Задача 1",
                "Первая задача", Status.NEW);
        taskManager.save(task1);
        Task task2 = new Task("Задача 2",
                "Вторая задача", Status.IN_PROGRESS);
        taskManager.save(task2);

        // создайте эпик с 3 подзадачами
        Epic epicWith3Subtasks = new Epic("Эпик 3",
                "включает три подзадачи");
        taskManager.save(epicWith3Subtasks);

        SubTask subTask1 = new SubTask("Подзадача 1 (эпика 3)",
                "Оплатить учебу",
                Status.IN_PROGRESS, epicWith3Subtasks.getId());
        taskManager.save(subTask1);

        SubTask subTask2 = new SubTask("Подзадача 2 (эпика 3)",
                "Учиться, учиться",
                Status.NEW, epicWith3Subtasks.getId());
        taskManager.save(subTask2);
        SubTask subTask3 = new SubTask("Подзадача 3 (эпика 3)",
                "И еще раз учиться",
                Status.DONE, epicWith3Subtasks.getId());
        taskManager.save(subTask3);

        //создайте эпик без подзадач
        Epic epicWithoutSubtasks = new Epic("Эпик 0",
                "Без подзадач");
        taskManager.save(epicWithoutSubtasks);

        //запросите созданные задачи несколько раз в разном порядке;
        //после каждого запроса выведите историю и убедитесь, что в ней нет повторов;
        taskManager.getTaskById(task1.getId()); //1
        System.out.println(taskManager.getHistory());
        taskManager.getTaskById(task2.getId()); //2
        System.out.println(taskManager.getHistory());
        taskManager.getEpicById(epicWith3Subtasks.getId()); //3
        System.out.println(taskManager.getHistory());
        taskManager.getSubTaskById(subTask1.getId());//4
        System.out.println(taskManager.getHistory());
        taskManager.getSubTaskById(subTask2.getId()); //5
        System.out.println(taskManager.getHistory());
        taskManager.getSubTaskById(subTask3.getId()); //6
        System.out.println(taskManager.getHistory());
        taskManager.getEpicById(epicWithoutSubtasks.getId()); //7
        System.out.println(taskManager.getHistory());
        taskManager.getTaskById(task1.getId()); //8
        System.out.println(taskManager.getHistory());

        System.out.println(epicWith3Subtasks.getSubTaskIds() + " субтаски эпика3");

        System.out.println("\nудалите задачу, которая есть в истории, " +
                "и проверьте, что при печати она не будет выводиться");
        taskManager.delTaskById(task2.getId());
        System.out.println(taskManager.getHistory());

        System.out.println("\nудалите эпик с тремя подзадачами и убедитесь,\n" +
                "что из истории удалился как сам эпик, так и все его подзадачи.");
        System.out.println(taskManager.getHistory().size() +
                " - размер истории до удаление эпика с 3 подзадачами");
        taskManager.delTaskById(epicWith3Subtasks.getId());
        System.out.println(taskManager.getHistory().size() +
                " - размер после удаления эпика с 3 подзадачами," + " подзадачи не удаляются =(");
        System.out.println(taskManager.getHistory());
        System.out.println("То есть, да я понимаю, что у меня ошибка,\n" +
                "но никак не могу ее найти уже второй день, буду рад,\n" +
                "если ты мне на нее укажешь, спасибо!");

        System.out.println("\nПосле удаления эпика," +
                " удаление его субтасков происходит успешно");
        System.out.println(taskManager.getSubTasks().size() + " кол-во до");
        taskManager.delEpicById(epicWith3Subtasks.getId());
        System.out.println(taskManager.getSubTasks().size() + " кол-во после");
    }
}