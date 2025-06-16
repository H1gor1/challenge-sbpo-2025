package org.sbpo2025.challenge.Genetic.CrossOverOperators;

import java.util.Random;


public class UniformCrossOver implements CrossOverOp{

    @Override
    public double[] makeCrossOver(double[] bestParent, double[] worstParent, double pBetterParent, Random random) {
        double[] child = new double[bestParent.length];
        for(int i = 0; i < bestParent.length; i++){
            if(random.nextDouble() < pBetterParent){
                child[i] = bestParent[i];
            }else{
                child[i] = worstParent[i];
            }
        }
        return child;
    }
}
