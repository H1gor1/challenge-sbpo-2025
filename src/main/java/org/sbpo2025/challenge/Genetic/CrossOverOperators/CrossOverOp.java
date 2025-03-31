package org.sbpo2025.challenge.Genetic.CrossOverOperators;

import java.util.Random;

public interface CrossOverOp {

    /**
     * Should make a crossover between two parents, generating a child random key.
     * Should integrate in the your logic the pBetterParent parameter, that is the prodability of
     * the gene of the best parent be in the child.
     * @param bestParent The random keys of the best parent.
     * @param worstFather The random keys of the worst parent. 
     * @param pBetterParent The probability of the gene of the best parent be in the child.
     * @param random The random object to generate random numbers.
     * @return The random keys of the child.
     */
    public double[] makeCrossOver(double[] bestParent, double[] worstParent, double pBetterParent, Random random);
}
