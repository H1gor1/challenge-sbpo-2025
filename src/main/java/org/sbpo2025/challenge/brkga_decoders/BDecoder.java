import org.sbpo2025.ChallengeSolution;

interface BDecoder {
    /**
     * Receives an array of random keys and decodes them into an instance of ChallengeSolution.
     * 
     * @param keys An array of random keys, each in the range [0, 1].
     * @return An instance of ChallengeSolution representing a valid solution to the problem.
     */
    abstract ChallengeSolution decode(double[] keys);
}
