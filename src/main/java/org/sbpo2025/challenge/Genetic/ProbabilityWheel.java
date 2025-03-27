package org.sbpo2025.challenge.Genetic;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class ProbabilityWheel <T>{

    private final Random RANDOM;
    private final double[] pList;
    private final List<T> elements;

    /**
     * This method receives a probability distribution and builds a list of 
     * accumulated probabilities to simulate a probability wheel.
     * @param probDist The probability distribution to be used in the wheel
     * @return A list of accumulated probabilities
     */
    private double[] makeProbMap(List<T> elements, Function<T, Double> valueGetter, Double sum) {

        double[] probList = new double[elements.size()];
        double accumulated = 0.0;
        for (int i = 0; i < elements.size(); i++) {
            accumulated += valueGetter.apply(elements.get(i))/sum;
            probList[i] = accumulated;
        }
        assert Double.compare(accumulated, 1.0) == 0;
        return probList;
    }

    public ProbabilityWheel(List<T> elements, Function<T, Double> valueGetter, Random random) {
        Double sum = elements.stream().mapToDouble(valueGetter::apply).sum();
        this.pList = makeProbMap(elements, valueGetter, sum);
        this.elements = elements;
        this.RANDOM = random;
    }

    /**
     * Returns a randomly selected index based on the probability distribution 
     * given in the constructor of the class.
     * @return the selected index
     */
    public T get() {
        double value = RANDOM.nextDouble();
        int idx = Arrays.binarySearch(pList, value);

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
        return elements.get(idx);
    }
}
