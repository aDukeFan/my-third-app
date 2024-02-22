package tasktracker.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private LocalDateTime endTime;

    private final List<Integer> subTaskIds = new ArrayList<>();

    public List<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void addToSubTaskIds(int id) {
        subTaskIds.add(id);
    }

    public Epic(String name, String description) {
        super(name, description, Status.NEW, null, 0);
        this.type = Type.EPIC;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        if (startTime != null) {
            return endTime;
        } else {
            return null;
        }
    }
}

