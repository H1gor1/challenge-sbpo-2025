package org.sbpo2025.challenge.Genetic.BrkgaDecoders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.ProblemData;

public abstract class Decoder {

    /**
     * Returns the number of random keys that the decoder needs to work.
     * 
     * The number of keys can be calculated based on the instance data.
     */
    abstract public int getRKeysSize(ProblemData instanceData);


    /**
     * Receives an array of random keys and decodes them into an instance of ChallengeSolution.
     * 
     * @param keys An array of random keys, each in the range [0, 1].
     * @param instanceData The data of the current problem instance that was provided to solve it.
     * @return An instance of ChallengeSolution representing a valid solution to the problem.
     */
    abstract protected ChallengeSolution performDecode(List<Double> keys, ProblemData instanceData);

    public ChallengeSolution decode(List<Double> keys, ProblemData instanceData){
        if ( keys.size() != getRKeysSize(instanceData) ){
            throw new IllegalArgumentException("The number of keys is not the expected.");
        }
        return performDecode(keys, instanceData);
    }

    protected  ArrayList<Pair<Double, Integer>> makeKeyIndexList(List<Double> keys){
        ArrayList<Pair<Double, Integer>> keyIndexList = new ArrayList<>(keys.size());
        for(int i = 0; i < keys.size(); i++){
            keyIndexList.add(Pair.of(keys.get(i), i));
        }
        return keyIndexList;
    }

    protected boolean isOrderServable(int orderIndex, int[] QuantItens, ProblemData instanceData){
        for (Map.Entry<Integer, Integer> kv : instanceData.orders().get(orderIndex).entrySet()){
            if (QuantItens[kv.getKey()] < kv.getValue()){
                return false;
            }
        }
        return true;
    }

    protected void updateQuantItens(int orderIndex, int[] QuantItens, ProblemData instanceData){
        for(Map.Entry<Integer, Integer> kv : instanceData.orders().get(orderIndex).entrySet()){
            QuantItens[kv.getKey()] -= kv.getValue();
        }
    }

    protected void updateQuantItens(int orderIndex, int[] QuantItens, ProblemData instanceData, int newAisle){
        for( Map.Entry<Integer, Integer> kv : instanceData.aisles().get(newAisle).entrySet()){
            QuantItens[kv.getKey()] += kv.getValue();
        }
        for ( Map.Entry<Integer, Integer> kv : instanceData.orders().get(orderIndex).entrySet()){
            QuantItens[kv.getKey()] -= kv.getValue();
        }
    }
    protected void updateQuantItens(int[] QuantItens, ProblemData instanceData, int newAisle){
        for(Map.Entry<Integer, Integer> kv : instanceData.aisles().get(newAisle).entrySet()){
            QuantItens[kv.getKey()] += kv.getValue();
        }
    }
}
