package org.sbpo2025.challenge.Genetic.BrkgaDecoders;

import java.util.Map;

import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.ProblemData;

class EvaluationIterator {

    private final int[] elements;
    private final int size;
    private int currentIndex = 0;

    public EvaluationIterator(int[] elements, int size){
        this.elements = elements;
        this.size = size;
    }
    public boolean hasNext(){
        return currentIndex < size;
    }
    public int next(){
        int nextElement = elements[currentIndex];
        currentIndex++;
        return nextElement;
    }

}
class EvaluationOrder {
    /**
     * A instance of this class represent an order of evaluation that should be considered
     * to decode a solution using some constructive heuristic.
     */

    private final int ordersQuantity;
    private final int aislesQuantity;
    private final int[] orderIndexes;
    private final int[] aisleIndexes;
    private int idxOrder;
    private int idxAisle;

    public EvaluationOrder(ProblemData instanceData) {
        this.ordersQuantity = instanceData.orders().size();
        this.aislesQuantity = instanceData.aisles().size();
        this.orderIndexes = new int[ordersQuantity];
        this.aisleIndexes = new int[aislesQuantity];
        this.idxOrder = 0;
        this.idxAisle = 0;
    }

    public void addOrder(int newOrderIndex) {
        orderIndexes[idxOrder] = newOrderIndex;
        idxOrder++;
    }
    public void addAisle(int newAisleIndex) {
        aisleIndexes[idxAisle] = newAisleIndex;
        idxAisle++;
    }

    public int getCurrentAisleQuantity() {
        return idxAisle;
    }
    public int getCurrentOrderQuantity() {
        return idxOrder;
    }

    public EvaluationIterator orderIterator() {
        return new EvaluationIterator(orderIndexes, idxOrder);
    }

    public EvaluationIterator aisleIterator() {
        return new EvaluationIterator(aisleIndexes, idxAisle);
    }

}

public abstract class Decoder {

    /**
     * Returns the number of random keys that the decoder needs to work.
     * 
     * The number of keys can be calculated based on the instance data.
     */
    abstract public int getRKeysSize(ProblemData instanceData);

    /**
     * Builds a evaluation order instance  that means what is the order
     * of evaluation that constructive heuristic should to use to construct the solution
     * on method 'performDecode'
     * @param keys The random keys of the current individual
     * @param instanceData The data of the current problem instance
     * @return An instance of EvaluationOrder
     */
    abstract protected EvaluationOrder
    calcEvaluatingOrder(double[] keys, ProblemData instanceData);

    /**
     * Receives an array of random keys and decodes them into an instance of ChallengeSolution.
     * 
     * @param evaluationOrder The order of evaluation that the constructive heuristic into should
     * consider to build the solution
     * @param instanceData The data of the current problem instance that was provided to solve it.
     * @return An instance of ChallengeSolution representing a valid solution to the problem.
     */
    abstract protected ChallengeSolution performDecode(EvaluationOrder evaluationOrder, ProblemData instanceData);

    final public ChallengeSolution decode(double[] keys, ProblemData instanceData){
        if ( keys.length != getRKeysSize(instanceData) ){
            throw new IllegalArgumentException("The number of keys is not the expected.");
        }
        EvaluationOrder evaluationOrder = calcEvaluatingOrder(keys, instanceData);
        return performDecode(evaluationOrder, instanceData);
    }

    protected boolean isOrderServable(int orderIndex, int[] QuantItens, ProblemData instanceData){
        Map<Integer, Integer> orderItems = instanceData.orders().get(orderIndex);
        for (Map.Entry<Integer, Integer> kv : orderItems.entrySet()){
            if (QuantItens[kv.getKey()] < kv.getValue()){
                return false;
            }
        }
        return true;
    }

    protected void updateQuantItens(int orderIndex, int[] QuantItens, ProblemData instanceData){
        Map<Integer, Integer> orderItems = instanceData.orders().get(orderIndex);
        for(Map.Entry<Integer, Integer> kv : orderItems.entrySet()){
            QuantItens[kv.getKey()] -= kv.getValue();
        }
    }
}
