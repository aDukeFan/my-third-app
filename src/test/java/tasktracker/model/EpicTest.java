package tasktracker.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tasktracker.manager.InMemoryTaskManager;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {

    private final InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
    private Epic makeTestEpic() {
        Epic epic = new Epic(
                "Epic",
                "for test");
        inMemoryTaskManager.save(epic);
        return epic;
    }
    private void makeTestSub(Status status, int epicId, LocalDateTime startTime) {
        SubTask subTask = new SubTask(
                "Sub",
                "for test",
                status,
                startTime,
                10,
                epicId);
        inMemoryTaskManager.save(subTask);
    }

    @Test
    public void statusOfEpicWithoutSubsShouldBeNew() {
        assertEquals(Status.NEW, makeTestEpic().getStatus());

    }
    @Test
    public void statusOfEpicWithSubsWhichHaveStatusNewShouldBeNew() {
        Epic epic = makeTestEpic();
        makeTestSub(Status.NEW, epic.getId(), LocalDateTime.of(2024, 2, 9, 16, 30));
        makeTestSub(Status.NEW, epic.getId(), LocalDateTime.of(2024, 3, 9, 16, 30));
        assertEquals(Status.NEW, epic.getStatus());
    }
    @Test
    public void statusOfEpicWithSubsWhichHaveStatusDoneShouldBeDone() {
        Epic epic = makeTestEpic();
        makeTestSub(Status.DONE, epic.getId(),LocalDateTime.of(2024, 2, 9, 16, 30));
        makeTestSub(Status.DONE, epic.getId(),LocalDateTime.of(2024, 3, 9, 16, 30));
        assertEquals(Status.DONE, epic.getStatus());
    }
    @Test
    public void statusOfEpicWithSubsWhichHaveStatusesNewAndDoneShouldBeInProgress() {
        Epic epic = makeTestEpic();
        makeTestSub(Status.NEW, epic.getId(),LocalDateTime.of(2024, 2, 9, 16, 30));
        makeTestSub(Status.DONE, epic.getId(),LocalDateTime.of(2024, 3, 9, 16, 30));
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    public void statusOfEpicWithSubsWhichHaveStatusesInProgressShouldBeInProgress() {
        Epic epic = makeTestEpic();
        makeTestSub(Status.IN_PROGRESS, epic.getId(),LocalDateTime.of(2024, 2, 9, 16, 30));
        makeTestSub(Status.IN_PROGRESS, epic.getId(),LocalDateTime.of(2024, 3, 9, 16, 30));
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    public void shouldSetRightEpicTime() {
        Epic epic = makeTestEpic();
        Assertions.assertNull(epic.getStartTime());
        Assertions.assertNull(epic.getEndTime());
        Assertions.assertEquals(0, epic.getDuration());
        makeTestSub(Status.NEW, epic.getId(), LocalDateTime.of(2024, 2, 10, 20, 30));
        makeTestSub(Status.NEW, epic.getId(), LocalDateTime.of(2024, 2, 10, 20, 50));
        Assertions.assertEquals(20, epic.getDuration());
        Assertions.assertEquals(LocalDateTime.of(2024, 2, 10, 20, 30),
                epic.getStartTime());
        Assertions.assertEquals(LocalDateTime.of(2024, 2, 10, 21,0 ),
                epic.getEndTime());

    }
}