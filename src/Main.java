public class Main {

    public static void main(String[] args) {

        // Тестирование
        Manager manager = new Manager();

        Epic epic2Subtask = new Epic("Пройти обучение JavaDev",
                "на ЯндексПрактикум за 10 месяцев");
        manager.make(epic2Subtask);

        SubTask subTask1 = new SubTask("Заплатить за обучение",
                "Взять деньги из накоплений",
                "DONE");
        manager.make(subTask1);
        epic2Subtask.subTaskIds.add(subTask1.id);

        SubTask subTask2 = new SubTask("Обучаться",
                "Запоминать, практиковаться, задавать вопросы",
                "NEW");
        manager.make(subTask2);
        epic2Subtask.subTaskIds.add(subTask2.id);

        Epic epic1Subtask = new Epic("Приготовить завтрак",
                "Из того, что есть, например, яиц");
        manager.make(epic1Subtask);

        SubTask subTask3 = new SubTask("Готовим глазунью",
                "Два яйца разбить на сковороду и подождать пару минут",
                "DONE");
        manager.make(subTask3);
        epic1Subtask.subTaskIds.add(subTask3.id);

        Epic epic0Subtask = new Epic("Пустой эпик",
                "без элементов");
        manager.make(epic0Subtask);

        System.out.println("Список простых задач:");
        System.out.println(manager.getSimpleTasks());
        System.out.println("Список всех Epic:");
        System.out.println(manager.getEpicTasks());
        System.out.println("Список всех подзадач:");
        System.out.println(manager.getSubTasks());

        System.out.println("Статус эпика с 1 подзадачей:");
        System.out.println(manager.setEpicStatus(epic1Subtask));
        System.out.println("Статус эпика с 2 подзадачами:");
        System.out.println(manager.setEpicStatus(epic2Subtask));
        System.out.println("Статус эпика без подзадач:");
        System.out.println(manager.setEpicStatus(epic0Subtask));

    }
}