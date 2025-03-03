package org.sbpo2025.challenge.Genetic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;

public class OpManager<Op> {

    final private List<Op> operators;
    final private HashMap<Integer, List<Double>> opProbabilities = new HashMap<>();
    final private List<Double> defaultProbabilities;
    private Pair<Integer, Integer> lastReturned = null;

    public OpManager(List<Op> operators) {
        this.operators = operators;
        this.defaultProbabilities = IntStream.range(0, operators.size())
                .mapToDouble(k -> (1.0 / operators.size()))
                .boxed().collect(Collectors.toList());
    }

    private int findBoundedIndex(List<Double> probList, Double value) {
        int i = 0;
        int j = probList.size() - 1;
        int half;
        while (i < j) {
            half = (i + j) / 2;
            if (probList.get(half) < value) {
                i = half + 1;
            } else if (probList.get(half) > value) {
                j = half - 1;
            } else {
                return half;
            }
        }
        return i;
    }

    private List<Double> getProbArray(int state) {
        List<Double> probList = opProbabilities.get(state);
        if (probList == null) {
            probList = new ArrayList<>(defaultProbabilities);
            opProbabilities.put(state, probList);
        }

        Double Vsum = probList.stream().reduce(0.0, (a, b) -> a + b);
        List<Double> normalizedProbList = probList.stream().map(p -> p / Vsum).collect(Collectors.toList());
        
        List<Double> probVector = new ArrayList<>(List.of(0.0));
        for (Double prob : normalizedProbList) {
            probVector.add(probVector.get(probVector.size() - 1) + prob);
        }
        probVector.set(probVector.size() - 1, 1.0);
        return probVector;
    }

    public Op getOperator(int state) {
        List<Double> probList = getProbArray(state);
        Random random = new Random();
        double value = random.nextDouble();
        int opIndex = findBoundedIndex(probList, value);
        lastReturned = Pair.of(state, opIndex);
        return operators.get(opIndex);
    }

    public void feedBack(Double improvementRate) throws RuntimeException {
        if (lastReturned == null) {
            throw new RuntimeException("No operator was returned yet");
        }

        List<Double> probList = opProbabilities.get(lastReturned.getLeft());
        int index = lastReturned.getRight();

        double currentProb = probList.get(index);
        probList.set(index, currentProb * improvementRate);
    }
}
