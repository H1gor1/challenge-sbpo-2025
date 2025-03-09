package org.sbpo2025.challenge.Genetic;

import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import org.sbpo2025.challenge.Genetic.MutationOperators.MutationOp;

public class MutOpManager {

    private static final int NOTSETED = -1;
    private double diversityBase = (double)NOTSETED;
    private int genRange = NOTSETED;
    final private HashMap<Integer, List<MutationOp>> operatorsMap = new HashMap<>();

    public MutOpManager(List<MutationOp> operators) {
        if (operators == null || operators.isEmpty()) {
            throw new IllegalArgumentException("The operators list cannot be null or empty.");
        }
        IntStream.range(0, operators.size()).forEach(
            i -> operatorsMap.put(i, operators.subList(i, operators.size()))
        );
    }

    public void setRange(double diversityBase, int nGen) {
        if (diversityBase <= 0) {
            throw new IllegalArgumentException("Diversity base must be greater than zero.");
        }
        if ( nGen <= 0 ){
            throw new IllegalArgumentException("Number of generations must be greater than zero.");
        }
        this.diversityBase = diversityBase;
        this.genRange = (int)Math.ceil((double)nGen / operatorsMap.size());
    }

    public MutationOp getOperator(double currentDiversity, int currentGen) {
        if (diversityBase == (double)NOTSETED) {
            throw new IllegalStateException("Range not set, please call setRange before calling getOperator.");
        }

        if (currentDiversity >= diversityBase) {
            return operatorsMap.get(0).get(operatorsMap.size() - 1);
        }

        int i = currentGen / genRange;
        int opRange = (int)diversityBase / operatorsMap.get(i).size();
        int j = (int)(currentDiversity / opRange);
        return operatorsMap.get(i).get(j);
    }
}
