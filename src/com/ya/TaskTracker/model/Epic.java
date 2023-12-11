package com.ya.TaskTracker.model;

import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subTaskIds = new ArrayList<>();

    public ArrayList<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void addToSubTaskIds(int id) {
        subTaskIds.add(id);
    }

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
    }
}

