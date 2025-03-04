package org.sbpo2025.challenge.Genetic.MutationOperators;

import java.util.List;
import java.util.Random;

public class RandomReset implements MutationOp {

    @Override
    public void makeMutation(List<Double> randomKeys, Random random) {
        int target = random.nextInt(randomKeys.size());
        randomKeys.set(target, random.nextDouble());
    }
}
