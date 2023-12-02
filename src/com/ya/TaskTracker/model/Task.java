package com.ya.TaskTracker.model;

public class Task {
    protected int id;
    protected String name;
    protected String description;
    protected String status;


    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public Task(String name, String description, String status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }
}
