package tasktracker.manager;

import tasktracker.model.Node;
import tasktracker.model.Task;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {

    private Node<Task> head;
    private Node<Task> tail;
    private final Map<Integer, Node<Task>> acceptedTasks = new HashMap<>();

    private void linkedLast(Task task) {
        if (acceptedTasks.containsKey(task.getId())) {
            removeNode(acceptedTasks.get(task.getId()));
            acceptedTasks.remove(task.getId());
        }
        final Node<Task> oldTail = tail;
        final Node<Task> newNode = new Node<>(tail, task, null);
        tail = newNode;
        if (oldTail == null) head = newNode;
        else oldTail.setNext(newNode);
        acceptedTasks.put(task.getId(), newNode);
    }

    private List<Task> getTasks() {
        List<Task> viewedTasks = new ArrayList<>();
        Node<Task> first = head;
        if (first != null) {
            while (first != null) {
                viewedTasks.add(first.getData());
                first = first.getNext();
            }
        }
        return viewedTasks;
    }

    private void removeNode(Node<Task> node) {
        if (node != null) {
            Node<Task> next = node.getNext();
            Node<Task> prev = node.getPrev();
            node.setData(null);
            if (head == node && tail == node) {
                head = null;
                tail = null;
            } else if (head == node) {
                head = next;
                head.setPrev(null);
            } else if (tail == node) {
                tail = prev;
                tail.setNext(null);
            } else {
                prev.setNext(next);
                next.setPrev(prev);
            }
        }
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            linkedLast(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        removeNode(acceptedTasks.get(id));
        acceptedTasks.remove(id);
    }
}