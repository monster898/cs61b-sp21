package tester;

import org.junit.Test;
import edu.princeton.cs.algs4.StdRandom;
import student.StudentArrayDeque;

import static org.junit.Assert.*;

public class TestArrayDequeEC {
    @Test
    public void randomizedTest() {
        StudentArrayDeque<Integer> studentArrayDeque = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> correctArrayDeque = new ArrayDequeSolution<>();

        int N = 100000;
        String errMessage = "";
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 5);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                studentArrayDeque.addLast(randVal);
                correctArrayDeque.addLast(randVal);
                errMessage += "addLast(" + randVal + ")\n";
            } else if (operationNumber == 1) {
                int randVal = StdRandom.uniform(0, 100);
                studentArrayDeque.addFirst(randVal);
                correctArrayDeque.addFirst(randVal);
                errMessage += "addFirst(" + randVal + ")\n";
            }

            if (studentArrayDeque.isEmpty()) continue;

            if (operationNumber == 2) {
                errMessage += "removeFirst()";
                Integer expected = correctArrayDeque.removeFirst();
                Integer actual = studentArrayDeque.removeFirst();
                assertEquals(errMessage, expected, actual);
                errMessage += "\n";
            } else if (operationNumber == 3) {
                errMessage += "removeLast()";
                Integer expected = correctArrayDeque.removeLast();
                Integer actual = studentArrayDeque.removeLast();
                assertEquals(errMessage, expected, actual);
                errMessage += "\n";
            }
        }
    }
}
