package org.sbpo2025.challenge.brkga_decoders;

import java.util.List;

import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.ProblemData;

public interface Decoder {

    /**
     * Returns the number of random keys that the decoder needs to work.
     * 
     * The number of keys can be calculated based on the instance data.
     */
    public int getRKeysSize(ProblemData instanceData);



    /**
     * Receives an array of random keys and decodes them into an instance of ChallengeSolution.
     * 
     * @param keys An array of random keys, each in the range [0, 1].
     * @param instanceData The data of the current problem instance that was provided to solve it.
     * @return An instance of ChallengeSolution representing a valid solution to the problem.
     */
    public ChallengeSolution decode(List<Double> keys, ProblemData instanceData);
}
