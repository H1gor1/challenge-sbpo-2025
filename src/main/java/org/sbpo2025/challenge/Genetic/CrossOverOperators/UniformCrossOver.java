package org.sbpo2025.challenge.Genetic.CrossOverOperators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class UniformCrossOver implements CrossOverOp{

    @Override
    public List<Double> makeCrossOver(List<Double> bestParent, List<Double> worstParent, Double pBetterParent, Random random) {
        List<Double> child = new ArrayList<>();
        for(int i = 0; i < bestParent.size(); i++){
            if(random.nextDouble() < pBetterParent){
                child.add(bestParent.get(i));
            }else{
                child.add(worstParent.get(i));
            }
        }
        return child;
    }
}
