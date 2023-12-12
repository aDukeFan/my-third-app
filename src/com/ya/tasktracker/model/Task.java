package com.ya.tasktracker.model;

public class Task {
    protected int id;
    protected String name;
    protected String description;
    protected Status status;


    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
