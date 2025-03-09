package org.sbpo2025.challenge.Genetic.MutationOperators;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Shift implements MutationOp {

    private final double diversityLevel;

    public Shift(double diversityLevel) {
        this.diversityLevel = diversityLevel;
    }

    @Override
    public void makeMutation(List<Double> randomKeys, Random random) {
        int sizeMutation = (int) (diversityLevel * randomKeys.size());
        sizeMutation = Math.max(sizeMutation, 3);

        int start = random.nextInt(randomKeys.size());
        int end = Math.min(start + sizeMutation, randomKeys.size());

        Collections.rotate(randomKeys.subList(start, end), 1);
    }
}
