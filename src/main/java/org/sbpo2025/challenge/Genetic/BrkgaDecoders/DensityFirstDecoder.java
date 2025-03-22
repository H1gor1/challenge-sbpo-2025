package org.sbpo2025.challenge.Genetic.BrkgaDecoders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

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
    protected  List<List<Integer>>
    calcEvaluatingOrder(List<Double> keys, ProblemData instanceData) {

        // Criação da lista de chaves para os pedidos (primeira parte das chaves)
        List<Double> orderKeys = keys.subList(0, instanceData.orders().size());

        // Criação da lista de corredores e ordenação por número total de itens disponíveis
        ArrayList<Integer> aisleByItensQuantities = new ArrayList<>(instanceData.aisles().size());
        for (int i = 0; i < instanceData.aisles().size(); i++) {
            int totalItems = instanceData.aisles().get(i).values().stream().mapToInt(Integer::intValue).sum();
            aisleByItensQuantities.set(i, totalItems);
        }
        // Ordena os corredores do maior para o menor número de itens disponíveis
        List<Integer> sortedAisles = Stream.iterate(0, i -> i + 1).limit(aisleByItensQuantities.size())
            .sorted(Comparator.comparing(aisleByItensQuantities::get))
            .toList();

        // Ordenação dos pedidos com base nas chaves aleatórias (segunda parte das chaves)
        List<Integer> sortedOrders = Stream.iterate(0, i -> i + 1).limit(orderKeys.size())
            .sorted(Comparator.comparing(orderKeys::get))
            .toList();

        // Retorna as listas de índices dos pedidos e dos corredores ordenados
        return List.of(
            sortedOrders,
            sortedAisles
        );
    }
}
