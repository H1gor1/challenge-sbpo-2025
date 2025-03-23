package org.sbpo2025.challenge.Genetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ProbabilityWheel {

    private final Random RANDOM;
    private final List<Double> pList;

    /**
     * This method receives a probability distribution and builds a list of 
     * accumulated probabilities to simulate a probability wheel.
     * @param probDist The probability distribution to be used in the wheel
     * @return A list of accumulated probabilities
     */
    private List<Double> makeProbMap(List<Double> probDist) {
        double Vsum = probDist.stream().reduce(0.0, (a, b) -> a + b);
        boolean isNormalized = Double.compare(Vsum, 1.0) == 0;

        List<Double> normalizedProb = isNormalized
            ? probDist
            : probDist.stream().map(p -> p / Vsum).collect(Collectors.toList());

        List<Double> probList = new ArrayList<>();
        double accumulated = 0.0;
        for (double prob : normalizedProb) {
            accumulated += prob;
            probList.add(accumulated);
        }
        return probList;
    }

    public ProbabilityWheel(List<Double> probDist, Random random) {
        this.pList = makeProbMap(probDist);
        this.RANDOM = random;
    }

    /**
     * Returns a randomly selected index based on the probability distribution 
     * given in the constructor of the class.
     * @return the selected index
     */
    public int get() {
        double value = RANDOM.nextDouble();
        int idx = Collections.binarySearch(pList, value);

        /* This conversion is needed because when the value doesn't exist in the list,  
        the binarySearch method returns `-insertionPoint - 1`, which is a negative number.  
        So, we need to extract the insertion point from the returned result:  

        x = -insertionPoint - 1  
        x + 1 = -insertionPoint  
        -x - 1 = insertionPoint  
        */
        if (idx < 0) {
            idx = -idx - 1;
        }
        return idx;
    }
}
