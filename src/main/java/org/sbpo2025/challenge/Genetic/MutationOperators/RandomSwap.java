package org.sbpo2025.challenge.Genetic.MutationOperators;

import java.util.List;
import java.util.Random;

public class RandomSwap implements MutationOp{

    final private double diversityLevel;
    public RandomSwap(double diversityLevel) {
        this.diversityLevel = diversityLevel;
    }

    @Override
    public void makeMutation(List<Double> randomKeys, Random random) {
        int source;
        int target;
        int sizeMutation = (int) (diversityLevel * randomKeys.size());
        sizeMutation = Math.max(sizeMutation, 1);
        for (int i = 0; i < sizeMutation; i++) {
            source = random.nextInt(randomKeys.size());
            target = random.nextInt(randomKeys.size());
            double temp = randomKeys.get(source);
            randomKeys.set(source, randomKeys.get(target));
            randomKeys.set(target, temp);
        }
    }
}