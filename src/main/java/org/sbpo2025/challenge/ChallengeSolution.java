package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record ChallengeSolution(Set<Integer> orders, Set<Integer> aisles, Double fo, Integer itensCount) {

    public List<Double> getSolutionCharacteristics() {
        List<Double> characters = List.of((double) orders.size(), (double) aisles.size(), fo, itensCount.doubleValue());
        return new ArrayList<>(characters);
    }
}
