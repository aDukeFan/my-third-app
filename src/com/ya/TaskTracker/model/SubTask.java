package com.ya.TaskTracker.model;

public class SubTask extends Task {

    private int epicId;

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public SubTask(String name, String description, String status) {
        super(name, description, status);
    }
}

