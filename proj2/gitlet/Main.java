package gitlet;

import java.lang.reflect.Array;
import java.util.Arrays;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Hao Chen
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        checkArgs(args);
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                break;
                // TODO: FILL THE REST IN
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

        if (!command.equals("init") && !Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        switch (command) {
            case "init":
                if (args.length != 1) {

                }
                break;
            case "add":


        }

        if (notValid) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
