package deque;

import org.junit.Test;

import java.util.Comparator;
import java.util.Optional;

import static org.junit.Assert.*;

public class MaxArrayDequeTest {
    private static class NormalComparator<T> implements Comparator<T> {
        @Override
        public int compare(T o1, T o2) {
            if (o1 == o2) {
                return 0;
            } else if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }

            return (int)o1 - (int)o2;
        }
    }

    private static class ReversedComparator<T> implements Comparator<T> {
        @Override
        public int compare(T o1, T o2) {
            if (o1 == o2) {
                return 0;
            } else if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }
            return (int)o2 - (int)o1;
        }
    }
    @Test
    public void testMaxArrayDeque() {
        Comparator<Integer> normalComparator = new NormalComparator<>();
        Comparator<Integer> reversedComparator = new ReversedComparator<>();
        MaxArrayDeque<Integer> deque = new MaxArrayDeque<>(normalComparator);
        for (int i = 0; i <= 100; i += 2) {
            deque.addLast(i);
        }
        int max = deque.max();
        int min = deque.max(reversedComparator);
        assertEquals(max,100);
        assertEquals(min,0);
    }
}
