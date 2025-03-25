package org.sbpo2025.challenge.Genetic.BrkgaDecoders;

import java.util.ArrayList;
import java.util.List;

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
}
