package org.sbpo2025.challenge.Genetic.MutationOperators;

import java.util.List;
import java.util.Random;

public class Reverse implements MutationOp {

    @Override
    public void makeMutation(List<Double> randomKeys, Random random) {
     
        int start = random.nextInt(randomKeys.size()-1);
        int end = random.nextInt(start+1, randomKeys.size());
        double temp;
        while (start < end ){
            temp = randomKeys.get(start);
            randomKeys.set(start, randomKeys.get(end));
            randomKeys.set(end, temp);
            start++;
            end--;
        }
    }
    
}
