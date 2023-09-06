package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy Z").withZone(ZoneId.systemDefault());

    public static void init() {
        BRANCHES_DIR.mkdirs();

        Commit initialCommit = Commit.createInitialCommit();

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
        TreeMap<String, String> trackedFilesMap = headCommit.getTrackedFilesMap();
        if (trackedFilesMap.containsKey(fileName) && trackedFilesMap.get(fileName).equals(hash)) {
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
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }

        StagingArea stagingArea = readObject(INDEX, StagingArea.class);
        TreeMap<String, String> stagingForAdditionMap = stagingArea.filesForAdditionMap;
        TreeMap<String, String> stagingForDeletionMap = stagingArea.filesForDeletionMap;
        if (stagingForAdditionMap.isEmpty() && stagingForDeletionMap.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }

        Commit headCommit = getHeadCommit();
        String headCommitHash = computeObjHash(headCommit);
        TreeMap<String, String> trackedFilesMap = headCommit.getTrackedFilesMap();
        for (String fileName: stagingForDeletionMap.keySet()) {
            trackedFilesMap.remove(fileName);
        }
        for (String fileName: stagingForAdditionMap.keySet()) {
            trackedFilesMap.put(fileName, stagingArea.filesForAdditionMap.get(fileName));
        }

        Commit newCommit = new Commit(message, headCommitHash, null);
        newCommit.setTrackedFilesMap(trackedFilesMap);
        String newCommitHash = computeObjHash(newCommit);
        writeObjectWithHashName(newCommit, newCommitHash);
        writeContents(getCurrentBranchFile(), newCommitHash);
        stagingArea.clear();
        writeObject(INDEX, stagingArea);
    }

    public static void rm(String fileName) {
        // Unstage the file if it is currently staged for addition.
        StagingArea stagingArea = readObject(INDEX, StagingArea.class);
        TreeMap<String, String> trackedFilesMap = getHeadCommit().getTrackedFilesMap();
        TreeMap<String, String> stagingForAdditionMap = stagingArea.filesForAdditionMap;
        TreeMap<String, String> stagingForDeletionMap = stagingArea.filesForDeletionMap;
        if (!stagingForAdditionMap.containsKey(fileName) && !trackedFilesMap.containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (stagingForAdditionMap.containsKey(fileName)) {
            stagingForAdditionMap.remove(fileName);
            deleteFileByHash(stagingForAdditionMap.get(fileName));
        }
        //  If the file is tracked in the current commit, stage it for removal
        if (trackedFilesMap.containsKey(fileName)) {
            stagingForDeletionMap.put(fileName, trackedFilesMap.get(fileName));
        }
        //  remove the file from the working directory if the user has not already done so
        join(CWD, fileName).delete();
    }

    public static void log() {
        Commit currentCommit = getHeadCommit();
        String hash = computeObjHash(currentCommit);
        while (currentCommit != null) {
            System.out.println("===");
            System.out.println("commit " + hash);
            String firstParentHash = currentCommit.getFirstParentHash();
            String secondParentHash = currentCommit.getSecondParentHash();
            if (firstParentHash != null && secondParentHash != null) {
                // this commit is a merged commit
                System.out.println("Merge: " + hash + " " + currentCommit.getSecondParentHash());
            }
            System.out.println(currentCommit.toString(formatter));
            System.out.print("\n");
            if (firstParentHash == null) {
                break;
            }
            hash = firstParentHash;
            currentCommit = readObject(getFileByHash(firstParentHash), Commit.class);
        }
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
