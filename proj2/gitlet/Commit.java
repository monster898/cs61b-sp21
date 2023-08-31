package gitlet;

// TODO: any imports you need here

import java.util.Calendar;
import java.util.Date; // TODO: You'll likely use this in this class

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Hao Chen
 */
public class Commit {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The timestamp of this commit was made */
    private String timestamp = Calendar.getInstance().set(1970, 1,1);

    /** The message of this Commit. */
    private final String message;

    private final String parent1;

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
}
