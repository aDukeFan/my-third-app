package tasktracker.model;

import java.time.LocalDateTime;

public class Task {
    protected int id;
    protected Type type;
    protected String name;
    protected String description;
    protected Status status;

    protected long duration;

    protected LocalDateTime startTime;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Task(String name, String description, Status status, LocalDateTime startTime, long duration) {
        this.name = name;
        this.type = Type.TASK;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getEndTime() {
        return startTime.plusMinutes(duration);
    }

    public long getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Type getType() {
        return type;
    }
}
