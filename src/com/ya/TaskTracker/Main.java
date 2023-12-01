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
                "DONE");
        manager.make(task2);

        //Один эпик с 2 подзадачами, а другой эпик с 1 подзадачей.
        Epic epicWith2Subtask = new Epic("Пройти обучение JavaDev",
                "на ЯндексПрактикум за 10 месяцев");
        manager.make(epicWith2Subtask);

        SubTask subTask1 = new SubTask("Заплатить за обучение",
                "Взять деньги из накоплений",
                "DONE");
        manager.make(subTask1);
        epicWith2Subtask.subTaskIds.add(subTask1.id);

        SubTask subTask2 = new SubTask("Обучаться",
                "Запоминать, практиковаться, задавать вопросы",
                "NEW");
        manager.make(subTask2);
        epicWith2Subtask.subTaskIds.add(subTask2.id);

        Epic epicWith1Subtask = new Epic("Приготовить завтрак",
                "Из того, что есть, например, яиц");
        manager.make(epicWith1Subtask);

        SubTask subTask3 = new SubTask("Готовим глазунью",
                "Два яйца разбить на сковороду и подождать пару минут",
                "DONE");
        manager.make(subTask3);
        epicWith1Subtask.subTaskIds.add(subTask3.id);

        //Распечатайте списки эпиков, задач и подзадач, через System.out.println(..)
        System.out.println("Список всех Epic:");
        System.out.println(manager.getEpicTasks());
        System.out.println("Список всех задач:");
        System.out.println(manager.getTasks());
        System.out.println("Список всех подзадач:");
        System.out.println(manager.getSubTasks());

        // Измените статусы созданных объектов, распечатайте. Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
        // И, наконец, попробуйте удалить одну из задач и один из эпиков.

    }
}