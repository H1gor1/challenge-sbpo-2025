package org.sbpo2025.challenge.brkga_decoders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.ProblemData;

public class AisleOrderDecoder implements Decoder {

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
    private Pair<List<Integer>, List<Integer>> 
    calcEvaluatingOrder(List<Double> keys, ProblemData instanceData) {
        
        ArrayList<Pair<Double, Integer>> orderKeys = makeKeyIndexList(
            keys.subList(0, instanceData.orders().size())
        );
        ArrayList<Pair<Double, Integer>> aisleKeys = makeKeyIndexList(
            keys.subList(instanceData.orders().size(), keys.size())
        );

        orderKeys.sort(Comparator.comparingDouble(Pair::getLeft));
        aisleKeys.sort(Comparator.comparingDouble(Pair::getLeft));

        return Pair.of(
            orderKeys.stream().map(Pair::getRight).toList(),
            aisleKeys.stream().map(Pair::getRight).toList()
        );
    }

    /**
     * Checks if an order can be served with the current quantity of items from all currently used aisles.
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
            QuantItens[i] += instanceData.aisles().get(newAisle).get(i);
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
        Pair<List<Integer>, List<Integer>> evaluatingOrder = calcEvaluatingOrder(
            keys, instanceData
        );
        List<Integer> orderKeys = evaluatingOrder.getLeft();
        List<Integer> aisleKeys = evaluatingOrder.getRight();

        HashSet<Integer> orderResp = new HashSet<>();
        HashSet<Integer> aisleResp = new HashSet<>(List.of(aisleKeys.get(0)));
        int itensSum=0;

        int first_aisle = aisleKeys.get(0);
        int[] QuantItens = instanceData.aisles().get(first_aisle).values()
                                  .stream()
                                  .mapToInt(Integer::intValue)
                                  .toArray();

        int currentOrder;
        int orderItensSum;
        double foAt = 0;
        int newAisle;
        for(int i = 0; i < orderKeys.size(); i++){
            currentOrder = orderKeys.get(i);
            orderItensSum = instanceData.orders().get(currentOrder).values().stream().mapToInt(Integer::intValue).sum();

            // if add this order to the response will exceed the wave upper bound constraint, then we skip it
            if ( itensSum + orderItensSum > instanceData.waveSizeUB()){
                continue;
            }
            // if the order is currently servable, then we add it to the response
            if ( isOrderServable(currentOrder, QuantItens, instanceData) ){
                orderResp.add(currentOrder);
                updateQuantItens(currentOrder, QuantItens, instanceData);
                itensSum += instanceData.orders().get(currentOrder).values().stream().mapToInt(Integer::intValue).sum();
                foAt = itensSum/(double)aisleResp.size();
                continue;
            }
            //otherwise, we need to check if we can find a feasible aisle to serve the order
            // This condition checks whether adding a new aisle is beneficial to make the current order servable,
            // but only if the current item quantity satisfies the lower bound constraint.
            if ((itensSum + orderItensSum) / (double)(aisleResp.size() + 1) < foAt && itensSum >= instanceData.waveSizeLB()) {
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
        assert itensSum >= instanceData.waveSizeLB();
        return new ChallengeSolution(
            orderResp,
            aisleResp
        );
    }
}