package org.sbpo2025.challenge.Genetic.BrkgaDecoders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.ProblemData;

public class FirstFeasibleAisleDecoder extends Decoder {

    @Override
    public int getRKeysSize(ProblemData instanceData){
        return instanceData.aisles().size() + instanceData.orders().size();
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
    @Override
    protected List<List<Integer>> 
    calcEvaluatingOrder(List<Double> keys, ProblemData instanceData) {
        
        ArrayList<Pair<Double, Integer>> orderKeys = makeKeyIndexList(
            keys.subList(0, instanceData.orders().size())
        );
        ArrayList<Pair<Double, Integer>> aisleKeys = makeKeyIndexList(
            keys.subList(instanceData.orders().size(), keys.size())
        );

        orderKeys.sort(Comparator.comparingDouble(Pair::getLeft));
        aisleKeys.sort(Comparator.comparingDouble(Pair::getLeft));

        return List.of(
            orderKeys.stream().map(Pair::getRight).toList(),
            aisleKeys.stream().map(Pair::getRight).toList()
        );
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
                    Integer aisleCurrentItens = instanceData.aisles().get(lambdaAisleIndex).get(i);
                    Integer orderCurrentItens = instanceData.orders().get(orderIndex).get(i);
                    aisleCurrentItens = aisleCurrentItens == null ? 0 : aisleCurrentItens;
                    orderCurrentItens = orderCurrentItens == null ? 0 : orderCurrentItens;
                    return quantItens[i] + aisleCurrentItens >= orderCurrentItens;
                }
            );
            if ( isFeasible ){
                return iAisle;
            }
        }
        return -1;
    }

    @Override
    public ChallengeSolution performDecode(List<List<Integer>>  evaluationOrder, ProblemData instanceData){
        List<Integer> orderKeys = evaluationOrder.get(0);
        List<Integer> aisleKeys = evaluationOrder.get(1);

        HashSet<Integer> orderResp = new HashSet<>();
        HashSet<Integer> aisleResp = new HashSet<>(List.of(aisleKeys.get(0)));
        int itensSum=0;

        int first_aisle = aisleKeys.get(0);
        int[] QuantItens = new int[instanceData.nItems()];
        for (Map.Entry<Integer, Integer>  kv : instanceData.aisles().get(first_aisle).entrySet()) {
            QuantItens[kv.getKey()] = kv.getValue();
        }

        int orderItensSum;
        double foAt = 0;
        int newAisle;
        for(int currentOrder : orderKeys){
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
            aisleResp,
            foAt
        );
    }
}