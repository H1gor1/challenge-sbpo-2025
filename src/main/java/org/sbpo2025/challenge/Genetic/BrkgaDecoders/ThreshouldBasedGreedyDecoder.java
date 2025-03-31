package org.sbpo2025.challenge.Genetic.BrkgaDecoders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.ProblemData;

public class ThreshouldBasedGreedyDecoder extends Decoder {

    private final int thresholdKeysQuantity = 2;
    @Override
    public int getRKeysSize(ProblemData instanceData) {
        return thresholdKeysQuantity + instanceData.orders().size() + instanceData.aisles().size();
    }
    @Override
    protected List<List<Integer>> calcEvaluatingOrder(double[] keys, ProblemData instanceData) {

        Double aislesThreshould = keys[0];
        Double ordersThreshould = keys[1];

        int firstOrderIndex = thresholdKeysQuantity;
        int firstAisleIndex = firstOrderIndex + instanceData.orders().size();

        int orderIndex = firstOrderIndex;
        int aisleIndex = firstAisleIndex;

        int ordersFinishAt = firstOrderIndex + instanceData.orders().size();
        int aislesFinishAt = firstAisleIndex + instanceData.aisles().size();

        List<Integer> orderIndexes = new ArrayList<>(instanceData.orders().size());
        List<Integer> aisleIndexes = new ArrayList<>(instanceData.aisles().size());
        while ( orderIndex < ordersFinishAt && aisleIndex < aislesFinishAt ){
            if (keys[orderIndex] < ordersThreshould ){
                orderIndexes.add(orderIndex - firstOrderIndex);
            }
            if (keys[aisleIndex] < aislesThreshould ){
                aisleIndexes.add(aisleIndex - firstAisleIndex);
            }
            orderIndex++;
            aisleIndex++;
        }
        while (orderIndex < ordersFinishAt){
            if (keys[orderIndex] < ordersThreshould ){
                orderIndexes.add(orderIndex - firstOrderIndex);
            }
            orderIndex++;
        }
        while (aisleIndex < aislesFinishAt){
            if (keys[aisleIndex] < aislesThreshould ){
                aisleIndexes.add(aisleIndex - firstAisleIndex);
            }
            aisleIndex++;
        }
        if ( aisleIndexes.isEmpty() ){
            aisleIndexes = List.of(0);
        }
        return List.of(orderIndexes, aisleIndexes, List.of(aisleIndexes.size()));
    }

    private void countIntialItems(
        int[] quantItens,
        List<Integer> aisleIndexes,
        int QAisles,
        ProblemData instanceData,
        HashSet<Integer> aisleResp
    ) {
        List<Map<Integer, Integer>> aisles = instanceData.aisles();

        for (int i = 0; i < QAisles; i++) {
            int aisleIndex = aisleIndexes.get(i);
            Map<Integer, Integer> aisle = aisles.get(aisleIndex);

            for (Map.Entry<Integer, Integer> kv : aisle.entrySet()) {
                quantItens[kv.getKey()] += kv.getValue();
            }

            aisleResp.add(aisleIndex);
        }
    }

    @Override
    protected ChallengeSolution performDecode(List<List<Integer>> evaluationOrder, ProblemData instanceData) {
        List<Integer> orderIndexes = evaluationOrder.get(0);
        List<Integer> aisleIndexes = evaluationOrder.get(1);
        List<Integer> qAisleIndexes = evaluationOrder.get(2);
        Integer QAisles = qAisleIndexes.get(0);

        HashSet<Integer> orderResp = new HashSet<>(instanceData.orders().size());
        HashSet<Integer> aisleResp = new HashSet<>(QAisles);

        int[] QuantItens = new int[instanceData.nItems()];
        countIntialItems(QuantItens, aisleIndexes, QAisles, instanceData, aisleResp);
        
        int itensSum = 0;
        int waveSizeUB = instanceData.waveSizeUB();
        int waveSizeLB = instanceData.waveSizeLB();
        
        for (Integer order : orderIndexes) {
            if (!isOrderServable(order, QuantItens, instanceData)) {
                continue;
            }
            int orderItens = sumOrderItems(order, instanceData);

            if (itensSum + orderItens > waveSizeUB) {
                continue;
            }
            itensSum += orderItens;
            updateQuantItens(order, QuantItens, instanceData);
            orderResp.add(order);
        }

        double fo = (itensSum < waveSizeLB) ? 0.0 : (itensSum / (double) QAisles);

        return new ChallengeSolution(orderResp, aisleResp, fo);
    }

    private int sumOrderItems(Integer order, ProblemData instanceData) {
        int sum = 0;
        Map<Integer, Integer> orderItems = instanceData.orders().get(order);
        for (int qty : orderItems.values()) {
            sum += qty;
        }
        return sum;
    }
}
