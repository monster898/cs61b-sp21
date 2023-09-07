package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        StagingArea stagingArea = new StagingArea();
        writeObject(INDEX, stagingArea);
        writeContents(HEAD, "ref: refs/heads/master");
        writeContents(MASTER, hash);
        writeObjectWithHashAsFilename(initialCommit, hash);
    }

    public static void add(String fileName) {
        File targetFile = join(CWD, fileName);
        if (!targetFile.exists()) {
           System.out.println("File does not exist.");
           System.exit(0);
        }

        StagingArea stagingArea = readObject(INDEX, StagingArea.class);
        TreeMap<String, String> stagingForAdditionMap = stagingArea.filesForAdditionMap;

        String content = readContentsAsString(targetFile);
        String hash = computeObjHash(content);

        // If the current working version of the file is identical to the version in the current commit
        // not stage it and unstage if it is already there.
        Commit headCommit = getHeadCommit();
        TreeMap<String, String> trackedFilesMap = headCommit.getTrackedFilesMap();
        if (trackedFilesMap.containsKey(fileName) && trackedFilesMap.get(fileName).equals(hash)) {
            stagingForAdditionMap.remove(fileName);
            deleteFileByHash(hash);
            return;
        }

        // Delete previous version of the file in INDEX
        if (stagingForAdditionMap.containsKey(fileName)) {
            String previousHash = stagingForAdditionMap.get(fileName);
            deleteFileByHash(previousHash);
        }

        stagingForAdditionMap.put(fileName, hash);
        writeObjectWithHashAsFilename(content, hash);
        writeObject(INDEX, stagingArea);
    }

    private static Commit getHeadCommit() {
        File file = getCurrentBranchFile();
        String hash = readContentsAsString(file);
        return getObjectByHash(hash, Commit.class);
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
            trackedFilesMap.put(fileName, stagingForAdditionMap.get(fileName));
        }

        Commit newCommit = new Commit(message, headCommitHash, null);
        newCommit.setTrackedFilesMap(trackedFilesMap);
        String newCommitHash = computeObjHash(newCommit);
        writeObjectWithHashAsFilename(newCommit, newCommitHash);
        writeContents(getCurrentBranchFile(), newCommitHash);
        stagingArea.clear();
        writeObject(INDEX, stagingArea);
    }

    public static void rm(String fileName) {
        StagingArea stagingArea = readObject(INDEX, StagingArea.class);
        TreeMap<String, String> trackedFilesMap = getHeadCommit().getTrackedFilesMap();
        TreeMap<String, String> stagingForAdditionMap = stagingArea.filesForAdditionMap;
        TreeMap<String, String> stagingForDeletionMap = stagingArea.filesForDeletionMap;
        if (!stagingForAdditionMap.containsKey(fileName) && !trackedFilesMap.containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        // Unstage the file if it is currently staged for addition.
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
        while (currentCommit != null) {
            printCommit(currentCommit);
            String firstParentHash = currentCommit.getFirstParentHash();
            if (firstParentHash == null) {
                break;
            }
            currentCommit = getObjectByHash(firstParentHash, Commit.class);
        }
    }

    private static void printCommit(Commit commit) {
        String hash = computeObjHash(commit);
        System.out.println("===");
        System.out.println("commit " + hash);
        String firstParentHash = commit.getFirstParentHash();
        String secondParentHash = commit.getSecondParentHash();
        if (firstParentHash != null && secondParentHash != null) {
            // this commit is a merged commit
            System.out.println("Merge: " + firstParentHash + " " + secondParentHash);
        }
        System.out.println(commit.toString(formatter));
        System.out.print("\n");
    }

    private static HashSet<Commit> getAllCommits() {
        HashSet<Commit> allCommits = new HashSet<>();
        List<String> branchHeadsFilename = plainFilenamesIn(BRANCHES_DIR);
        for (String headFilename: branchHeadsFilename) {
            String hash = readContentsAsString(join(BRANCHES_DIR, headFilename));
            Commit commit = readObject(getFileByHash(hash), Commit.class);
            ArrayDeque<Commit> queue = new ArrayDeque<>();
            queue.add(commit);
            while (!queue.isEmpty()) {
                Commit currentCommit = queue.pop();
                allCommits.add(currentCommit);
                String firstParentHash = currentCommit.getFirstParentHash();
                String secondParentHash = currentCommit.getSecondParentHash();
                if (firstParentHash != null) {
                    Commit firstParent = getObjectByHash(firstParentHash, Commit.class);
                    queue.add(firstParent);
                }

                if (secondParentHash != null) {
                    Commit secondParent = getObjectByHash(secondParentHash, Commit.class);
                    queue.add(secondParent);
                }
            }
        }
        return allCommits;
    }

    public static void globalLog() {
        for (Commit commit: getAllCommits()) {
            printCommit(commit);
        }
    }

    public static void find(String commitMessage) {
        boolean found = false;
        for (Commit commit: getAllCommits()) {
            if (commitMessage.equals(commit.getMessage())) {
                found = true;
                System.out.println(computeObjHash(commit));
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status() {

    }

    public static void checkoutFileOnHeadCommit(String fileName) {
        Commit headCommit = getHeadCommit();
        String headCommitHash = computeObjHash(headCommit);
        checkoutFileOnSpecificCommit(headCommitHash, fileName);
    }

    public static void checkoutFileOnSpecificCommit(String commitHash,String fileName) {
        File commitFile = getFileByHash(commitHash);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit commit = getObjectByHash(commitHash, Commit.class);
        TreeMap<String, String> trackedFilesMap = commit.getTrackedFilesMap();
        if (!trackedFilesMap.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        String blobHash = trackedFilesMap.get(fileName);
        writeContents(join(CWD, fileName), getObjectByHash(blobHash, String.class));
    }

    public static void checkoutBranch(String branchName) {

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
