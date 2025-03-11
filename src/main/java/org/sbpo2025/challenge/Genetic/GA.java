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
import org.sbpo2025.challenge.Genetic.CrossOverOperators.CrossOverOp;
import org.sbpo2025.challenge.Genetic.MutationOperators.MutationOp;
import org.sbpo2025.challenge.ProblemData;


class ProdabilityWheel{

    final private Random RANDOM;
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
    public ProdabilityWheel(List<Double> probDist, Random random){
        probMap = makeProbMap(probDist);
        RANDOM = random;
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
    final private Decoder brkgaDecoder;
    final private int ngen;
    final private int psize;
    final private Double tmut;
    final private Double pbetterParent;
    final private OpManager<CrossOverOp> crossOps;
    final private MutationOp mutOp;
    final private ProblemData instanceData;
    private Double  currentDiversity = 0.0;
    private ChallengeSolution bestSol = null;

    private ArrayList<Pair<List<Double>, ChallengeSolution>> pop;

    private void initializePopulation(){
        pop = new ArrayList<>();
        List<Double> currentRandomKeys;
        ChallengeSolution currentSol;
        currentRandomKeys = IntStream.range(0, brkgaDecoder.getRKeysSize(instanceData))
                            .mapToDouble(k -> RANDOM.nextDouble())
                            .boxed()
                            .collect(Collectors.toList());
        currentSol = brkgaDecoder.decode(currentRandomKeys, instanceData);
        pop.add(
            Pair.of(
                currentRandomKeys,
                currentSol
            )
        );
        bestSol = currentSol;

        for(int i = 1; i < psize; i++){
            currentRandomKeys = IntStream.range(0, brkgaDecoder.getRKeysSize(instanceData)).mapToDouble(k -> RANDOM.nextDouble())
                                .boxed()
                                .collect(Collectors.toList());
            currentSol = brkgaDecoder.decode(currentRandomKeys, instanceData);
            pop.add(
                Pair.of(
                    currentRandomKeys,
                    currentSol
                )
            );
            if (currentSol.fo() > bestSol.fo()){
                bestSol = currentSol;
            }
        }
    }

    public GA(
        Decoder brkgaDecoder,
        int ngen,
        int psize,
        Double tmut,
        Double pbetterParent,
        ProblemData instanceData,
        OpManager<CrossOverOp> crossOps,
        MutationOp mutOp
    ){
        this.brkgaDecoder = brkgaDecoder;
        this.ngen = ngen;
        this.psize = psize;
        this.tmut = tmut;
        this.pbetterParent = pbetterParent;
        this.crossOps = crossOps;
        this.mutOp = mutOp;
        this.instanceData = instanceData;
    }

    private ProdabilityWheel buildProbWheel(){
        Double SumFitness = pop.stream().mapToDouble(p -> p.getRight().fo()).sum();
        List<Double> fitnesses = pop.stream().map(p -> p.getRight().fo() / SumFitness).collect(Collectors.toList());
        return new ProdabilityWheel(fitnesses, RANDOM);
    }
    private ArrayList<Pair<List<Double>, ChallengeSolution>> makeCrossOvers(){
        ArrayList<Pair<List<Double>, ChallengeSolution>> newPop = new ArrayList<>();
        Pair<List<Double>, ChallengeSolution> parent1, parent2, newChild, bestChild;
        List<Double> newChildKeys;
        bestChild = null;

        // set the current state with base on the best solution FO until now
        final Pair<List<Double>, ChallengeSolution> bestParent = pop.get(0);
        Double currentState = bestParent.getRight().fo()/STATESRANGE;
        CrossOverOp crossOp = crossOps.getOperator(currentState.intValue());

        ProdabilityWheel wheel = buildProbWheel();
        for(int i = 0; i < psize/2; i++){
            parent1 = pop.get(wheel.get());
            parent2 = pop.get(wheel.get());

            if (parent1.getRight().fo() > parent2.getRight().fo()){
                newChildKeys = crossOp.makeCrossOver(parent1.getLeft(), parent2.getLeft(), pbetterParent, RANDOM);
            }else{
                newChildKeys = crossOp.makeCrossOver(parent2.getLeft(), parent1.getLeft(), pbetterParent, RANDOM);
            }
            newChild = Pair.of(
                newChildKeys,
                brkgaDecoder.decode(newChildKeys, instanceData)
            );
            if ( bestChild == null || newChild.getRight().fo() > bestChild.getRight().fo() ){
                bestChild = newChild;
            }
            if ( bestSol.fo() < newChild.getRight().fo() ){
                bestSol = newChild.getRight();
            }
            newPop.add(newChild);
        }
        Double improvementRate = bestChild.getRight().fo() / bestParent.getRight().fo();
        crossOps.feedBack(improvementRate);
        return newPop;
    }
    private void makeMutations(){
        for(int i = 0; i<pop.size(); i++){
            if ( RANDOM.nextDouble() < tmut ){
                mutOp.makeMutation(pop.get(i).getLeft(), RANDOM);
                pop.set(i, Pair.of(
                    pop.get(i).getLeft(),
                    brkgaDecoder.decode(pop.get(i).getLeft(), instanceData)
                ));
                if ( bestSol.fo() < pop.get(i).getRight().fo() ){
                    bestSol = pop.get(i).getRight();
                }
            }
        }
    }
    public ChallengeSolution solve(){

        ArrayList<Pair<List<Double>, ChallengeSolution>> children;
    
        initializePopulation();
        pop.sort(Comparator.comparingDouble((Pair<List<Double>, ChallengeSolution> p) -> p.getRight().fo()).reversed());
        for ( int cGen = 0; cGen < ngen; cGen++){
            children  = makeCrossOvers();
            pop.addAll(children);
            pop.sort(Comparator.comparingDouble((Pair<List<Double>, ChallengeSolution> p) -> p.getRight().fo()).reversed());
            pop = new ArrayList<>(pop.subList(0, psize));
            makeMutations();
        }
        return bestSol;
    }
}