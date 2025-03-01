package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sbpo2025.challenge.brkga_decoders.Decoder;
import org.sbpo2025.challenge.brkga_decoders.aisleOrderDecoder;

public class Test {
    public static void main(String[] args) {
        List<Map<Integer, Integer>> orders = new ArrayList<>();
        orders.add(Map.of(0, 5, 1, 3, 2, 2));
        orders.add(Map.of(0, 2, 1, 4, 2, 1));
        orders.add(Map.of(0, 6, 1, 2, 2, 3));

        List<Map<Integer, Integer>> aisles = new ArrayList<>();
        aisles.add(Map.of(0, 910, 1, 995, 2, 994));
        aisles.add(Map.of(0, 3, 1, 7, 2, 2));
        aisles.add(Map.of(0, 4, 1, 6, 2, 5));

        ProblemData instanceData = new ProblemData(orders, aisles, 3, 1, 5);
        Decoder d = new aisleOrderDecoder();
        ChallengeSolution cs = d.decode(new ArrayList<>(List.of(0.6, 0.2, 0.8, 0.01, 0.5, 0.1)), instanceData);
    }
}
