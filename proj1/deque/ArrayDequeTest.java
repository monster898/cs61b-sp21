package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArrayDequeTest {

    @Test
    public void randomizedTest() {
        ArrayDeque<Integer> ADeque = new ArrayDeque<>();
        LinkedListDeque<Integer> LLDeque = new LinkedListDeque<>();

        int N = 100000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 5);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                ADeque.addLast(randVal);
                LLDeque.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size1 = ADeque.size();
                int size2 = LLDeque.size();
                assertEquals(size1, size2);
            } else if (operationNumber == 2) {
                int randVal = StdRandom.uniform(0, 100);
                ADeque.addFirst(randVal);
                LLDeque.addFirst(randVal);
            } else if (operationNumber == 3) {
                Integer value1 = ADeque.removeFirst();
                Integer value2 = LLDeque.removeFirst();
                assertEquals(value1, value2);
            } else if (operationNumber == 4) {
                Integer value1 = ADeque.removeLast();
                Integer value2 = LLDeque.removeLast();
                assertEquals(value1, value2);
            }
        }
    }
}
