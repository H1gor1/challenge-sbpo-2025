package org.sbpo2025.challenge.Genetic.BrkgaDecoders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.sbpo2025.challenge.ProblemData;

public class DensityFirstDecoder extends FirstFeasibleAisleDecoder {

    /**
     * Este método calcula a ordem de avaliação dos pedidos e corredores a partir das chaves aleatórias.
     *
     * A chave para cada pedido está na primeira parte da lista de chaves, e a chave para cada corredor
     * está na segunda parte. A ordem de avaliação será baseada nessas chaves.
     *
     * @param keys Lista de chaves aleatórias que estão sendo decodificadas.
     * @param instanceData Dados do problema atual fornecidos para resolvê-lo.
     * @return Um par de listas: a primeira lista contém a ordem dos pedidos,
     *         e a segunda lista contém a ordem dos corredores.
     */
    @Override
    protected  Pair<List<Integer>, List<Integer>>
    calcEvaluatingOrder(List<Double> keys, ProblemData instanceData) {

        // Criação da lista de chaves para os pedidos (primeira parte das chaves)
        ArrayList<Pair<Double, Integer>> orderKeys = makeKeyIndexList(
                keys.subList(0, instanceData.orders().size())
        );

        // Criação da lista de corredores e ordenação por número total de itens disponíveis
        ArrayList<Pair<Integer, Integer>> aisleQuantities = new ArrayList<>();
        for (int i = 0; i < instanceData.aisles().size(); i++) {
            int totalItems = instanceData.aisles().get(i).values().stream().mapToInt(Integer::intValue).sum();
            aisleQuantities.add(Pair.of(totalItems, i));
        }

        // Ordena os corredores do maior para o menor número de itens disponíveis
        aisleQuantities.sort(Comparator.comparingInt((Pair<Integer, Integer> pair) -> pair.getLeft()).reversed());

        // Criação da lista ordenada de índices dos corredores
        List<Integer> sortedAisleIndices = aisleQuantities.stream()
                .map(Pair::getRight)
                .toList();

        // Ordenação dos pedidos com base nas chaves aleatórias (segunda parte das chaves)
        orderKeys.sort(Comparator.comparingDouble(Pair::getLeft));

        // Retorna as listas de índices dos pedidos e dos corredores ordenados
        return Pair.of(
                orderKeys.stream().map(Pair::getRight).toList(),
                sortedAisleIndices
        );
    }
}
