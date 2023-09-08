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

    public static final File COMMITS = join(GITLET_DIR, "commits");

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy Z").withZone(ZoneId.systemDefault());

    public static void init() {
        BRANCHES_DIR.mkdirs();
        Commit initialCommit = Commit.createInitialCommit();
        String hash = computeObjHash(initialCommit);
        StagingArea stagingArea = new StagingArea();
        HashSet<String> allCommits = new HashSet<>();
        allCommits.add(hash);
        writeObject(INDEX, stagingArea);
        writeContents(HEAD, "master");
        writeContents(MASTER, hash);
        writeObjectWithHashAsFilename(initialCommit, hash);
        writeObject(COMMITS, allCommits);
    }

    public static void add(String fileName) {
        File targetFile = join(CWD, fileName);
        if (!targetFile.exists()) {
           System.out.println("File does not exist.");
           System.exit(0);
        }

        StagingArea stagingArea = readObject(INDEX, StagingArea.class);
        TreeMap<String, String> stagingForAdditionMap = stagingArea.filesForAdditionMap;
        TreeMap<String, String> stagingForDeletionMap = stagingArea.filesForDeletionMap;

        String content = readContentsAsString(targetFile);
        String hash = computeObjHash(content);

        // If the current working version of the file is identical to the version in the current commit
        // not stage it and unstage if it is already there.
        Commit headCommit = getHeadCommit();
        TreeMap<String, String> trackedFilesMap = headCommit.getTrackedFilesMap();
        if (trackedFilesMap.containsKey(fileName) && trackedFilesMap.get(fileName).equals(hash)) {
            if (stagingForAdditionMap.containsKey(fileName)) {
                stagingForAdditionMap.remove(fileName);
                deleteFileByHash(hash);
            }
            stagingForDeletionMap.remove(fileName);
            writeObject(INDEX, stagingArea);
            return;
        }

        // Delete the staged blob if it's different from the newly added one.
        String stagedBlobHash = stagingForAdditionMap.get(fileName);
        if (stagingForAdditionMap.containsKey(fileName) && !hash.equals(stagedBlobHash)) {
            deleteFileByHash(stagedBlobHash);
        }

        stagingForAdditionMap.put(fileName, hash);
        writeObjectWithHashAsFilename(content, hash);
        writeObject(INDEX, stagingArea);
    }

    private static Commit getHeadCommit() {
        File file = getCurrentBranchHeadFile();
        String hash = readContentsAsString(file);
        return getObjectByHash(hash, Commit.class);
    }

    private static File getCurrentBranchHeadFile() {
        String branchName = readContentsAsString(HEAD);
        return join(BRANCHES_DIR,branchName);
    }

    private static <T extends Serializable> String computeObjHash(T obj) {
        return sha1(serialize(obj));
    }

    public static void commit(String message, String mergedCommitHash) {
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

        Commit newCommit = new Commit(message, headCommitHash, mergedCommitHash);
        newCommit.setTrackedFilesMap(trackedFilesMap);
        String newCommitHash = computeObjHash(newCommit);
        writeObjectWithHashAsFilename(newCommit, newCommitHash);

        HashSet<String> allCommits = getAllCommits();
        allCommits.add(newCommitHash);
        writeObject(COMMITS, allCommits);

        writeContents(getCurrentBranchHeadFile(), newCommitHash);
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
            String hash = stagingForAdditionMap.remove(fileName);
            deleteFileByHash(hash);
        }
        //  If the file is tracked in the current commit, stage it for removal
        if (trackedFilesMap.containsKey(fileName)) {
            stagingForDeletionMap.put(fileName, trackedFilesMap.get(fileName));
            //  remove the file from the working directory if the user has not already done so
            restrictedDelete(join(CWD, fileName));
        }
        writeObject(INDEX, stagingArea);
    }

    public static void log() {
        Commit currentCommit = getHeadCommit();
        while (true) {
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
            System.out.println("Merge: " + firstParentHash.substring(0, 7) + " " + secondParentHash.substring(0, 7));
        }
        System.out.println(commit.toString(formatter));
        System.out.print("\n");
    }

    private static HashSet<String> getAllCommits() {
        return readObject(COMMITS, HashSet.class);
    }

    private static HashSet<String> traversal(String commitHash) {
        HashSet<String> result = new HashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(commitHash);
        while (!queue.isEmpty()) {
            String currentCommitHash = queue.pop();

            result.add(currentCommitHash);

            Commit commit = getObjectByHash(currentCommitHash, Commit.class);
            String firstParentHash = commit.getFirstParentHash();
            String secondParentHash = commit.getSecondParentHash();
            if (firstParentHash != null) {
                queue.add(firstParentHash);
            }

            if (secondParentHash != null) {
                queue.add(secondParentHash);
            }
        }
        return result;
    }

    public static void globalLog() {
        for (String commitHash: getAllCommits()) {
            Commit commit = readObject(getFileByHash(commitHash), Commit.class);
            printCommit(commit);
        }
    }

    public static void find(String commitMessage) {
        boolean found = false;
        for (String commitHash: getAllCommits()) {
            Commit commit = readObject(getFileByHash(commitHash), Commit.class);
            if (commitMessage.equals(commit.getMessage())) {
                found = true;
                System.out.println(computeObjHash(commit));
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    private static <K extends String, V> List<String> getMapKeyListInLexicographicOrder(Map<K, V> map) {
        List<String> keyList = new ArrayList<>(map.keySet());
        Collections.sort(keyList);
        return keyList;
    }

    private static boolean isCurrentBranch(String branchName) {
        return readContentsAsString(HEAD).equals(branchName);
    }

    private static List<String> getUntrackedFilenameList() {
        // 1. Files present in the working directory but neither staged for addition nor tracked.
        // 2. Files that have been staged for removal, but then re-created without Gitlet’s knowledge.
        List<String> result = new ArrayList<>();
        List<String> cwdFilenames = plainFilenamesIn(CWD);
        TreeMap<String, String> trackedFilesMap = getHeadCommit().getTrackedFilesMap();
        StagingArea stagingArea = readObject(INDEX, StagingArea.class);
        TreeMap<String, String> stagingForAdditionMap = stagingArea.filesForAdditionMap;
        TreeMap<String, String> stagingForDeletionMap = stagingArea.filesForDeletionMap;
        if (cwdFilenames != null) {
            for (String filename: cwdFilenames) {
                // Files that have been staged for removal, but then re-created without Gitlet’s knowledge.
                if (stagingForDeletionMap.containsKey(filename)) {
                    result.add(filename);
                }

                // Files present in the working directory but neither staged for addition nor tracked.
                if (!stagingForAdditionMap.containsKey(filename) && !trackedFilesMap.containsKey(filename)) {
                    result.add(filename);
                }
            }
        }
        return result;
    }

    private static String getBlobHash(File file) {
        return computeObjHash(readContentsAsString(file));
    }

    public static void status() {
        // There is an empty line between sections, and the entire status ends in an empty line as well
        List<String> branches = plainFilenamesIn(BRANCHES_DIR);
        Collections.sort(branches);
        System.out.println("=== Branches ===");
        for (String branchName: branches) {
            String displayName = branchName;
            if (isCurrentBranch(branchName)) {
                displayName = "*" + displayName;
            }
            System.out.println(displayName);
        }
        System.out.print("\n");

        StagingArea stagingArea = readObject(INDEX, StagingArea.class);
        TreeMap<String, String> stagingForAdditionMap = stagingArea.filesForAdditionMap;
        List<String> stagingForAdditionFilenames = getMapKeyListInLexicographicOrder(stagingForAdditionMap);
        System.out.println("=== Staged Files ===");
        for (String filename: stagingForAdditionFilenames) {
            System.out.println(filename);
        }
        System.out.print("\n");

        TreeMap<String, String> stagingForDeletionMap = stagingArea.filesForDeletionMap;
        List<String> stagingForDeletionFilenames = getMapKeyListInLexicographicOrder(stagingForDeletionMap);
        System.out.println("=== Removed Files ===");
        for (String filename: stagingForDeletionFilenames) {
            System.out.println(filename);
        }
        System.out.print("\n");

        TreeMap<String, String> trackedFilesMap = getHeadCommit().getTrackedFilesMap();
        List<String> modificationNotStagedFilenames = new ArrayList<>();
        // 1. Tracked in the current commit, changed in the working directory, but not staged;
        // 2. Not staged for removal, but tracked in the current commit and deleted from the working directory.
        for (String filename: trackedFilesMap.keySet()) {
            File file = join(CWD, filename);
            if (file.exists()) {
                String currentBlobHash = getBlobHash(file);
                String trackedBlobHash = trackedFilesMap.get(filename);

                if (!stagingForAdditionMap.containsKey(filename) && !currentBlobHash.equals(trackedBlobHash)) {
                    modificationNotStagedFilenames.add(filename + " (modified)");
                }
            } else if (!stagingForDeletionFilenames.contains(filename)) {
                modificationNotStagedFilenames.add(filename + " (deleted)");
            }
        }
        // 3. Staged for addition, but with different contents than in the working directory.
        // 4. Staged for addition, but deleted in the working directory.
        for (String filename: stagingForAdditionFilenames) {
            if (trackedFilesMap.containsKey(filename)) {
                // handled above
                continue;
            }

            File file = join(CWD, filename);
            if (!file.exists()) {
                modificationNotStagedFilenames.add(filename + " (deleted)");
                continue;
            }
            String currentBlobHash = getBlobHash(file);
            String stagedBlobHash = stagingForAdditionMap.get(filename);
            if (!currentBlobHash.equals(stagedBlobHash)) {
                modificationNotStagedFilenames.add(filename + " (modified)");
            }
        }
        Collections.sort(modificationNotStagedFilenames);
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String filename: modificationNotStagedFilenames) {
            System.out.println(filename);
        }
        System.out.print("\n");

        List<String> untrackedFilenames = getUntrackedFilenameList();
        Collections.sort(untrackedFilenames);
        System.out.println("=== Untracked Files ===");
        for (String filename: untrackedFilenames) {
            System.out.println(filename);
        }
        System.out.print("\n");
    }

    public static void checkoutFileOnHeadCommit(String fileName) {
        Commit headCommit = getHeadCommit();
        String headCommitHash = computeObjHash(headCommit);
        checkoutFilesOnSpecificCommit(headCommitHash, new String[]{fileName});
    }
    public static  void checkoutFileOnSpecificCommit(String commitHash, String filename) {
        File commitFile = getFileByHash(commitHash);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        checkoutFilesOnSpecificCommit(commitHash, new String[]{filename});
    }

    /**
     * Takes the version of the file as it exists in the specific commit and puts it in the working directory,
     * overwriting the version of the file that's already there if there is one.
     * */
    public static void checkoutFilesOnSpecificCommit(String commitHash, String[] filenames) {
        Commit commit = getObjectByHash(commitHash, Commit.class);
        TreeMap<String, String> trackedFilesMap = commit.getTrackedFilesMap();
        for (String filename: filenames) {
            if (!trackedFilesMap.containsKey(filename)) {
                System.out.println("File does not exist in that commit.");
                return;
            }

            String blobHash = trackedFilesMap.get(filename);
            String content = readContentsAsString(getFileByHash(blobHash));
            writeContents(join(CWD, filename), content);
        }
    }

    private static void checkoutSpecificCommit(String commitHash) {
        File commitFile = getFileByHash(commitHash);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        // If a working file is untracked in the current branch and would be overwritten by the checkout
        if (hasUntrackedFileInTheWay(commitHash)) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
        // Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
        TreeMap<String, String> currentBranchTrackedFilesMap = getHeadCommit().getTrackedFilesMap();
        TreeMap<String, String> checkedOutCommitTrackedFilesMap = getObjectByHash(commitHash, Commit.class).getTrackedFilesMap();
        for (String filename: currentBranchTrackedFilesMap.keySet()) {
            if (!checkedOutCommitTrackedFilesMap.containsKey(filename)) {
                restrictedDelete(join(CWD, filename));
            }
        }
        checkoutFilesOnSpecificCommit(commitHash, checkedOutCommitTrackedFilesMap.keySet().toArray(new String[0]));
        // Clear staging area.
        StagingArea stagingArea = readObject(INDEX, StagingArea.class);
        stagingArea.clear();
        writeObject(INDEX, stagingArea);
    }

    public static void checkoutBranch(String branchName) {
        File branchHeadFile = join(BRANCHES_DIR, branchName);
        if (!branchHeadFile.exists()) {
            System.out.println("No such branch exists.");
            return;
        }

        if (isCurrentBranch(branchName)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        checkoutSpecificCommit(readContentsAsString(branchHeadFile));
        // Update HEAD
        writeContents(HEAD, branchName);
    }

    private static boolean hasUntrackedFileInTheWay(String commitHash) {
        List<String> untrackedFilenames = getUntrackedFilenameList();
        TreeMap<String, String> checkedOutBranchTrackedFilesMap = getObjectByHash(commitHash, Commit.class).getTrackedFilesMap();
        for (String filename: untrackedFilenames) {
            if (checkedOutBranchTrackedFilesMap.containsKey(filename)) {
                return true;
            }
        }
        return false;
    }
    public static void branch(String branchName) {
        File newBranchHeadFile = join(BRANCHES_DIR, branchName);
        if (newBranchHeadFile.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        File  currentBranchHeadFile = getCurrentBranchHeadFile();
        writeContents(newBranchHeadFile, readContentsAsString(currentBranchHeadFile));
    }

    public static void rmBranch(String branchName) {
        File branchHeadFile = join(BRANCHES_DIR, branchName);
        if (!branchHeadFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        if (isCurrentBranch(branchName)) {
            System.out.println("Cannot remove the current branch.");
        }

        branchHeadFile.delete();
    }

    /**
     * Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit.
     * Also moves the current branch’s head to that commit node.
     *
     * The command is essentially checkout of an arbitrary commit that also changes the current branch head.
     * */
    public static void reset(String commitHash) {
        checkoutSpecificCommit(commitHash);
        File branchHeadFile = getCurrentBranchHeadFile();
        writeContents(branchHeadFile, commitHash);
    }

    public static void merge(String branchName) {
        StagingArea stagingArea = readObject(INDEX, StagingArea.class);
        TreeMap<String, String> stagingForAdditionMap = stagingArea.filesForAdditionMap;
        TreeMap<String, String> stagingForDeletionMap = stagingArea.filesForDeletionMap;
        if (!stagingForAdditionMap.isEmpty() || !stagingForDeletionMap.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        File targetBranchHeadFile = join(BRANCHES_DIR, branchName);
        if (!targetBranchHeadFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        if (isCurrentBranch(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        File currentBranchHeadFile = getCurrentBranchHeadFile();
        String targetBranchHeadHash = readContentsAsString(targetBranchHeadFile);
        String currentHeadCommitHash = readContentsAsString(currentBranchHeadFile);
        HashSet<String> currentBranchCommitHashSet = traversal(currentHeadCommitHash);
        if (currentBranchCommitHashSet.contains(targetBranchHeadHash)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        HashSet<String> targetBranchCommitHashSet = traversal(targetBranchHeadHash);
        if (targetBranchCommitHashSet.contains(currentHeadCommitHash)) {
            // Move HEAD branch to target commit
            checkoutBranch(branchName);
            writeContents(currentBranchHeadFile, targetBranchHeadHash);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        if (hasUntrackedFileInTheWay(targetBranchHeadHash)) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            return;
        }
        // Find LCA
        String splitCommitHash;
        Commit currentCommit = getHeadCommit();
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(currentHeadCommitHash);
        while (true) {
            String currentCommitHash = queue.pop();
            Commit commit = getObjectByHash(currentCommitHash, Commit.class);
            String firstParentHash = commit.getFirstParentHash();
            String secondParentHash = commit.getSecondParentHash();

            if (targetBranchCommitHashSet.contains(firstParentHash)) {
                splitCommitHash = firstParentHash;
                break;
            }
            if (targetBranchCommitHashSet.contains(secondParentHash)) {
                splitCommitHash = secondParentHash;
                break;
            }

            if (firstParentHash != null) {
                queue.add(firstParentHash);
            }

            if (secondParentHash != null) {
                queue.add(secondParentHash);
            }
        }
        Commit splitCommit = getObjectByHash(splitCommitHash, Commit.class);
        Commit targetCommit = getObjectByHash(targetBranchHeadHash, Commit.class);
        TreeMap<String, String> splitCommitTrackedFilesMap = splitCommit.getTrackedFilesMap();
        TreeMap<String, String> currentCommitTrackedFilesMap = currentCommit.getTrackedFilesMap();
        TreeMap<String, String> targetCommitTrackedFilesMap = targetCommit.getTrackedFilesMap();

        HashSet<String> allInvolvedFilenames = new HashSet<>();
        allInvolvedFilenames.addAll(splitCommitTrackedFilesMap.keySet());
        allInvolvedFilenames.addAll(currentCommitTrackedFilesMap.keySet());
        allInvolvedFilenames.addAll(targetCommitTrackedFilesMap.keySet());

        boolean hasConflict = false;

        for (String filename: allInvolvedFilenames) {
            boolean isFilePresentInTargetBranch =  targetCommitTrackedFilesMap.containsKey(filename);
            boolean isFilePresentInSplitCommit = splitCommitTrackedFilesMap.containsKey(filename);
            boolean isFilePresentInCurrentBranch = currentCommitTrackedFilesMap.containsKey(filename);

            String blobHashInSplitPoint = splitCommitTrackedFilesMap.get(filename);
            String blobHashInCurrentBranch = currentCommitTrackedFilesMap.get(filename);
            String blobHashInTargetBranch = targetCommitTrackedFilesMap.get(filename);


            boolean isFileModifiedInCurrentBranch = isFilePresentInSplitCommit != isFilePresentInCurrentBranch || (isFilePresentInSplitCommit && !blobHashInSplitPoint.equals(blobHashInCurrentBranch));
            boolean isFileModifiedInTargetBranch = isFilePresentInSplitCommit != isFilePresentInTargetBranch || (isFilePresentInSplitCommit && !blobHashInSplitPoint.equals(blobHashInTargetBranch));


            if (isFileModifiedInTargetBranch && !isFileModifiedInCurrentBranch) {
                if (!isFilePresentInTargetBranch) {
                    // remove
                    rm(filename);
                } else {
                    checkoutFileOnSpecificCommit(targetBranchHeadHash, filename);
                    add(filename);
                }
            } else if (isFileModifiedInCurrentBranch && isFileModifiedInTargetBranch) {
                // In different way
                if (!isFilePresentInCurrentBranch && !isFilePresentInTargetBranch) {
                    continue;
                }

                if (!isFilePresentInSplitCommit && blobHashInCurrentBranch.equals(blobHashInTargetBranch)) {
                    continue;
                }

                String contentsOfFileInCurrentBranch = blobHashInCurrentBranch == null ? "" : readContentsAsString(getFileByHash(blobHashInCurrentBranch));
                String contentsOfFileInTargetBranch = blobHashInTargetBranch ==  null ? "" : readContentsAsString(getFileByHash(blobHashInTargetBranch));
                writeContents(join(CWD, filename), "<<<<<<< HEAD\n" + contentsOfFileInCurrentBranch + "=======\n" + contentsOfFileInTargetBranch + ">>>>>>>\n");
                add(filename);
                hasConflict = true;
            }
        }

        String currentBranchName = readContentsAsString(HEAD);
        commit("Merged " + branchName + " into " + currentBranchName+ ".", targetBranchHeadHash);
        writeContents(targetBranchHeadFile, readContentsAsString(currentBranchHeadFile));
        if (hasConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }
}
