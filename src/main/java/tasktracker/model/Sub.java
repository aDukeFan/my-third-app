package tasktracker.model;

import java.time.LocalDateTime;

public class Sub extends Task {

    private int epicId;

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public Sub(String name, String description, Status status, LocalDateTime startTime, long duration, int epicId) {
        super(name, description, status, startTime, duration);
        this.type = Type.SUB;
        this.epicId = epicId;
    }
}

