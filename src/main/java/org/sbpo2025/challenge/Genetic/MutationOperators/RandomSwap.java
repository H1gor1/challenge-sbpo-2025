package org.sbpo2025.challenge.Genetic.MutationOperators;

import java.util.List;
import java.util.Random;

public class RandomSwap implements MutationOp{

    final private int diversityLevel;
    public RandomSwap(int diversityLevel) {
        this.diversityLevel = diversityLevel;
    }

    @Override
    public void makeMutation(List<Double> randomKeys, Random random) {
        int source;
        int target;
        for (int i = 0; i < diversityLevel; i++) {
            source = random.nextInt(randomKeys.size());
            target = random.nextInt(randomKeys.size());
            double temp = randomKeys.get(source);
            randomKeys.set(source, randomKeys.get(target));
            randomKeys.set(target, temp);
        }
    }
}
