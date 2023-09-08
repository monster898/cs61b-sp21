package gitlet;

import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Hao Chen
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        checkArgs(args);
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Repository.init();
                break;
            case "add":
                Repository.add(args[1]);
                break;
            case "commit":
                Repository.commit(args[1], null);
                break;
            case "rm":
                Repository.rm(args[1]);
                break;
            case "log":
                Repository.log();
                break;
            case "global-log":
                Repository.globalLog();
                break;
            case "find":
                Repository.find(args[1]);
                break;
            case "status":
                Repository.status();
                break;
            case "checkout":
                if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else if (args.length == 3) {
                    Repository.checkoutFileOnHeadCommit(args[2]);
                } else if (args.length == 4) {
                    Repository.checkoutFileOnSpecificCommit(args[1], args[3]);
                }
                break;
            case "branch":
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                Repository.reset(args[1]);
                 break;
            case "merge":
                Repository.merge(args[1]);
                break;
        }
    }

    private static void checkArgs(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String[] allCommands = {"init", "add", "commit", "rm", "log", "global-log", "find", "status",
            "checkout", "branch", "rm-branch", "reset", "merge"};
        String command = args[0];
        if (!Arrays.asList(allCommands).contains(command)) {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }

        if (command.equals("init") && Repository.GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        if (!command.equals("init") && !Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        boolean valid = true;
        switch (command) {
            case "init":
            case "log":
            case "global-log":
            case "status":
                valid = args.length == 1;
                break;
            case "add":
            case "commit":
            case "rm":
            case "find":
            case "branch":
            case "rm-branch":
            case "reset":
            case "merge":
                valid = args.length == 2;
                break;
            case "checkout":
                if (args.length > 4) {
                    valid = false;
                } else if (args.length == 3) {
                    valid = Objects.equals(args[1], "--");
                } else if (args.length == 4) {
                    valid = Objects.equals(args[2], "--");
                }
                break;
        }

        if (!valid) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
