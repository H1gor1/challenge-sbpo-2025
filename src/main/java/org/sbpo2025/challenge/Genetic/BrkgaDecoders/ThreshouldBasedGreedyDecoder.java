package org.sbpo2025.challenge.Genetic.BrkgaDecoders;

import java.util.List;
import java.util.stream.Stream;

import org.sbpo2025.challenge.ProblemData;

public class ThreshouldBasedGreedyDecoder extends TripleKeyGreedyDecoder {

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
        List<Integer> orderIndexes = Stream.iterate(0, i -> i + 1).limit(orderKeys.size())
            .filter(index -> Double.compare(orderKeys.get(index), ordersThreshould) < 0).toList();
        List<Integer> aisleIndexes = Stream.iterate(0, i -> i + 1).limit(aisleKeys.size())
            .filter(index -> Double.compare(aisleKeys.get(index), aislesThreshould) < 0).toList();
        return List.of(orderIndexes, aisleIndexes, List.of(aisleIndexes.size()));

    }
}
