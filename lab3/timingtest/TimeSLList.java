package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        SLList<Number> listForTest = new SLList<>();
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        ArrayList<Number> checkPoints = new ArrayList<>(Arrays.asList(1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000));

        for (int i = 0; i <= 128000; i++) {
            listForTest.addLast(0);
            if (checkPoints.contains(i)) {
                // perform test
                Stopwatch sw = new Stopwatch();
                for (int j = 0; j < 1000; j++) {
                    listForTest.getLast();
                }
                Double timeNowInSeconds = sw.elapsedTime();
                Ns.addLast(i);
                times.addLast(timeNowInSeconds);
                opCounts.addLast(1000);
            }
        }
        printTimingTable(Ns, times, opCounts);
    }

}
