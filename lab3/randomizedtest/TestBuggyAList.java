package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> correctAList = new AListNoResizing<>();
        BuggyAList<Integer> buggyAList = new BuggyAList<>();

        for (int i = 4; i <= 6; i++) {
            correctAList.addLast((i));
            buggyAList.addLast((i));
        }

        for (int i = 0; i < 3; i++) {
            int x = correctAList.removeLast();
            int y = buggyAList.removeLast();
            assertEquals(x, y);
        }
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> broken = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                correct.addLast(randVal);
                broken.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size1 = correct.size();
                int size2 = broken.size();
            }

            if (correct.size() == 0) continue;

            if (operationNumber == 2) {
                int value1 = correct.getLast();
                int value2 = broken.getLast();
            } else if (operationNumber == 3) {
                int value1 = correct.removeLast();
                int value2 = broken.removeLast();
            }
        }
    }
}
