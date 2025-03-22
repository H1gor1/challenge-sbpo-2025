package org.sbpo2025.challenge.Genetic.BrkgaDecoders;

import java.util.List;
import java.util.Map;

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
     * Builds the list of evaluations that should be used to construct the solution
     * @param keys The random keys of the current individual
     * @param instanceData The data of the current problem instance
     * @return A list of evaluations, where each position contains other list with the order of evaluation
     * of the some part of the solution
     */
    abstract protected List<List<Integer>> 
    calcEvaluatingOrder(List<Double> keys, ProblemData instanceData);

    /**
     * Receives an array of random keys and decodes them into an instance of ChallengeSolution.
     * 
     * @param keys An array of random keys, each in the range [0, 1].
     * @param instanceData The data of the current problem instance that was provided to solve it.
     * @return An instance of ChallengeSolution representing a valid solution to the problem.
     */
    abstract protected ChallengeSolution performDecode(List<List<Integer>> evaluationOrder, ProblemData instanceData);

    final public ChallengeSolution decode(List<Double> keys, ProblemData instanceData){
        if ( keys.size() != getRKeysSize(instanceData) ){
            throw new IllegalArgumentException("The number of keys is not the expected.");
        }
        List<List<Integer>> evaluationOrder = calcEvaluatingOrder(keys, instanceData);
        return performDecode(evaluationOrder, instanceData);
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
