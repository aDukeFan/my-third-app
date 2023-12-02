package com.ya.TaskTracker;

import com.ya.TaskTracker.model.Manager;
import com.ya.TaskTracker.model.SubTask;
import com.ya.TaskTracker.model.Task;
import com.ya.TaskTracker.model.Epic;

public class Main {

    public static void main(String[] args) {

        // Тестирование в соотвествии с ТЗ:
        Manager manager = new Manager();

        //Создайте 2 задачи:
        Task task1 = new Task("Задача 1",
                "Первая задача",
                "NEW");
        manager.make(task1);

        Task task2 = new Task("Задача 2",
                "Вторая задача",
                "IN_PROGRESS");
        manager.make(task2);

        //Один эпик с 2 подзадачами, а другой эпик с 1 подзадачей.
        Epic epicWith2Subtask = new Epic("Эпик 2",
                "на ЯндексПрактикум за 10 месяцев");
        manager.make(epicWith2Subtask);

        SubTask subTask1 = new SubTask("Подзадача 1 (эпика 2)",
                "Оплатить учебу",
                "IN_PROGRESS");
        manager.make(subTask1, manager.getId(epicWith2Subtask));

        SubTask subTask2 = new SubTask("Подзадача 2 (эпика 2)",
                "Учиться",
                "NEW");
        manager.make(subTask2, manager.getId(epicWith2Subtask));

        Epic epicWith1Subtask = new Epic("Эпик 1",
                "Готовим глазунью");
        manager.make(epicWith1Subtask);

        SubTask subTask3 = new SubTask("Подзадача 3 (эпика 1)",
                "Готовить",
                "DONE");
        manager.make(subTask3, manager.getId(epicWith1Subtask));

        //Распечатайте списки эпиков, задач и подзадач, через System.out.println(..)
        System.out.println("Список всех Epic: " + manager.getEpicTasks());
        System.out.println("Список всех задач: " + manager.getTasks());
        System.out.println("Список всех подзадач:" + manager.getSubTasks());

        // Измените статусы созданных объектов, распечатайте.
        manager.setStatus(task1, "DONE");
        manager.setStatus(task2, "DONE");
        manager.setStatus(subTask1, "NEW");
        manager.setStatus(subTask2, "DONE");
        manager.setStatus(subTask3, "NEW");
        // Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач
        System.out.printf("Статусы после изменений:\n" +
                        "Задача 1 - %s\n" +
                        "Задача 2 - %s\n" +
                        "Подзадача 1 (эпик 2) - %s\n" +
                        "Подзадача 2 (эпик 2) - %s\n" +
                        "Подзадача 3 (эпик 1) - %s\n" +
                        "Эпик 1 - %s\n" +
                        "Эпик 2 - %s\n",
                manager.getStatus(task1),
                manager.getStatus(task2),
                manager.getStatus(subTask1),
                manager.getStatus(subTask2),
                manager.getStatus(subTask3),
                manager.getStatus(epicWith1Subtask),
                manager.getStatus(epicWith2Subtask));

        // Попробуйте удалить одну из задач и один из эпиков.
        manager.delTaskById(manager.getId(task1));
        manager.delEpicById(manager.getId(epicWith2Subtask));
    }
}