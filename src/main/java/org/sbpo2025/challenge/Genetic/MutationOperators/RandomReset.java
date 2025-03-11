package org.sbpo2025.challenge.Genetic.MutationOperators;

import java.util.List;
import java.util.Random;

public class RandomReset implements MutationOp {

    final private double diversityLevel;

    public RandomReset(double diversityLevel) {
        this.diversityLevel = diversityLevel;
    }

    @Override
    public void makeMutation(List<Double> randomKeys, Random random) {
        int target;
        int sizeMutation = (int) (diversityLevel * randomKeys.size());
        sizeMutation = Math.max(sizeMutation, 1);
        for (int i = 0; i < sizeMutation; i++) {
            target = random.nextInt(randomKeys.size());
            randomKeys.set(target, random.nextDouble());
        }
    }
}