package org.sbpo2025.challenge.brkga_decoders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.ProblemData;

public class aisleOrderDecoder implements Decoder{

    @Override
    public int getRKeysSize(ProblemData instanceData){
        return instanceData.aisles().size() + instanceData.orders().size();
    }

    private ArrayList<Pair<Double, Integer>> makeKeyIndexList(List<Double> keys){
        ArrayList<Pair<Double, Integer>> keyIndexList = new ArrayList<>(keys.size());
        for(int i = 0; i < keys.size(); i++){
            keyIndexList.add(Pair.of(keys.get(i), i));
        }
        return keyIndexList;
    }

    /**
     * Builds the list of orders and aisles that should be used to construct the solution 
     * from the given random keys.
     *
     * @param keys The random keys that are being decoded.
     * @param instanceData The data of the current problem instance provided for solving it.
     * @return A pair of lists, where the first list represents the order in which the 
     *         orders will be evaluated, and the second list represents the order in which 
     *         the aisles will be evaluated.
     */
    private Pair<ArrayList<Pair<Double, Integer>>, ArrayList<Pair<Double, Integer>>> 
    calcEvaluatingOrder(List<Double> keys, ProblemData instanceData) {
        
        ArrayList<Pair<Double, Integer>> orderKeys = makeKeyIndexList(
            keys.subList(0, instanceData.orders().size())
        );
        ArrayList<Pair<Double, Integer>> aisleKeys = makeKeyIndexList(
            keys.subList(instanceData.orders().size(), keys.size())
        );

        orderKeys.sort(Comparator.comparingDouble(Pair::getLeft));
        aisleKeys.sort(Comparator.comparingDouble(Pair::getLeft));

        return Pair.of(orderKeys, aisleKeys);
    }

    /**
     * Check if an order can be served with the current quantity of items of the all current aisles used.
     * @param orderIndex The index of the order to be checked.
     * @param QuantItens The vetor that contains the sum of all items of the aisles that still was not used
     * @param instanceData The data of the current problem instance provided for solving it.
     * @return True if the order can be served with the current aisles in use, false otherwise.
     */
    private boolean isOrderServable(int orderIndex, int[] QuantItens, ProblemData instanceData){

        for(int i = 0; i<instanceData.nItems(); i++){
            if ( QuantItens[i] < instanceData.orders().get(orderIndex).get(i)) {
                return false;
            }
        }
        return true;
        
    }

    private void updateQuantItens(int orderIndex, int[] QuantItens, ProblemData instanceData){
        for(int i = 0; i<instanceData.nItems(); i++){
            QuantItens[i] -= instanceData.orders().get(orderIndex).get(i);
        }
    }
    private void updateQuantItens(int orderIndex, int[] QuantItens, ProblemData instanceData, int newAisle){
        for(int i = 0; i<instanceData.nItems(); i++){
            QuantItens[i] -= instanceData.orders().get(orderIndex).get(i);
            QuantItens[i] += instanceData.orders().get(newAisle).get(i);
        }
    }

    private int findFeasibleAisle(
        int orderIndex, 
        int[] quantItens,
        HashSet<Integer> aisleResp, 
        ProblemData instanceData
    ) {
        boolean isFeasible;
        for (int iAisle = 0; iAisle < instanceData.aisles().size(); iAisle++) {
            if ( aisleResp.contains(iAisle) ){
                continue;
            }
            final int lambdaAisleIndex = iAisle;
            isFeasible = IntStream.range(0, quantItens.length).allMatch(
                i -> {
                    return quantItens[i] + instanceData.aisles().get(lambdaAisleIndex).get(i) >= instanceData.orders().get(orderIndex).get(i);
                }
            );
            if ( isFeasible ){
                return iAisle;
            }
        }
        return -1;
    }

    @Override
    public ChallengeSolution decode(List<Double> keys, ProblemData instanceData){
        Pair<ArrayList<Pair<Double, Integer>>, ArrayList<Pair<Double, Integer>>> evaluatingOrder = calcEvaluatingOrder(
            keys, instanceData
        );
        ArrayList<Pair<Double, Integer>> orderKeys = evaluatingOrder.getLeft();
        ArrayList<Pair<Double, Integer>> aisleKeys = evaluatingOrder.getRight();

        HashSet<Integer> orderResp = new HashSet<>();
        HashSet<Integer> aisleResp = new HashSet<>(List.of(aisleKeys.get(0).getRight()));
        int itensSum=0;

        int first_aisle = orderKeys.get(0).getRight();
        int[] QuantItens = instanceData.orders().get(first_aisle).values()
                                  .stream()
                                  .mapToInt(Integer::intValue)
                                  .toArray();

        int currentOrder;
        int orderItensSum;
        double foAt = 0;
        int newAisle;
        for(int i = 0; i < orderKeys.size(); i++){
            currentOrder = orderKeys.get(i).getRight();
            orderItensSum = instanceData.orders().get(currentOrder).values().stream().mapToInt(Integer::intValue).sum();

            // if the order is currently servable, then we add it to the response
            if ( isOrderServable(currentOrder, QuantItens, instanceData) ){
                orderResp.add(currentOrder);
                updateQuantItens(currentOrder, QuantItens, instanceData);
                itensSum += instanceData.orders().get(currentOrder).values().stream().mapToInt(Integer::intValue).sum();
                continue;
            }
            //otherwise, we need to check if we can find a feasible aisle to serve the order
            if ( (itensSum + orderItensSum)/(double)(aisleResp.size() + 1) < foAt){
                continue;
            }
            newAisle = findFeasibleAisle(currentOrder, QuantItens, aisleResp, instanceData);
            if (newAisle == -1){
                continue;
            }
            aisleResp.add(newAisle);
            orderResp.add(currentOrder);
            updateQuantItens(currentOrder, QuantItens, instanceData, newAisle);
            itensSum += orderItensSum;
            foAt = (foAt + itensSum)/aisleResp.size();
        }

        return new ChallengeSolution(
            orderResp,
            aisleResp
        );
    }
}