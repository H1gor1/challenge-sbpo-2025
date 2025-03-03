package org.sbpo2025.challenge.Genetic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.Genetic.BrkgaDecoders.Decoder;
import org.sbpo2025.challenge.Genetic.CrossOverOperators.CrossoverInterface;
import org.sbpo2025.challenge.Genetic.MutationOperators.MutationInterface;
import org.sbpo2025.challenge.ProblemData;

class GA{

    /**
     * Algorithm parameters:
     */
    private Decoder brkgaDecoder;
    private int ngen;
    private int psize;
    private Double tmut;
    private Double pbetterParent;
    private OpManager<CrossoverInterface> crossOps;
    private OpManager<MutationInterface> mutOps;
    private ProblemData instanceData;

    private ArrayList<Pair<List<Double>, ChallengeSolution>> pop;


    private void initializePopulation(){
        pop = new ArrayList<>();
        Random random = new Random();
        List<Double> currentRandomKeys;
        for(int i = 0; i < psize; i++){
            currentRandomKeys = IntStream.range(0, psize).mapToDouble(k -> random.nextDouble())
                                .boxed()
                                .collect(Collectors.toList());
            pop.add(
                Pair.of(
                    currentRandomKeys,
                    brkgaDecoder.decode(currentRandomKeys, instanceData)
                )
            );
        }
    }

    public GA(
        Decoder brkgaDecoder,
        int ngen,
        int psize,
        Double tmut,
        Double pbetterParent,
        ProblemData instanceData,
        OpManager<CrossoverInterface> crossOps,
        OpManager<MutationInterface> mutOps
    ){
        this.brkgaDecoder = brkgaDecoder;
        this.ngen = ngen;
        this.psize = psize;
        this.tmut = tmut;
        this.pbetterParent = pbetterParent;
        this.crossOps = crossOps;
        this.mutOps = mutOps;
        this.instanceData = instanceData;

        initializePopulation();
        pop.sort(Comparator.comparingDouble((Pair<List<Double>, ChallengeSolution> p) -> p.getRight().fo()).reversed());
    }

    private ArrayList<Pair<List<Double>, ChallengeSolution>> makeCrossOvers(){
        ArrayList<Pair<List<Double>, ChallengeSolution>> newPop = new ArrayList<>();
        crossOps.getOperator(0);
        return null;
    }
    private void makeMutations(){

    }
    public ChallengeSolution solve(){
        return null;
    }
}