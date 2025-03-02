package org.sbpo2025.challenge.brkga_decoders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.ProblemData;

public class AisleNewOrderDecoder implements Decoder {

    // Método para retornar o tamanho das chaves (RKeys), que é a soma dos tamanhos dos pedidos e corredores
    @Override
    public int getRKeysSize(ProblemData instanceData) {
        return instanceData.aisles().size() + instanceData.orders().size();
    }

    // Método para criar uma lista de pares (chave, índice) a partir de uma lista de chaves
    private ArrayList<Pair<Double, Integer>> makeKeyIndexList(List<Double> keys) {
        ArrayList<Pair<Double, Integer>> keyIndexList = new ArrayList<>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            keyIndexList.add(Pair.of(keys.get(i), i));
        }
        return keyIndexList;
    }

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
    private Pair<List<Integer>, List<Integer>>
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
        aisleQuantities.sort(Comparator.comparingInt(Pair::getLeft).reversed());

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

    /**
     * Verifica se um pedido pode ser atendido com a quantidade de itens disponível nos corredores atualmente em uso.
     *
     * @param orderIndex O índice do pedido a ser verificado.
     * @param QuantItens O vetor que contém a soma de todos os itens dos corredores ainda não utilizados.
     * @param instanceData Dados do problema atual.
     * @return Verdadeiro se o pedido pode ser atendido com os corredores atuais, falso caso contrário.
     */
    private boolean isOrderServable(int orderIndex, int[] QuantItens, ProblemData instanceData) {
        for (int i = 0; i < instanceData.nItems(); i++) {
            // Verifica se a quantidade de itens disponível é suficiente para o pedido
            if (QuantItens[i] < instanceData.orders().get(orderIndex).get(i)) {
                return false;
            }
        }
        return true;
    }

    // Atualiza as quantidades de itens disponíveis após atender um pedido
    private void updateQuantItens(int orderIndex, int[] QuantItens, ProblemData instanceData) {
        for (int i = 0; i < instanceData.nItems(); i++) {
            QuantItens[i] -= instanceData.orders().get(orderIndex).get(i);
        }
    }

    // Atualiza as quantidades de itens disponíveis ao adicionar um novo corredor ao conjunto atual
    private void updateQuantItens(int orderIndex, int[] QuantItens, ProblemData instanceData, int newAisle) {
        for (int i = 0; i < instanceData.nItems(); i++) {
            QuantItens[i] -= instanceData.orders().get(orderIndex).get(i);
            QuantItens[i] += instanceData.aisles().get(newAisle).get(i);
        }
    }

    /**
     * Procura um corredor viável que pode atender a um pedido específico.
     *
     * @param orderIndex O índice do pedido.
     * @param quantItens O vetor que contém a quantidade atual de itens disponíveis.
     * @param aisleResp Conjunto de corredores já utilizados.
     * @param instanceData Dados do problema.
     * @return O índice do corredor viável, ou -1 se nenhum corredor for viável.
     */
    private int findFeasibleAisle(int orderIndex, int[] quantItens, HashSet<Integer> aisleResp, ProblemData instanceData) {
        boolean isFeasible;
        for (int iAisle = 0; iAisle < instanceData.aisles().size(); iAisle++) {
            // Se o corredor já foi utilizado, pula para o próximo
            if (aisleResp.contains(iAisle)) {
                continue;
            }
            // Verifica se todos os itens necessários para o pedido podem ser atendidos pelo corredor
            final int lambdaAisleIndex = iAisle;
            isFeasible = IntStream.range(0, quantItens.length).allMatch(i -> {
                return quantItens[i] + instanceData.aisles().get(lambdaAisleIndex).get(i) >= instanceData.orders().get(orderIndex).get(i);
            });
            // Se for viável, retorna o índice do corredor
            if (isFeasible) {
                return iAisle;
            }
        }
        // Retorna -1 se nenhum corredor for viável
        return -1;
    }

    /**
     * Decodifica a solução a partir das chaves fornecidas.
     *
     * @param keys Chaves aleatórias para decodificar.
     * @param instanceData Dados do problema.
     * @return A solução gerada a partir das chaves.
     */
    @Override
    public ChallengeSolution decode(List<Double> keys, ProblemData instanceData) {
        // Calcula a ordem de avaliação dos pedidos e corredores a partir das chaves
        Pair<List<Integer>, List<Integer>> evaluatingOrder = calcEvaluatingOrder(keys, instanceData);
        List<Integer> orderKeys = evaluatingOrder.getLeft();
        List<Integer> aisleKeys = evaluatingOrder.getRight();

        HashSet<Integer> orderResp = new HashSet<>();
        HashSet<Integer> aisleResp = new HashSet<>(List.of(aisleKeys.get(0)));
        int itensSum = 0;

        // Inicializa o primeiro corredor e os itens disponíveis
        int first_aisle = aisleKeys.get(0);
        int[] QuantItens = instanceData.aisles().get(first_aisle).values()
                .stream()
                .mapToInt(Integer::intValue)
                .toArray();

        int currentOrder;
        int orderItensSum;
        double foAt = 0;
        int newAisle;

        // Itera sobre os pedidos para tentar atendê-los
        for (int i = 0; i < orderKeys.size(); i++) {
            currentOrder = orderKeys.get(i);
            orderItensSum = instanceData.orders().get(currentOrder).values().stream().mapToInt(Integer::intValue).sum();

            // Se adicionar este pedido ultrapassar o limite superior da onda (wave upper bound), ignora
            if (itensSum + orderItensSum > instanceData.waveSizeUB()) {
                continue;
            }
            // Se o pedido for atendível com os corredores atuais, adiciona à resposta
            if (isOrderServable(currentOrder, QuantItens, instanceData)) {
                orderResp.add(currentOrder);
                updateQuantItens(currentOrder, QuantItens, instanceData);
                itensSum += orderItensSum;
                foAt = itensSum / (double) aisleResp.size();
                continue;
            }
            // Caso contrário, verifica se é possível adicionar um novo corredor para atender o pedido
            if ((itensSum + orderItensSum) / (double) (aisleResp.size() + 1) < foAt && itensSum >= instanceData.waveSizeLB()) {
                continue;
            }

            // Tenta encontrar um corredor viável para atender o pedido
            newAisle = findFeasibleAisle(currentOrder, QuantItens, aisleResp, instanceData);
            if (newAisle == -1) {
                continue;
            }
            // Se um corredor viável foi encontrado, adiciona-o à resposta
            aisleResp.add(newAisle);
            orderResp.add(currentOrder);
            updateQuantItens(currentOrder, QuantItens, instanceData, newAisle);
            itensSum += orderItensSum;
            foAt = (foAt + itensSum) / aisleResp.size();
        }

        // Certifica-se de que a soma dos itens é maior ou igual ao limite inferior da onda (wave lower bound)
        assert itensSum >= instanceData.waveSizeLB();

        // Retorna a solução encontrada
        return new ChallengeSolution(orderResp, aisleResp);
    }
}
