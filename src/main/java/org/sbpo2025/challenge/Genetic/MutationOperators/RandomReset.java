package org.sbpo2025.challenge.Genetic.MutationOperators;

import java.util.List;
import java.util.Random;

public class RandomReset implements MutationOp {

    final private int diversityLevel;

    public RandomReset(int diversityLevel) {
        this.diversityLevel = diversityLevel;
    }

    @Override
    public void makeMutation(List<Double> randomKeys, Random random) {
        int target;
        for (int i = 0; i < diversityLevel; i++) {
            target = random.nextInt(randomKeys.size());
            randomKeys.set(target, random.nextDouble());
        }
    }
}
