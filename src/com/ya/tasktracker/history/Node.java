package com.ya.tasktracker.history;

public class Node <Task> {
    private Task data;
    private Node<Task> next;
    private Node<Task> prev;

    public Node(Node<Task> prev, Task data, Node<Task> next) {
        this.data = data;
        this.next = next;
        this.prev = prev;
    }

    public Task getData() {
        return data;
    }

    public Node<Task> getNext() {
        return next;
    }

    public Node<Task> getPrev() {
        return prev;
    }

    public void setData(Task data) {
        this.data = data;
    }

    public void setNext(Node<Task> next) {
        this.next = next;
    }

    public void setPrev(Node<Task> prev) {
        this.prev = prev;
    }
}
