package org.sbpo2025.challenge.Genetic.MutationOperators;

import java.util.List;

public interface MutationInterface {

    /**
     * Should make a mutation in the random keys accordance to the mutation rate.
     * @param randomKeys The random keys that should be mutated.
     * @param mutationRate The mutation rate that should be used to make the mutation.
     */
    public void makeMutation(List<Double> randomKeys, Double mutationRate);
}
