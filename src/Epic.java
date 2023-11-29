import java.util.ArrayList;

public class Epic extends Task {
    public ArrayList<Integer> subTaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
