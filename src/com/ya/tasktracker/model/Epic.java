package com.ya.tasktracker.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subTaskIds = new ArrayList<>();

    public List<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void addToSubTaskIds(int id) {
        subTaskIds.add(id);
    }

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
    }
}

