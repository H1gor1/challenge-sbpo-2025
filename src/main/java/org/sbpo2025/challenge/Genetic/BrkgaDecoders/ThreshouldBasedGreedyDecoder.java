package org.sbpo2025.challenge.Genetic.BrkgaDecoders;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.ProblemData;

public class ThreshouldBasedGreedyDecoder extends Decoder {

    public ThreshouldBasedGreedyDecoder(ProblemData instanceData) {
        super(instanceData);
    }

    private final int thresholdKeysQuantity = 2;

    @Override
    public int getRKeysSize() {
        return thresholdKeysQuantity + Math.max(instanceData.orders().size(), instanceData.aisles().size());
    }
    @Override
    protected EvaluationOrder calcEvaluatingOrder(double[] keys) {

        Double aislesThreshould = keys[0];
        Double ordersThreshould = keys[1];

        int firstOrderIndex = thresholdKeysQuantity;
        int firstAisleIndex = thresholdKeysQuantity;

        int orderIndex = firstOrderIndex;
        int aisleIndex = firstAisleIndex;

        int ordersFinishAt = firstOrderIndex + instanceData.orders().size();
        int aislesFinishAt = firstAisleIndex + instanceData.aisles().size();

        EvaluationOrder evalOrder = new EvaluationOrder(instanceData);
        while ( orderIndex < ordersFinishAt && aisleIndex < aislesFinishAt ){
            if (keys[orderIndex] < ordersThreshould ){
                evalOrder.addOrder(orderIndex - firstOrderIndex);
            }
            if (keys[aisleIndex] < aislesThreshould ){
                evalOrder.addAisle(aisleIndex - firstAisleIndex);
            }
            orderIndex++;
            aisleIndex++;
        }
        while (orderIndex < ordersFinishAt){
            if (keys[orderIndex] < ordersThreshould ){
                evalOrder.addOrder(orderIndex - firstOrderIndex);
            }
            orderIndex++;
        }
        while (aisleIndex < aislesFinishAt){
            if (keys[aisleIndex] < aislesThreshould ){
                evalOrder.addAisle(aisleIndex - firstAisleIndex);
            }
            aisleIndex++;
        }
        if ( evalOrder.getCurrentAisleQuantity() == 0 ){
            evalOrder.addAisle(0);
        }
        return evalOrder;
    }

    private void countIntialItems(
        int[] quantItens,
        EvaluationOrder evalOrder,
        HashSet<Integer> aisleResp
    ) {
        List<Map<Integer, Integer>> aisles = instanceData.aisles();

        EvaluationIterator aisleIterator = evalOrder.aisleIterator();
        while ( aisleIterator.hasNext() ){
            int aisleIndex = aisleIterator.next();
            Map<Integer, Integer> aisle = aisles.get(aisleIndex);

            for (Map.Entry<Integer, Integer> kv : aisle.entrySet()) {
                quantItens[kv.getKey()] += kv.getValue();
            }

            aisleResp.add(aisleIndex);
        }
    }

    @Override
    protected ChallengeSolution performDecode(EvaluationOrder evaluationOrder) {
        Integer QOrders = evaluationOrder.getCurrentOrderQuantity();
        Integer QAisles = evaluationOrder.getCurrentAisleQuantity();

        HashSet<Integer> orderResp = new HashSet<>(QOrders);
        HashSet<Integer> aisleResp = new HashSet<>(QAisles);

        int[] QuantItens = new int[instanceData.nItems()];
        countIntialItems(QuantItens, evaluationOrder, aisleResp);
        
        int itensSum = 0;
        int waveSizeUB = instanceData.waveSizeUB();
        int waveSizeLB = instanceData.waveSizeLB();

        EvaluationIterator orderIterator = evaluationOrder.orderIterator();
        while (orderIterator.hasNext()) {
            int order = orderIterator.next();
            if (!isOrderServable(order, QuantItens)) {
                continue;
            }
            int orderItens = sumOrderItems(order);

            if (itensSum + orderItens > waveSizeUB) {
                continue;
            }
            itensSum += orderItens;
            updateQuantItens(order, QuantItens);
            orderResp.add(order);
        }

        double fo = (itensSum < waveSizeLB) ? 0.0 : (itensSum / (double) QAisles);

        return new ChallengeSolution(orderResp, aisleResp, fo);
    }

}
