package org.sbpo2025.challenge.Genetic.MutationOperators;

import java.util.List;
import java.util.Random;

public class CreepMutation implements MutationOp {

    final private double diversityLevel;

    public CreepMutation(double diversityLevel) {
        this.diversityLevel = diversityLevel;
    }

    @Override
    public void makeMutation(List<Double> randomKeys, Random random) {
        int target;
        int sizeMutation = (int) (diversityLevel * randomKeys.size());
        sizeMutation = Math.max(sizeMutation, 1);
        for (int i = 0; i < sizeMutation; i++) {
            target = random.nextInt(randomKeys.size());
            randomKeys.set(target, randomKeys.get(target) + random.nextDouble(-1, 2));
            if ( randomKeys.get(target) < 0.0){
                randomKeys.set(target, 1.0+randomKeys.get(target));
            }else if ( randomKeys.get(target) > 1.0){
                randomKeys.set(target, randomKeys.get(target)-1.0);
            }
        }
    }
}
