package org.sbpo2025.challenge;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.sbpo2025.challenge.Genetic.BrkgaDecoders.Decoder;
import org.sbpo2025.challenge.Genetic.BrkgaDecoders.TripleKeyGreedyDecoder;
import org.sbpo2025.challenge.Genetic.CrossOpManager;
import org.sbpo2025.challenge.Genetic.CrossOverOperators.UniformCrossOver;
import org.sbpo2025.challenge.Genetic.GA;
import org.sbpo2025.challenge.Genetic.MutOpManager;
import org.sbpo2025.challenge.Genetic.MutationOperators.CreepMutation;
import org.sbpo2025.challenge.Genetic.MutationOperators.KeyShuffle;
import org.sbpo2025.challenge.Genetic.MutationOperators.RandomReset;
import org.sbpo2025.challenge.Genetic.MutationOperators.RandomSwap;
import org.sbpo2025.challenge.Genetic.MutationOperators.Reverse;
import org.sbpo2025.challenge.Genetic.MutationOperators.Shift;

public class ChallengeSolver {
    private final long MAX_RUNTIME = 600000; // milliseconds; 10 minutes

    protected List<Map<Integer, Integer>> orders;
    protected List<Map<Integer, Integer>> aisles;
    protected int nItems;
    protected int waveSizeLB;
    protected int waveSizeUB;

    public ChallengeSolver(
            List<Map<Integer, Integer>> orders, List<Map<Integer, Integer>> aisles, int nItems, int waveSizeLB, int waveSizeUB) {
        this.orders = orders;
        this.aisles = aisles;
        this.nItems = nItems;
        this.waveSizeLB = waveSizeLB;
        this.waveSizeUB = waveSizeUB;
    }

    public ChallengeSolution solve(StopWatch stopWatch) {
        ProblemData instanceData = new ProblemData(orders, aisles, nItems, waveSizeLB, waveSizeUB);
        Decoder decoder = new TripleKeyGreedyDecoder();
        GA genetic = new GA(
            decoder,
            300,
            300,
            0.2,
            0.6,
            instanceData,
            new CrossOpManager<>(List.of(new UniformCrossOver())),
            new MutOpManager(
            List.of(
                new RandomReset(0.50),
                new CreepMutation(0.35),
                new Reverse(0.20),
                new KeyShuffle(0.10),
                new Reverse(0.05),
                new RandomSwap(0.02),
                new Shift(0.005),
                new RandomSwap(0.001)
            )
        )


        );
        return genetic.solve();
    }

    /*
     * Get the remaining time in seconds
     */
    protected long getRemainingTime(StopWatch stopWatch) {
        return Math.max(
                TimeUnit.SECONDS.convert(MAX_RUNTIME - stopWatch.getTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS),
                0);
    }

    protected boolean isSolutionFeasible(ChallengeSolution challengeSolution) {
        Set<Integer> selectedOrders = challengeSolution.orders();
        Set<Integer> visitedAisles = challengeSolution.aisles();
        if (selectedOrders == null || visitedAisles == null || selectedOrders.isEmpty() || visitedAisles.isEmpty()) {
            return false;
        }

        int[] totalUnitsPicked = new int[nItems];
        int[] totalUnitsAvailable = new int[nItems];

        // Calculate total units picked
        for (int order : selectedOrders) {
            for (Map.Entry<Integer, Integer> entry : orders.get(order).entrySet()) {
                totalUnitsPicked[entry.getKey()] += entry.getValue();
            }
        }

        // Calculate total units available
        for (int aisle : visitedAisles) {
            for (Map.Entry<Integer, Integer> entry : aisles.get(aisle).entrySet()) {
                totalUnitsAvailable[entry.getKey()] += entry.getValue();
            }
        }

        // Check if the total units picked are within bounds
        int totalUnits = Arrays.stream(totalUnitsPicked).sum();
        if (totalUnits < waveSizeLB || totalUnits > waveSizeUB) {
            return false;
        }

        // Check if the units picked do not exceed the units available
        for (int i = 0; i < nItems; i++) {
            if (totalUnitsPicked[i] > totalUnitsAvailable[i]) {
                return false;
            }
        }

        return true;
    }

    protected double computeObjectiveFunction(ChallengeSolution challengeSolution) {
        Set<Integer> selectedOrders = challengeSolution.orders();
        Set<Integer> visitedAisles = challengeSolution.aisles();
        if (selectedOrders == null || visitedAisles == null || selectedOrders.isEmpty() || visitedAisles.isEmpty()) {
            return 0.0;
        }
        int totalUnitsPicked = 0;

        // Calculate total units picked
        for (int order : selectedOrders) {
            totalUnitsPicked += orders.get(order).values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
        }

        // Calculate the number of visited aisles
        int numVisitedAisles = visitedAisles.size();

        // Objective function: total units picked / number of visited aisles
        return (double) totalUnitsPicked / numVisitedAisles;
    }
}
