import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class Manager {
    private LinkedHashMap<Integer, SimpleTask> simpleTasks = new LinkedHashMap<>();
    private LinkedHashMap<Integer, SubTask> subTasks = new LinkedHashMap<>();
    private LinkedHashMap<Integer, Epic> epicTasks = new LinkedHashMap<>();

    private int nextId = 1;

    // Создание. Сам объект должен передаваться в качестве параметра
    public int make(SimpleTask task) {
        task.id = nextId++;
        simpleTasks.put(task.id, task);
        return task.id;
    }

    public int make(Epic epic) {
        epic.id = nextId++;
        epicTasks.put(epic.id, epic);
        return epic.id;
    }

    public int make(SubTask task) {
        task.id = nextId++;
        subTasks.put(task.id, task);
        return task.id;
    }

    //Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра
    public void update(SimpleTask task) {
        simpleTasks.put(task.id, task);

    }

    public void update(Epic epic) {
        epicTasks.put(epic.id, epic);

    }

    public void update(SubTask task) {
        subTasks.put(task.id, task);

    }

    // Получение списка всех задач
    public LinkedHashMap<Integer, SimpleTask> getSimpleTasks() {
        return simpleTasks;
    }

    public LinkedHashMap<Integer, SubTask> getSubTasks() {
        return subTasks;
    }

    public LinkedHashMap<Integer, Epic> getEpicTasks() {
        return epicTasks;
    }

    // Удаление всех задач.
    public void clearSimpleTasks() {
        simpleTasks.clear();
    }

    public void clearSubTasks() {
        subTasks.clear();
    }

    public void clearEpic() {
        epicTasks.clear();
    }

    // Получение по идентификатору
    public SimpleTask getSimpleTaskById(int id) {
        return simpleTasks.get(id);
    }

    public SubTask getSubTaskById(int id) {
        return subTasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epicTasks.get(id);
    }

    // Удаление по идентификатору
    public SimpleTask delSimpleTaskById(int id) {
        return simpleTasks.remove(id);
    }

    public SubTask delSubTaskById(int id) {
        return subTasks.remove(id);
    }

    public Epic delEpicById(int id) {
        return epicTasks.remove(id);
    }

    // Получение списка всех подзадач определённого эпика
    public ArrayList<Integer> getSubTasksFromEpic(int id) {
        return epicTasks.get(id).subTaskIds;
    }

    //Управление статусом Epic
    public String setEpicStatus(Epic epic) {
        if (epic.subTaskIds.isEmpty()) {
            epic.status = "NEW";
        } else if (epic.subTaskIds.size() == 1) {
            epic.status = subTasks.get(epic.subTaskIds.get(0)).status;
        } else {
            HashSet<String> subStatuses = new HashSet<>();
            for (Integer subTaskId : epic.subTaskIds) {
                subStatuses.add(subTasks.get(subTaskId).status);
            }
            if (subStatuses.size() > 1) {
                epic.status = "IN_PROGRESS";
            } else {
                epic.status = String.join("", subStatuses);
            }
        }
        return epic.status;
    }
}
