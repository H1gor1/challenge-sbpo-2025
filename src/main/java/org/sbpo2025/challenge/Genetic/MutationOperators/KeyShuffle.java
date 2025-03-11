package org.sbpo2025.challenge.Genetic.MutationOperators;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class KeyShuffle implements MutationOp{

    final private double diversityLevel;
    public KeyShuffle(double diversityLevel) {
        this.diversityLevel = diversityLevel;
    }

    @Override
    public void makeMutation(List<Double> randomKeys, Random random) {
        int sizeMutation = (int) (diversityLevel * randomKeys.size());
        sizeMutation = Math.max(sizeMutation, 3);
        int start = random.nextInt(randomKeys.size());
        int end = random.nextInt(
            start, 
            (start + sizeMutation > randomKeys.size()) 
                ? randomKeys.size() 
                : start + sizeMutation
        );
        List<Double> subList = randomKeys.subList(start, end);
        Collections.shuffle(subList);
    }
}
