package org.sbpo2025.challenge.Genetic.BrkgaDecoders;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.ProblemData;

public class RandomFirstDecoder extends  FirstFeasibleAisleDecoder {

    private boolean isFeasibleWithAisle(
        int orderIndex,
        int newPossibleAisle,
        int[] quantItens,
        ProblemData instanceData
    ){
        Integer aisleCurrentItens;
        Map<Integer, Integer> orderItens = instanceData.orders().get(orderIndex);
        Map<Integer, Integer> newAisleItens = instanceData.aisles().get(newPossibleAisle);
        for (Map.Entry<Integer, Integer> kv : orderItens.entrySet()){
            aisleCurrentItens = newAisleItens.get(kv.getKey());
            aisleCurrentItens = aisleCurrentItens == null ? 0 : aisleCurrentItens;
            if ( kv.getValue() > quantItens[kv.getKey()] + aisleCurrentItens ){
                return false;
            }
        }
        return true;
    }
    private ChallengeSolution addIfImproves(
        List<Integer> orderKeys,
        List<Integer> aisleKeys,
        int[] QuantItens,
        int currentOrder,
        int currentAisle,
        ProblemData instanceData,
        HashSet<Integer> orderResp,
        HashSet<Integer> aisleResp,
        int itensSum
    ){
        int orderItensSum;
        double foAt = itensSum/(double)aisleResp.size();
        for(; currentOrder < orderKeys.size(); currentOrder++){
            orderItensSum = instanceData.orders().get(orderKeys.get(currentOrder)).values().stream().mapToInt(Integer::intValue).sum();
            if (orderItensSum + itensSum > instanceData.waveSizeUB()){
                continue;
            }
            if ( isOrderServable(orderKeys.get(currentOrder), QuantItens, instanceData) ){
                itensSum += orderItensSum;
                orderResp.add(orderKeys.get(currentOrder));
                updateQuantItens(orderKeys.get(currentOrder), QuantItens, instanceData);
                continue;
            }
            if( (itensSum + orderItensSum)/((double)(aisleResp.size()+1)) < foAt ){
                continue;
            }
            currentAisle++;
            if (currentAisle >= aisleKeys.size()){
                break;
            }
            if ( isFeasibleWithAisle(orderKeys.get(currentOrder), aisleKeys.get(currentAisle), QuantItens, instanceData)){
                itensSum += orderItensSum;
                orderResp.add(orderKeys.get(currentOrder));
                updateQuantItens(orderKeys.get(currentOrder), QuantItens, instanceData, aisleKeys.get(currentAisle));
                aisleResp.add(aisleKeys.get(currentAisle));
                foAt = itensSum/(double)aisleResp.size();
            }
        }
        return new ChallengeSolution(orderResp, aisleResp, foAt, itensSum);
    }
    @Override
    public ChallengeSolution performDecode(List<List<Integer>> evaluationOrder, ProblemData instanceData) {
    
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
        int currentOrderIndex = 0;
        int currentAisleIndex = 0;
        int currentOrder, currentAisle;
        for(;currentAisleIndex < aisleKeys.size(); currentAisleIndex++){
            currentAisle = aisleKeys.get(currentAisleIndex);
            aisleResp.add(currentAisle);
            updateQuantItens(QuantItens, instanceData, currentAisle);

            while (
                currentOrderIndex < orderKeys.size() && 
                isOrderServable(orderKeys.get(currentOrderIndex), QuantItens, instanceData)
            ){
                currentOrder = orderKeys.get(currentOrderIndex);
                orderItensSum = instanceData.orders().get(currentOrder).values().stream().mapToInt(Integer::intValue).sum();
                if (orderItensSum + itensSum > instanceData.waveSizeUB()){
                    continue;
                }
                itensSum += orderItensSum;
                orderResp.add(currentOrder);
                updateQuantItens(currentOrder, QuantItens, instanceData);
                currentOrderIndex++;
                if ( itensSum >= instanceData.waveSizeLB() ){
                    return addIfImproves(
                        orderKeys, aisleKeys, QuantItens, currentOrderIndex,
                        currentAisleIndex, instanceData, orderResp, aisleResp, itensSum
                    );
                }
            }
        }
        currentOrderIndex++;//if the last for run until end, so means that the current Order is unservable
        currentOrder = orderKeys.get(currentOrderIndex);
        while (itensSum < instanceData.waveSizeLB()){
            if ( !isOrderServable(currentOrder, QuantItens, instanceData) ){
                currentOrderIndex++;
                currentOrder = orderKeys.get(currentOrderIndex);
                continue;
            }
            orderItensSum = instanceData.orders().get(currentOrder).values().stream().mapToInt(Integer::intValue).sum();
            if (orderItensSum + itensSum > instanceData.waveSizeUB()){
                break;
            }
            itensSum += orderItensSum;
            orderResp.add(orderKeys.get(currentOrder));
            updateQuantItens(currentOrder, QuantItens, instanceData);
            currentOrderIndex++;
            currentOrder = orderKeys.get(currentOrderIndex);
        }
        return addIfImproves(orderKeys, aisleKeys, QuantItens, currentOrderIndex, currentAisleIndex, instanceData, orderResp, aisleResp, itensSum);
    }
 
    
}
