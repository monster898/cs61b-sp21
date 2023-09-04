package gitlet;

import java.io.Serializable;
import java.util.TreeMap;

public class StagingArea implements Serializable {
    public TreeMap<String, String> filesForAdditionMap;
    public TreeMap<String, String> filesForDeletionMap;
}
