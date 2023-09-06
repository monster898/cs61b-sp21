package gitlet;

import java.io.Serializable;
import java.util.TreeMap;

public class StagingArea implements Serializable {
    public TreeMap<String, String> filesForAdditionMap = new TreeMap<>();
    public TreeMap<String, String> filesForDeletionMap = new TreeMap<>();

    public void clear() {
        filesForAdditionMap.clear();
        filesForDeletionMap.clear();
    }
}
