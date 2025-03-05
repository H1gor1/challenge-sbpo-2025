package org.sbpo2025.challenge.Genetic.BrkgaDecoders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
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
        ArrayList<Pair<Double, Integer>> orderKeysList = makeKeyIndexList(orderKeys);
        ArrayList<Pair<Double, Integer>> aisleKeysList = makeKeyIndexList(aisleKeys);
        ArrayList<Pair<Double, Integer>> qAisleKeysList = makeKeyIndexList(qAisleKeys);

        orderKeysList.sort(Comparator.comparingDouble(Pair::getLeft));
        aisleKeysList.sort(Comparator.comparingDouble(Pair::getLeft));
        qAisleKeysList.sort(Comparator.comparingDouble(Pair::getLeft));

        return List.of(
            orderKeysList.stream().map(Pair::getRight).toList(),
            aisleKeysList.stream().map(Pair::getRight).toList(),
            qAisleKeysList.stream().map(Pair::getRight).toList()
        );
    }

    private void countIntialItems(
        int[] quantItens,
        List<Integer> aisleIndexes,
        int QAisles,
        ProblemData instanceData,
        HashSet<Integer> aisleResp
    ){
        for (int i = 0; i<QAisles; i++){
            for (Map.Entry<Integer, Integer> kv : instanceData.aisles().get(aisleIndexes.get(i)).entrySet()){
                quantItens[kv.getKey()] += kv.getValue();
            }
            aisleResp.add(aisleIndexes.get(i));
        }
    }
    @Override
    protected ChallengeSolution performDecode(List<List<Integer>> evaluationOrder, ProblemData instanceData) {
        List<Integer> orderIndexes = evaluationOrder.get(0);
        List<Integer> aisleIndexes = evaluationOrder.get(1);
        List<Integer> qAisleIndexes = evaluationOrder.get(2);
        Integer QAisles = qAisleIndexes.get(0)+1;
        HashSet<Integer> orderResp = new HashSet<>();
        HashSet<Integer> aisleResp = new HashSet<>();

        int[] QuantItens = new int[instanceData.nItems()];
        countIntialItems(QuantItens, aisleIndexes, QAisles, instanceData, aisleResp);

        int itensSum=0;
        int orderItens;
        for( Integer order : orderIndexes){
            if ( !isOrderServable(order, QuantItens, instanceData) ){
                continue;
            }
            orderItens = instanceData.orders().get(order).values().stream().mapToInt(Integer::intValue).sum();
            if (itensSum + orderItens > instanceData.waveSizeUB()){
                continue;
            }
            itensSum += orderItens;
            updateQuantItens(order, QuantItens, instanceData);
            orderResp.add(order);
        }
        double fo;
        if (itensSum < instanceData.waveSizeLB() ){
            fo = 0.0;
        }else{
            fo = itensSum/(double)QAisles;
        }
        return new ChallengeSolution(
            orderResp,
            aisleResp,
            fo
        );
    }
   
    
 
    
}
