package com.ya.TaskTracker;

import com.ya.TaskTracker.manager.Managers;
import com.ya.TaskTracker.manager.TaskManager;
import com.ya.TaskTracker.model.Epic;
import com.ya.TaskTracker.model.Status;
import com.ya.TaskTracker.model.SubTask;
import com.ya.TaskTracker.model.Task;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = Managers.getDefault();
        //Тестирование
        //создайте несколько задач разного типа.
        Task task1 = new Task("Задача 1",
                "Первая задача", Status.NEW);
        taskManager.save(task1);
        Task task2 = new Task("Задача 2",
                "Вторая задача", Status.IN_PROGRESS);
        taskManager.save(task2);

        Epic epicWith2Subtask = new Epic("Эпик 2",
                "на ЯндексПрактикум за 10 месяцев");
        taskManager.save(epicWith2Subtask);

        SubTask subTask1 = new SubTask("Подзадача 1 (эпика 2)",
                "Оплатить учебу",
                Status.IN_PROGRESS, epicWith2Subtask.getId());
        taskManager.save(subTask1);

        SubTask subTask2 = new SubTask("Подзадача 2 (эпика 2)",
                "Учиться",
                Status.NEW, epicWith2Subtask.getId());
        taskManager.save(subTask2);

        Epic epicWith1Subtask = new Epic("Эпик 1",
                "Готовим глазунью");
        taskManager.save(epicWith1Subtask);

        SubTask subTask3 = new SubTask("Подзадача 3 (эпика 1)",
                "Готовить",
                Status.DONE, epicWith1Subtask.getId());
        taskManager.save(subTask3);


        // вызовите разные методы интерфейса TaskManager
        // и напечатайте историю просмотров после каждого вызова.
        System.out.println(taskManager.getHistory());
        /* у меня на входе история имеет в себе просмотр трех эпиков, поскольку метод save()
        при создании Subtask использует getEpicById(subTask.getEpicId());
        я оставил эту проблему открытой, поскольку из условий задачи не понятно,
        является ли такое использование этого метода просмотром задачи */
        taskManager.getTaskById(task1.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubTaskById(subTask1.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubTaskById(subTask2.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubTaskById(subTask3.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getEpicById(epicWith2Subtask.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getEpicById(epicWith1Subtask.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubTaskById(subTask1.getId());
        taskManager.getSubTaskById(subTask2.getId());
        taskManager.getSubTaskById(subTask3.getId());
        System.out.println(taskManager.getHistory().size());
    }
}