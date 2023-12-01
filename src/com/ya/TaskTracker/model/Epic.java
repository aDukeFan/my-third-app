package com.ya.TaskTracker.model;

import java.util.ArrayList;

public class Epic extends Task {
    protected ArrayList<Integer> subTaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, "NEW");
    }
}

