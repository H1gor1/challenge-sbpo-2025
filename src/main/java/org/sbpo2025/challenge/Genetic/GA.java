package org.sbpo2025.challenge.Genetic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.Genetic.BrkgaDecoders.Decoder;
import org.sbpo2025.challenge.Genetic.CrossOverOperators.CrossoverInterface;
import org.sbpo2025.challenge.Genetic.MutationOperators.MutationInterface;
import org.sbpo2025.challenge.ProblemData;


class ProdabilityWheel{

    final private Random RANDOM = new Random();
    final private TreeMap<Double, Integer> probMap;

    private TreeMap<Double, Integer> makeProbMap(List<Double> probDist){
        Double Vsum = probDist.stream().reduce(0.0, (a, b) -> a + b);
        final double epsilon = 1e-9;
        boolean isNormalized = Math.abs(Vsum - 1.0) < epsilon;
        probDist = isNormalized 
            ? probDist 
            : probDist.stream().map(p -> p / Vsum).collect(Collectors.toList());
        TreeMap<Double, Integer> map = new TreeMap<>();
        double accumulated = 0.0;
        for (int i = 0; i < probDist.size(); i++) {
            accumulated += probDist.get(i);
            map.put(accumulated, i);
        }
        return map;
    }
    public ProdabilityWheel(List<Double> probDist){
        probMap = makeProbMap(probDist);
    }
    public Integer get(){
        double value = RANDOM.nextDouble();
        return probMap.higherEntry(value).getValue();
    }
    
}
public class GA{

    /**
     * Algorithm parameters:
     */
    final private int STATESRANGE = 10;
    final private Random RANDOM = new Random(); 
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
        List<Double> currentRandomKeys;
        for(int i = 0; i < psize; i++){
            currentRandomKeys = IntStream.range(0, psize).mapToDouble(k -> RANDOM.nextDouble())
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

    private ProdabilityWheel buildProbWheel(){
        Double SumFitness = pop.stream().mapToDouble(p -> p.getRight().fo()).sum();
        List<Double> fitnesses = pop.stream().map(p -> p.getRight().fo() / SumFitness).collect(Collectors.toList());
        return new ProdabilityWheel(fitnesses);
    }
    private ArrayList<Pair<List<Double>, ChallengeSolution>> makeCrossOvers(){
        ArrayList<Pair<List<Double>, ChallengeSolution>> newPop = new ArrayList<>();
        Pair<List<Double>, ChallengeSolution> parent1, parent2, newChild;
        List<Double> newChildKeys;

        // set the current state with base on the best solution FO until now
        final Pair<List<Double>, ChallengeSolution> bestParent = pop.get(0);
        Double currentState = bestParent.getRight().fo()/STATESRANGE;
        CrossoverInterface crossOp = crossOps.getOperator(currentState.intValue());

        ProdabilityWheel wheel = buildProbWheel();
        for(int i = 0; i < psize; i++){
            parent1 = pop.get(wheel.get());
            parent2 = pop.get(wheel.get());

            if (parent1.getRight().fo() > parent2.getRight().fo()){
                newChildKeys = crossOp.makeCrossOver(parent1.getLeft(), parent2.getLeft(), pbetterParent);
            }else{
                newChildKeys = crossOp.makeCrossOver(parent2.getLeft(), parent1.getLeft(), pbetterParent);
            }
            newChild = Pair.of(
                newChildKeys,
                brkgaDecoder.decode(newChildKeys, instanceData)
            );
            newPop.add(newChild);
        }
        newPop.sort(Comparator.comparingDouble((Pair<List<Double>, ChallengeSolution> p) -> p.getRight().fo()).reversed());
        Double improvementRate = newPop.get(0).getRight().fo() / bestParent.getRight().fo();
        crossOps.feedBack(improvementRate);
        return newPop;
    }
    private void makeMutations(){

        
    }
    public ChallengeSolution solve(){
        return null;
    }
}