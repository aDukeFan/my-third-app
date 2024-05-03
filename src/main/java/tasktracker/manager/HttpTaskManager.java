package tasktracker.manager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import tasktracker.api.GsonMaker;
import tasktracker.api.KVTaskClient;
import tasktracker.model.Epic;
import tasktracker.model.Sub;
import tasktracker.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class HttpTaskManager extends FileBackedTasksManager {

    private final Gson gson = new GsonMaker().makeSpesialGson();

    private final KVTaskClient client;

    public HttpTaskManager(String urlToKVServer) {
        super("httpTaskManagerData.csv");
        this.client = new KVTaskClient(urlToKVServer);

    }

    public void saveToServer() {
        client.put("tasks", gson.toJson(getTasks()));
        client.put("subs", gson.toJson(getSubs()));
        client.put("epics", gson.toJson(getEpics()));
        client.put("history", historyToString(historyManager));
    }

    public static HttpTaskManager loadFromServer(String url) {
        HttpTaskManager manager = new HttpTaskManager(url);
        JsonArray jsonTasksArray = JsonParser.parseString(manager.client.load("tasks")).getAsJsonArray();
        Gson gson = new GsonMaker().makeSpesialGson();
        for (JsonElement element : jsonTasksArray) {
            Task task = gson.fromJson(element, Task.class);
            manager.tasks.put(task.getId(), task);
        }
        JsonArray jsonEpicsArray = JsonParser.parseString(manager.client.load("epics")).getAsJsonArray();
        for (JsonElement element : jsonEpicsArray) {
            Epic epic = gson.fromJson(element, Epic.class);
            manager.epics.put(epic.getId(), epic);
        }
        JsonArray jsonSubsArray = JsonParser.parseString(manager.client.load("subs")).getAsJsonArray();
        for (JsonElement element : jsonSubsArray) {
            Sub sub = gson.fromJson(element, Sub.class);
            manager.subs.put(sub.getId(), sub);
        }
        for (Integer id : historyFromString(manager.client.load("history"))) {
            if (manager.tasks.containsKey(id)) {
                manager.historyManager.add(manager.tasks.get(id));
            }
            if (manager.subs.containsKey(id)) {
                manager.historyManager.add(manager.subs.get(id));
            }
            if (manager.epics.containsKey(id)) {
                manager.historyManager.add(manager.epics.get(id));
            }
        }

        List<Integer> ids = new ArrayList<>();
        ids.addAll(manager.tasks.keySet());
        ids.addAll(manager.subs.keySet());
        ids.addAll(manager.epics.keySet());
        int maxId = Collections.max(ids);
        maxId++;
        manager.setNextId(maxId);

        manager.priority.addAll(manager.tasks.values());
        manager.priority.addAll(manager.subs.values());

        return manager;
    }
}
