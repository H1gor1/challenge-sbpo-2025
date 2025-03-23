
import java.util.List;
import java.util.Random;

import org.sbpo2025.challenge.Genetic.ProbabilityWheel;

public class TestProdabilityWheel {
    public static void main(String[] args) {
        List<Double> probList = List.of(0.2, 0.5, 0.3);
        ProbabilityWheel pw = new ProbabilityWheel(probList, new Random());
        int idx[] = new int[probList.size()];
        int total = 1000000;

        for (int i = 0; i < total; i++) {
            idx[pw.get()]++;
        }

        boolean result;
        for ( int i = 0; i < probList.size(); i++) {
            result = testProbability(idx[i], total, probList.get(i));
            System.out.println("Teste " + i + ": " + idx[i] + " passou? " + result);
        }
    }

    private static boolean testProbability(int observed, int total, double expectedProb) {
        double expected = total * expectedProb;
        double errorMargin = 1.96 * Math.sqrt((expectedProb * (1 - expectedProb)) / total);
        double lowerBound = expected - (errorMargin * total);
        double upperBound = expected + (errorMargin * total);

        return observed >= lowerBound && observed <= upperBound;
    }
}

