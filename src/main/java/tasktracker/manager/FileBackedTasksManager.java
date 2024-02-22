package tasktracker.manager;

import tasktracker.ManagerSaveException;
import tasktracker.model.*;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private final File history;

    public FileBackedTasksManager(String nameOfFile) {
        this.history = new File(nameOfFile);
    }

    public static FileBackedTasksManager load(File file) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager("history.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            List<String> linesCollection = new ArrayList<>();
            br.readLine();
            while ((line = br.readLine()) != null) {
                linesCollection.add(line);
            }
            if (!linesCollection.isEmpty()) {
                for (String s : linesCollection) {
                    if (!(s.isEmpty())) {
                        String[] lineArray = s.split(",");
                        if (lineArray[1].contains(Type.TASK.toString())) {
                            Task task = taskFromString(s);
                            fileBackedTasksManager.tasks.put(task.getId(), task);
                        } else if (lineArray[1].contains(Type.EPIC.toString())) {
                            Epic epic = epicFromString(s);
                            fileBackedTasksManager.epics.put(epic.getId(), epic);
                        } else if (lineArray[1].contains(Type.SUB.toString())) {
                            Sub sub = subTaskFromString(s);
                            fileBackedTasksManager.subs.put(sub.getId(), sub);
                        }
                    }
                }

                List<Integer> ids = new ArrayList<>();
                ids.addAll(fileBackedTasksManager.tasks.keySet());
                ids.addAll(fileBackedTasksManager.subs.keySet());
                ids.addAll(fileBackedTasksManager.epics.keySet());
                if (!ids.isEmpty()) {
                    int maxId = Collections.max(ids);
                    maxId++;
                    fileBackedTasksManager.setNextId(maxId);
                }
                for (Sub sub : fileBackedTasksManager.subs.values()) {
                    fileBackedTasksManager.epics.get(sub.getEpicId()).addToSubTaskIds(sub.getId());
                }
                for (Epic epic : fileBackedTasksManager.epics.values()) {
                    epic.setEndTime(fileBackedTasksManager.subs.values()
                            .stream()
                            .filter(value -> value.getEpicId() == epic.getId())
                            .map(Task::getEndTime)
                            .filter(endTime -> Optional.ofNullable(endTime).isPresent())
                            .min(LocalDateTime::compareTo).orElse(null));
                }
                fileBackedTasksManager.priority.addAll(fileBackedTasksManager.tasks.values());
                fileBackedTasksManager.priority.addAll(fileBackedTasksManager.subs.values());
                String historyLine = linesCollection.get(linesCollection.size() - 1);
                if (!(historyLine.isBlank() || historyLine.isEmpty())) {
                    historyFromString(historyLine).forEach(id ->
                            fileBackedTasksManager.historyManager.add(fileBackedTasksManager.tasks.get(id)));
                    historyFromString(historyLine).forEach(id ->
                            fileBackedTasksManager.historyManager.add(fileBackedTasksManager.subs.get(id)));
                    historyFromString(historyLine).forEach(id ->
                            fileBackedTasksManager.historyManager.add(fileBackedTasksManager.epics.get(id)));
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Error: File cant be read");
        }
        return fileBackedTasksManager;
    }

    protected void setNextId(int nextId) {
        this.nextId = nextId;
    }

    protected void keep() {
        try (Writer writer = new FileWriter(history)) {
            writer.write(
                    "id,type,name,status,description,startTime,duration,epic\n");
            for (Task task : super.getTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Sub task : super.getSubs()) {
                writer.write(toString(task) + "," + task.getEpicId() + "\n");
            }
            for (Epic task : super.getEpics()) {
                writer.write(toString(task) + "\n");
            }
            writer.write("\n" + historyToString(historyManager));
        } catch (IOException e) {
            throw new ManagerSaveException("Error: File cant be written");
        }
    }

    protected String toString(Task task) {
        String startTime;
        if (Optional.ofNullable(task.getStartTime()).isEmpty()) {
            startTime = "time isn't set";
        } else {
            startTime = task.getStartTime().toString();
        }
        return String.format("%d,%s,%s,%s,%s,%s,%d",
                task.getId(),
                task.getType().toString(),
                task.getName(),
                task.getStatus().toString(),
                task.getDescription(),
                startTime,
                task.getDuration());
    }

    protected static Task taskFromString(String value) {
        String[] taskValues = value.split(",");
        String id = taskValues[0];
        String name = taskValues[2];
        String status = taskValues[3];
        String description = taskValues[4];
        String duration = taskValues[6];
        LocalDateTime startTime = null;
        if (!taskValues[5].equals("time isn't set")) {
            startTime = LocalDateTime.parse(taskValues[5]);
        }
        Task task = new Task(name, description,
                Status.valueOf(status),
                startTime,
                Long.parseLong(duration));
        task.setId(Integer.parseInt(id));
        return task;
    }

    protected static Sub subTaskFromString(String value) {
        String[] taskValues = value.split(",");
        String id = taskValues[0];
        String name = taskValues[2];
        String status = taskValues[3];
        String description = taskValues[4];
        String duration = taskValues[6];
        String epicId = taskValues[7];
        LocalDateTime startTime = null;
        if (!taskValues[5].equals("time isn't set")) {
            startTime = LocalDateTime.parse(taskValues[5]);
        }
        Sub sub = new Sub(name, description,
                Status.valueOf(status),
                startTime,
                Long.parseLong(duration),
                Integer.parseInt(epicId));
        sub.setId(Integer.parseInt(id));
        return sub;
    }

    protected static Epic epicFromString(String value) {
        String[] taskValues = value.split(",");
        String id = taskValues[0];
        String name = taskValues[2];
        String status = taskValues[3];
        String description = taskValues[4];
        String duration = taskValues[6];
        Epic epic = new Epic(
                name, description);
        if (!taskValues[5].equals("time isn't set")) {
            epic.setStartTime(LocalDateTime.parse(taskValues[5]));
        }
        epic.setDuration(Long.parseLong(duration));
        epic.setStatus(Status.valueOf(status));
        epic.setId(Integer.parseInt(id));

        return epic;
    }

    protected static String historyToString(HistoryManager manager) {
        StringBuilder stringBuilder = new StringBuilder();
        String prefix = "";
        for (Task task : manager.getHistory()) {
            stringBuilder.append(prefix);
            prefix = ",";
            stringBuilder.append(task.getId());
        }

        return stringBuilder.toString();
    }

    protected static List<Integer> historyFromString(String value) {
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
        keep();
    }

    @Override
    public void save(Sub sub) {
        super.save(sub);
        keep();
    }

    @Override
    public void save(Epic epic) {
        super.save(epic);
        keep();
    }

    @Override
    public void update(Task task) {
        super.update(task);
        keep();
    }

    @Override
    public void update(Epic epic) {
        super.update(epic);
        keep();
    }

    @Override
    public void update(Sub sub) {
        super.update(sub);
        keep();
    }

    @Override
    public void clearAllTasks() {
        super.clearAllTasks();
        keep();

    }

    @Override
    public void clearAllSubTasks() {
        super.clearAllSubTasks();
        keep();
    }

    @Override
    public void clearAllEpics() {
        super.clearAllEpics();
        keep();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        keep();
        return task;
    }

    @Override
    public Sub getSubById(int id) {
        Sub sub = super.getSubById(id);
        keep();
        return sub;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        keep();
        return epic;
    }

    @Override
    public void delTaskById(int id) {
        super.delTaskById(id);
        keep();
    }

    @Override
    public void delSubTaskById(int id) {
        super.delSubTaskById(id);
        keep();
    }

    @Override
    public void delEpicById(int id) {
        super.delEpicById(id);
        keep();
    }

    @Override
    public List<Task> getTasks() {
        List<Task> result = super.getTasks();
        keep();
        return result;
    }

    @Override
    public List<Sub> getSubs() {
        List<Sub> result = super.getSubs();
        keep();
        return result;
    }

    @Override
    public List<Epic> getEpics() {
        List<Epic> result = super.getEpics();
        keep();
        return result;
    }
}
