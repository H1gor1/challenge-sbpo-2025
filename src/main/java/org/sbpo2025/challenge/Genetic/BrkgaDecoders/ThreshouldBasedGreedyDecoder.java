package org.sbpo2025.challenge.Genetic.BrkgaDecoders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.ProblemData;

public class ThreshouldBasedGreedyDecoder extends Decoder {

    @Override
    public int getRKeysSize(ProblemData instanceData) {
        return 2 + instanceData.orders().size() + instanceData.aisles().size();
    }
    @Override
    protected List<List<Integer>> calcEvaluatingOrder(List<Double> keys, ProblemData instanceData) {

        Double aislesThreshould = keys.get(0);
        Double ordersThreshould = keys.get(1);

        List<Double> orderKeys = keys.subList(2, 2 + instanceData.orders().size());
        List<Double> aisleKeys = keys.subList(
            2 + instanceData.orders().size(), 
            2 + instanceData.orders().size()+instanceData.aisles().size()
        );
        List<Integer> orderIndexes = new ArrayList<>();
        List<Integer> aisleIndexes = new ArrayList<>();
        for (int i = 0; i < orderKeys.size() || i < aisleKeys.size(); i++) {
            if ( i < orderKeys.size() && Double.compare(orderKeys.get(i), ordersThreshould) < 0 ){
                orderIndexes.add(i);
            }
            if (i < aisleKeys.size() && Double.compare(aisleKeys.get(i), aislesThreshould) < 0 ){
                aisleIndexes.add(i);
            }
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

        HashSet<Integer> orderResp = new HashSet<>();
        HashSet<Integer> aisleResp = new HashSet<>();

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

        return new ChallengeSolution(orderResp, aisleResp, fo, itensSum);
    }

    private int sumOrderItems(Integer order, ProblemData instanceData) {
        int sum = 0;
        for (int qty : instanceData.orders().get(order).values()) {
            sum += qty;
        }
        return sum;
    }
}
