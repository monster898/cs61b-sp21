package gitlet;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Hao Chen
 */
public class Commit {
    /** The timestamp of this commit was made */
    private String timestamp = getCurrentTimestamp();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy Z");

    /** The message of this Commit. */
    private final String message;

    /** The parent commit of this commit */
    private final String parent1;

    /** Another parent commit of this commit, because merge commit have two parents */
    private final String parent2;

    /* TODO: fill in the rest of this class. */
    public Commit(String message, String parent) {
        this.message = message;
        parent1 = parent;
        parent2 = null;
    }

    public Commit(String message, String parent1, String parent2) {
        this.message = message;
        this.parent1 = parent1;
        this.parent2 = parent2;
    }

    public void setInitCommitTimestamp() {
        Instant epoch = Instant.ofEpochSecond(0);
        timestamp = epoch.atOffset(ZoneOffset.UTC).format(formatter);
    }

    private String getCurrentTimestamp() {
        LocalDateTime dateTime = LocalDateTime.now(ZoneOffset.ofHours(8)); // Assuming +0800 as the time zone offset
        return dateTime.format(formatter);
    }

    @Override
    public String toString() {
        String date = "Date: " + timestamp;
        return date + "\n" + message;
    }
}
