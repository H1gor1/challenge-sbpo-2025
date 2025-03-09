package org.sbpo2025.challenge.Genetic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;

public class CrossOpManager<Op> {
    private static final Random RANDOM = new Random();

    private final List<Op> operators;
    private final HashMap<Integer, List<Double>> opProbabilities = new HashMap<>();
    private final List<Double> defaultProbabilities;
    private Pair<Integer, Integer> lastReturned = null;

    public CrossOpManager(List<Op> operators) {
        this.operators = operators;
        this.defaultProbabilities = IntStream.range(0, operators.size())
                .mapToDouble(k -> (1.0 / operators.size()))
                .boxed().collect(Collectors.toList());
    }

    private TreeMap<Double, Integer> getProbMap(int state) {
        List<Double> probList = opProbabilities.computeIfAbsent(state, k -> new ArrayList<>(defaultProbabilities));

        double Vsum = probList.stream().mapToDouble(Double::doubleValue).sum();
        final double epsilon = 1e-9;
        boolean isNormalized = Math.abs(Vsum - 1.0) < epsilon;

        List<Double> normalizedProbList = isNormalized 
                ? probList 
                : probList.stream().map(p -> p / Vsum).collect(Collectors.toList());

        TreeMap<Double, Integer> map = new TreeMap<>();
        double accumulated = 0.0;
        for (int i = 0; i < normalizedProbList.size(); i++) {
            accumulated += normalizedProbList.get(i);
            map.put(accumulated, i);
        }
        return map;
    }

    public Op getOperator(int state) {
        TreeMap<Double, Integer> probMap = getProbMap(state);
        double value = RANDOM.nextDouble();
        int opIndex = probMap.higherEntry(value).getValue();
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
