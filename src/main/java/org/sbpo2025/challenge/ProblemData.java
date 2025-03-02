package org.sbpo2025.challenge;

import java.util.List;
import java.util.Map;

public record ProblemData(
    List<Map<Integer, Integer>> orders,
    List<Map<Integer, Integer>> aisles,
    int nItems,
    int waveSizeLB,
    int waveSizeUB
){}