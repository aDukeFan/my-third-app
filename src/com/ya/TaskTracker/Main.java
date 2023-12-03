package com.ya.TaskTracker;

import com.ya.TaskTracker.manager.Manager;
import com.ya.TaskTracker.model.SubTask;
import com.ya.TaskTracker.model.Task;
import com.ya.TaskTracker.model.Epic;

public class Main {

    public static void main(String[] args) {

        Manager manager = new Manager();

        //Создайте 2 задачи
        Task task1 = new Task("Задача 1",
                "Первая задача",
                "NEW");
        manager.save(task1);

        Task task2 = new Task("Задача 2",
                "Вторая задача",
                "IN_PROGRESS");
        manager.save(task2);

        //Один эпик с 2 подзадачами, а другой эпик с 1 подзадачей
        Epic epicWith2Subtask = new Epic("Эпик 2",
                "на ЯндексПрактикум за 10 месяцев");
        manager.save(epicWith2Subtask);

        SubTask subTask1 = new SubTask("Подзадача 1 (эпика 2)",
                "Оплатить учебу",
                "IN_PROGRESS", epicWith2Subtask.getId());
        manager.save(subTask1);

        SubTask subTask2 = new SubTask("Подзадача 2 (эпика 2)",
                "Учиться",
                "NEW", epicWith2Subtask.getId());
        manager.save(subTask2);

        Epic epicWith1Subtask = new Epic("Эпик 1",
                "Готовим глазунью");
        manager.save(epicWith1Subtask);

        SubTask subTask3 = new SubTask("Подзадача 3 (эпика 1)",
                "Готовить",
                "DONE", epicWith1Subtask.getId());
        manager.save(subTask3);

        //Распечатайте списки эпиков, задач и подзадач, через System.out.println(..)
        System.out.println("Список всех Epic: " + manager.getEpicTasks());
        System.out.println("Список всех задач: " + manager.getTasks());
        System.out.println("Список всех подзадач:" + manager.getSubTasks());

        // Измените статусы созданных объектов, распечатайте
        task1.setStatus("DONE");
        task2.setStatus("DONE");
        subTask1.setStatus("NEW");
        subTask2.setStatus("DONE");
        subTask3.setStatus("NEW");
        manager.update(subTask1);
        manager.update(subTask2);
        manager.update(subTask3);

        // Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач
        System.out.printf("Статусы после изменений:\n" +
                        "Задача 1 - %s\n" +
                        "Задача 2 - %s\n" +
                        "Подзадача 1 (эпик 2) - %s\n" +
                        "Подзадача 2 (эпик 2) - %s\n" +
                        "Подзадача 3 (эпик 1) - %s\n" +
                        "Эпик 1 - %s\n" +
                        "Эпик 2 - %s\n",
                task1.getStatus(),
                task2.getStatus(),
                subTask1.getStatus(),
                subTask2.getStatus(),
                subTask3.getStatus(),
                epicWith1Subtask.getStatus(),
                epicWith2Subtask.getStatus());

        // Попробуйте удалить одну из задач и один из эпиков
        manager.delTaskById(task1.getId());
        manager.delEpicById(epicWith1Subtask.getId());
    }
}