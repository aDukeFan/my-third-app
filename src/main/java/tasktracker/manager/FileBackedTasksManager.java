package tasktracker.manager;

import tasktracker.ManagerSaveException;
import tasktracker.model.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private final File history;

    public FileBackedTasksManager(File history) {
        this.history = history;
    }

    public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(file);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            List<String> linesCollection = new ArrayList<>();
            br.readLine();
            while ((line = br.readLine()) != null) {
                linesCollection.add(line);
            }
            for (String s : linesCollection) {
                if (!(s.isEmpty())) {
                    String[] lineArray = s.split(",");
                    if (lineArray[1].contains(Type.TASK.toString())) {
                        fileBackedTasksManager.tasks.put(taskFromString(s).getId(), taskFromString(s));
                    } else if (lineArray[1].contains(Type.EPIC.toString())) {
                        fileBackedTasksManager.epicTasks.put(epicFromString(s).getId(), epicFromString(s));
                    } else if (lineArray[1].contains(Type.SUB.toString())) {
                        fileBackedTasksManager.subTasks.put(subTaskFromString(s).getId(), subTaskFromString(s));
                    }
                }
            }
            List<Integer> ids = new ArrayList<>();
            ids.addAll(fileBackedTasksManager.tasks.keySet());
            ids.addAll(fileBackedTasksManager.subTasks.keySet());
            ids.addAll(fileBackedTasksManager.epicTasks.keySet());
            int maxId = Collections.max(ids);
            maxId++;
            fileBackedTasksManager.setNextId(maxId);
            for (SubTask subTask : fileBackedTasksManager.subTasks.values()) {
                fileBackedTasksManager.epicTasks.get(subTask.getEpicId()).addToSubTaskIds(subTask.getId());
            }
            fileBackedTasksManager.priority.addAll(fileBackedTasksManager.tasks.values());
            fileBackedTasksManager.priority.addAll(fileBackedTasksManager.subTasks.values());

            String historyLine = linesCollection.get(linesCollection.size() - 1);
            if (!(historyLine.isBlank() || historyLine.isEmpty())) {
                for (Integer i : historyFromString(historyLine)) {
                    if (fileBackedTasksManager.tasks.containsKey(i)) {
                        fileBackedTasksManager.historyManager.add(fileBackedTasksManager.tasks.get(i));
                    } else if (fileBackedTasksManager.subTasks.containsKey(i)) {
                        fileBackedTasksManager.historyManager.add(fileBackedTasksManager.subTasks.get(i));
                    } else if (fileBackedTasksManager.epicTasks.containsKey(i)) {
                        fileBackedTasksManager.historyManager.add(fileBackedTasksManager.epicTasks.get(i));
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Error: File cant be read");
        }
        return fileBackedTasksManager;
    }

    private void setNextId(int nextId) {
        this.nextId = nextId;
    }

    private void saveToFile() {
        try (Writer writer = new FileWriter(history)) {
            writer.write(
                    "id,type,name,status,description,startTime,duration,epic\n");
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
            throw new ManagerSaveException("Error: File cant be write");
        }
    }

    private String toString(Task task) {
        String id = String.valueOf(task.getId());
        String type = Type.TASK.toString();
        String name = task.getName();
        String status = task.getStatus().toString();
        String description = task.getDescription();
        String startTime = task.getStartTime().toString();
        String duration = String.valueOf(task.getDuration());

        return String.format("%s,%s,%s,%s,%s,%s,%s",
                id, type, name, status, description, startTime, duration);
    }

    private String toString(Epic task) {
        String id = String.valueOf(task.getId());
        String type = Type.EPIC.toString();
        String name = task.getName();
        String status = task.getStatus().toString();
        String description = task.getDescription();
        String startTime;
        if (Optional.ofNullable(task.getStartTime()).isEmpty()) {
            startTime = "time isn't set";
        } else {
            startTime = task.getStartTime().toString();
        }
        String duration = String.valueOf(task.getDuration());
        return String.format("%s,%s,%s,%s,%s,%s,%s",
                id, type, name, status, description, startTime, duration);
    }

    private String toString(SubTask task) {
        String id = String.valueOf(task.getId());
        String type = Type.SUB.toString();
        String name = task.getName();
        String status = task.getStatus().toString();
        String description = task.getDescription();
        String startTime = task.getStartTime().toString();
        String duration = String.valueOf(task.getDuration());
        String epicId = String.valueOf(task.getEpicId());

        return String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                id, type, name, status, description, startTime, duration, epicId);
    }

    private static Task taskFromString(String value) {
        String[] taskValues = value.split(",");
        String id = taskValues[0];
        String name = taskValues[2];
        String status = taskValues[3];
        String description = taskValues[4];
        String[] startTimeArr = taskValues[5].split("T");
        String startTime = startTimeArr[0] + " " + startTimeArr[1];
        String duration = taskValues[6];
        Task task = new Task(
                name,
                description,
                Status.valueOf(status),
                LocalDateTime.parse(startTime,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                Long.parseLong(duration));
        task.setId(Integer.parseInt(id));

        return task;
    }

    private static SubTask subTaskFromString(String value) {
        String[] taskValues = value.split(",");
        String id = taskValues[0];
        String name = taskValues[2];
        String status = taskValues[3];
        String description = taskValues[4];
        String[] startTimeArr = taskValues[5].split("T");
        String startTime = startTimeArr[0] + " " + startTimeArr[1];
        String duration = taskValues[6];
        String epicId = taskValues[7];
        SubTask subTask = new SubTask(
                name, description, Status.valueOf(status),
                LocalDateTime.parse(startTime,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                Long.parseLong(duration),
                Integer.parseInt(epicId));
        subTask.setId(Integer.parseInt(id));
        return subTask;
    }

    private static Epic epicFromString(String value) {
        String[] taskValues = value.split(",");
        String id = taskValues[0];
        String name = taskValues[2];
        String status = taskValues[3];
        String description = taskValues[4];
        String duration = taskValues[6];
        Epic epic = new Epic(
                name, description);
        if (!(taskValues[5].equals("time isn't set"))) {
            String[] startTimeArr = taskValues[5].split("T");
            String startTime = startTimeArr[0] + " " + startTimeArr[1];
            epic.setStartTime(LocalDateTime.parse(startTime,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            epic.setDuration(Long.parseLong(duration));
        }
        epic.setStatus(Status.valueOf(status));
        epic.setId(Integer.parseInt(id));

        return epic;
    }

    private static String historyToString(HistoryManager manager) {
        StringBuilder stringBuilder = new StringBuilder();
        String prefix = "";
        for (Task task : manager.getHistory()) {
            stringBuilder.append(prefix);
            prefix = ",";
            stringBuilder.append(task.getId());
        }

        return stringBuilder.toString();
    }

    private static List<Integer> historyFromString(String value) {
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
        super.update(subTask);
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

    @Override
    public List<Task> getTasks() {
        List<Task> result = super.getTasks();
        saveToFile();
        return result;
    }

    @Override
    public List<SubTask> getSubTasks() {
        List<SubTask> result = super.getSubTasks();
        saveToFile();
        return result;
    }

    @Override
    public List<Epic> getEpicTasks() {
        List<Epic> result = super.getEpicTasks();
        saveToFile();
        return result;
    }


}