package gitlet;

import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *
 *  @author Hao Chen
 */
public class Commit implements Serializable {

    /** The timestamp of this commit was made */
    private OffsetDateTime timestamp = Instant.now().atOffset(ZoneOffset.UTC);

    /** The message of this Commit. */
    private final String message;

    /** The parent commit of this commit */
    private final String parent1;

    /** Another parent commit of this commit, because merge commit have two parents */
    private final String parent2;

    /** The file included in this commit, key is filename, value is file's hash */
    private TreeMap<String, String> trackedFilesMap;

    public Commit(String message, String parent1, String parent2) {
        this.message = message;
        this.parent1 = parent1;
        this.parent2 = parent2;
        trackedFilesMap = new TreeMap<>();
    }

    public static Commit createInitialCommit() {
        Commit commit = new Commit("initial commit", null, null);
        commit.timestamp = Instant.EPOCH.atOffset(ZoneOffset.UTC);
        return commit;
    }

    public TreeMap<String, String> getTrackedFilesMap() {
        return trackedFilesMap;
    }
    public void setTrackedFilesMap(TreeMap<String, String> map) {
        trackedFilesMap = map;
    }
    public String getFirstParentHash() {
        return parent1;
    }
    public String getSecondParentHash() {
        return parent2;
    }

    public String getMessage() {
        return message;
    }

    public String toString(DateTimeFormatter formatter) {
        String formattedTimestamp = timestamp.format(formatter);
        String date = "Date: " + formattedTimestamp;
        return date + "\n" + message;
    }
}
