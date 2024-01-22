package com.ya.tasktracker.manager;

import com.ya.tasktracker.ManagerSaveException;
import com.ya.tasktracker.history.HistoryManager;
import com.ya.tasktracker.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager implements TaskManager {

    //ТЕСТИРОВАНИЕ:
    public static void main(String[] args) {
        File file = new File("history.csv");
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(file);
        //Заведите несколько разных задач, эпиков и подзадач
        Task task1 = new Task("Задача 1",
                "Первая задача", Status.NEW);
        fileBackedTasksManager.save(task1);
        Task task2 = new Task("Задача 2",
                "Вторая задача", Status.IN_PROGRESS);
        fileBackedTasksManager.save(task2);
        Epic epicWith3Subtasks = new Epic("Эпик 3",
                "включает три подзадачи");
        fileBackedTasksManager.save(epicWith3Subtasks);
        SubTask subTask1 = new SubTask("Подзадача 1 (эпика 3)",
                "Оплатить учебу",
                Status.IN_PROGRESS, epicWith3Subtasks.getId());
        fileBackedTasksManager.save(subTask1);
        SubTask subTask2 = new SubTask("Подзадача 2 (эпика 3)",
                "учиться",
                Status.NEW, epicWith3Subtasks.getId());
        fileBackedTasksManager.save(subTask2);
        SubTask subTask3 = new SubTask("Подзадача 3 (эпика 3)",
                "И еще раз учиться",
                Status.DONE, epicWith3Subtasks.getId());
        fileBackedTasksManager.save(subTask3);
        Epic epicWithoutSubtasks = new Epic("Эпик 0",
                "Без подзадач");
        fileBackedTasksManager.save(epicWithoutSubtasks);
        System.out.println("--- Запросы для истории простмотров: ----------");
        System.out.println(task2.getId() + " "
                + fileBackedTasksManager.getTaskById(task2.getId()).getName());
        System.out.println(task1.getId() + " "
                + fileBackedTasksManager.getTaskById(task1.getId()).getName());
        System.out.println(subTask1.getId() + " "
                + fileBackedTasksManager.getSubTaskById(subTask1.getId()).getName());
        System.out.println(epicWith3Subtasks.getId() + " "
                + fileBackedTasksManager.getEpicById(epicWith3Subtasks.getId()).getName());
        System.out.println("--- История fileBackedTasksManager: -----------");
        for (Task task : fileBackedTasksManager.getHistory()) {
            System.out.println(task.getId() + " " + task.getName());
        }

        FileBackedTasksManager fileBackedTasksManagerFromFile = loadFromFile(file);
        System.out.println("--- История fileBackedTasksManagerFromFile: ---");
        for (Task task : fileBackedTasksManagerFromFile.getHistory()) {
            System.out.println(task.getId() + " " + task.getName());
        }
    }

    private final File history;

    private FileBackedTasksManager(File history) {
        this.history = history;
    }

    public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(file);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            List<String> linesCollection = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                linesCollection.add(line);
            }
            for (String s : linesCollection) {
                if (!(s.isEmpty())) {
                    String[] lineArray = s.split(",");
                    if (lineArray[1].contains(Type.TASK.toString())) {
                        fileBackedTasksManager.update(taskFromString(s));
                    } else if (lineArray[1].contains(Type.EPIC.toString())) {
                        fileBackedTasksManager.update(epicFromString(s));
                    } else if (lineArray[1].contains(Type.SUB.toString())) {
                        fileBackedTasksManager.update(subTaskFromString(s));
                    }
                }
            }
            String historyLine = linesCollection.get(linesCollection.size() - 1);
            if (!(historyLine.isBlank() || historyLine.isEmpty())) {
                for (Integer i : historyFromString(historyLine)) {
                    if (fileBackedTasksManager.tasks.containsKey(i)) {
                        fileBackedTasksManager.historyManager.add(fileBackedTasksManager.getTaskById(i));
                    } else if (fileBackedTasksManager.subTasks.containsKey(i)) {
                        fileBackedTasksManager.historyManager.add(fileBackedTasksManager.getSubTaskById(i));
                    } else if (fileBackedTasksManager.epicTasks.containsKey(i)) {
                        fileBackedTasksManager.historyManager.add(fileBackedTasksManager.getEpicById(i));
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Oops!");
        }
        return fileBackedTasksManager;
    }

    private void saveToFile() {
        try (Writer writer = new FileWriter(history)) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : super.getTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (SubTask task : super.getSubTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic task : super.getEpicTasks()) {
                writer.write(toString(task) + "\n");
            }
            writer.write("\n" + historyToString(historyManager));
        } catch (IOException e) {
            throw new ManagerSaveException("Sorry, but I can't =(");
        }
    }

    private String toString(Task task) {
        String id = String.valueOf(task.getId());
        String type = Type.TASK.toString();
        String name = task.getName();
        String status = task.getStatus().toString();
        String description = task.getDescription();

        return String.format("%s,%s,%s,%s,%s", id, type, name, status, description);
    }

    private String toString(Epic task) {
        String id = String.valueOf(task.getId());
        String type = Type.EPIC.toString();
        String name = task.getName();
        String status = task.getStatus().toString();
        String description = task.getDescription();

        return String.format("%s,%s,%s,%s,%s", id, type, name, status, description);
    }

    private String toString(SubTask task) {
        String id = String.valueOf(task.getId());
        String type = Type.SUB.toString();
        String name = task.getName();
        String status = task.getStatus().toString();
        String description = task.getDescription();
        String epicId = String.valueOf(task.getEpicId());

        return String.format("%s,%s,%s,%s,%s,%s", id, type, name, status, description, epicId);
    }

    private static Task taskFromString(String value) {
        String[] taskValues = value.split(",");
        String id = taskValues[0];
        String name = taskValues[2];
        String status = taskValues[3];
        String description = taskValues[4];
        Task task = new Task(name, description, Status.valueOf(status));
        task.setId(Integer.parseInt(id));

        return task;
    }

    private static SubTask subTaskFromString(String value) {
        String[] taskValues = value.split(",");
        String id = taskValues[0];
        String name = taskValues[2];
        String status = taskValues[3];
        String description = taskValues[4];
        int epicId = Integer.parseInt(taskValues[5]);
        SubTask subTask = new SubTask(name, description, Status.valueOf(status), epicId);
        subTask.setId(Integer.parseInt(id));

        return subTask;
    }

    private static Epic epicFromString(String value) {
        String[] taskValues = value.split(",");
        String id = taskValues[0];
        String name = taskValues[2];
        String status = taskValues[3];
        String description = taskValues[4];
        Epic epic = new Epic(name, description);
        epic.setStatus(Status.valueOf(status));
        epic.setId(Integer.parseInt(id));

        return epic;
    }

    static String historyToString(HistoryManager manager) {
        StringBuilder stringBuilder = new StringBuilder();
        String prefix = "";
        for (Task task : manager.getHistory()) {
            stringBuilder.append(prefix);
            prefix = ",";
            stringBuilder.append(task.getId());
        }

        return stringBuilder.toString();
    }

    static List<Integer> historyFromString(String value) {
        String[] historyMass = value.split(",");
        List<Integer> historyList = new ArrayList<>();
        for (String mass : historyMass) {
            historyList.add(Integer.parseInt(mass));
        }
        return historyList;
    }

    @Override
    public void save(Task task) {
        super.save(task);
        saveToFile();
    }

    @Override
    public void save(SubTask subTask) {
        super.save(subTask);
        saveToFile();
    }

    @Override
    public void save(Epic epic) {
        super.save(epic);
        saveToFile();
    }

    @Override
    public void update(Task task) {
        super.update(task);
        saveToFile();
    }

    @Override
    public void update(Epic epic) {
        super.update(epic);
        saveToFile();
    }

    @Override
    public void update(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);
        saveToFile();
    }

    @Override
    public void clearAllTasks() {
        super.clearAllTasks();
        saveToFile();

    }

    @Override
    public void clearAllSubTasks() {
        super.clearAllSubTasks();
        saveToFile();
    }

    @Override
    public void clearAllEpics() {
        super.clearAllEpics();
        saveToFile();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        saveToFile();
        return task;
    }

    @Override
    public SubTask getSubTaskById(int id) {
        SubTask subTask = super.getSubTaskById(id);
        saveToFile();
        return subTask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        saveToFile();
        return epic;
    }

    @Override
    public void delTaskById(int id) {
        super.delTaskById(id);
        saveToFile();
    }

    @Override
    public void delSubTaskById(int id) {
        super.delSubTaskById(id);
        saveToFile();
    }

    @Override
    public void delEpicById(int id) {
        super.delEpicById(id);
        saveToFile();
    }

}
