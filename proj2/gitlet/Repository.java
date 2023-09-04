package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *
 *  @author Hao Chen
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /* The .gitlet/objects directory*/
    public  static final File OBJECTS_DIR = join(GITLET_DIR, "objects");

    public static final File BRANCHES_DIR = join(GITLET_DIR, "refs", "heads");

    public static final File HEAD = join(GITLET_DIR, "HEAD");

    public static final  File MASTER = join(BRANCHES_DIR, "master");

    public static final File INDEX = join(GITLET_DIR, "index");

    public static void init() {
        BRANCHES_DIR.mkdirs();

        Commit initialCommit = new Commit("initial commit", null);
        initialCommit.setInitCommitTimestamp();

        String hash = computeObjHash(initialCommit);
        writeContents(HEAD, "ref: refs/heads/master");
        writeContents(MASTER, hash);
        writeObjectWithHashName(initialCommit, hash);
    }

    public static void add(String fileName) {
        File targetFile = join(CWD, fileName);
        if (!targetFile.exists()) {
           System.out.println("File does not exist.");
           System.exit(0);
        }

        TreeMap<String, String> filesForAdditionMap;
        StagingArea stagingArea = readObject(INDEX, StagingArea.class);
        if (!INDEX.exists()) {
            filesForAdditionMap = new TreeMap<>();
            stagingArea.filesForAdditionMap = filesForAdditionMap;
        }
        filesForAdditionMap = stagingArea.filesForAdditionMap;

        String content = readContentsAsString(targetFile);
        String hash = computeObjHash(content);

        Commit headCommit = getHeadCommit();
        TreeMap<String, String> fileNameToHashMap = headCommit.getFileNameToHashMap();
        if (fileNameToHashMap.get(fileName).equals(hash)) {
            filesForAdditionMap.remove(fileName);
            deleteFileByHash(hash);
            return;
        }
        filesForAdditionMap.put(fileName, hash);
        writeObjectWithHashName(content, hash);
        writeObject(INDEX, stagingArea);
    }

    private static Commit getHeadCommit() {
        File file = getCurrentBranchFile();
        String hash = readContentsAsString(file);
        return readObject(getFileByHash(hash), Commit.class);
    }

    private static File getCurrentBranchFile() {
        String content = readContentsAsString(HEAD);
        String branchPath = content.split(" ")[1];
        return join(GITLET_DIR, branchPath);
    }

    private static <T extends Serializable> String computeObjHash(T obj) {
        return sha1(serialize(obj));
    }

    public static void commit(String message) {
        Commit headCommit = getHeadCommit();
        String headCommitHash = computeObjHash(headCommit);
        TreeMap<String, String> fileNameToHashMap = headCommit.getFileNameToHashMap();
        StagingArea stagingArea = readObject(INDEX, StagingArea.class);
        for (String fileName: stagingArea.filesForDeletionMap.keySet()) {
            fileNameToHashMap.remove(fileName);
        }

        for (String fileName: stagingArea.filesForAdditionMap.keySet()) {
            fileNameToHashMap.put(fileName, stagingArea.filesForAdditionMap.get(fileName));
        }
        Commit newCommit = new Commit(message, headCommitHash);
        newCommit.setFileNameToHashMap(fileNameToHashMap);
        String newCommitHash = computeObjHash(newCommit);
        writeObjectWithHashName(newCommit, newCommitHash);
        writeContents(getCurrentBranchFile(), newCommitHash);
    }

    public static void rm() {
        // delete if exists on addition map
    }

    public static void log() {

    }

    public static void globalLog() {

    }

    public static void find() {

    }

    public static void status() {

    }

    public static void checkout() {

    }

    public static void branch() {

    }

    public static void rmBranch() {

    }

    public static void reset() {

    }

    public static void merge() {

    }


}
