package org.sbpo2025.challenge.Genetic.BrkgaDecoders;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.ProblemData;

public class TripleKeyGreedyDecoder extends  Decoder {

    @Override
    public int getRKeysSize(ProblemData instanceData) {
        return instanceData.orders().size() + instanceData.aisles().size()*2;
    }

    @Override
    protected List<List<Integer>> calcEvaluatingOrder(List<Double> keys, ProblemData instanceData) {
        List<Double> orderKeys = keys.subList(0, instanceData.orders().size());
        List<Double> aisleKeys = keys.subList(instanceData.orders().size(), instanceData.orders().size()+instanceData.aisles().size());
        List<Double> qAisleKeys = keys.subList(instanceData.orders().size()+instanceData.aisles().size(), keys.size());

        List<Integer> sortedOrderIndexes = Stream.iterate(0, i -> i + 1).limit(orderKeys.size())
            .sorted(Comparator.comparing(orderKeys::get))
            .toList();
        List<Integer> sortedAisleIndexes = Stream.iterate(0, i -> i + 1).limit(aisleKeys.size())
            .sorted(Comparator.comparing(aisleKeys::get))
            .toList();

        return List.of(
            sortedOrderIndexes,
            sortedAisleIndexes,
            List.of(
                IntStream.range(0, qAisleKeys.size()).reduce((a, b) -> qAisleKeys.get(a) < qAisleKeys.get(b)?a:b).orElse(0) + 1
            )
        );
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
