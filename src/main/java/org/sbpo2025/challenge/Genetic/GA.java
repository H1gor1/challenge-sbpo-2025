package org.sbpo2025.challenge.Genetic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.tuple.Pair;
import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.Genetic.BrkgaDecoders.Decoder;
import org.sbpo2025.challenge.Genetic.CrossOverOperators.CrossOverOp;
import org.sbpo2025.challenge.ProblemData;
import org.sbpo2025.challenge.ThreadPoolController.ThreadPool;
import org.sbpo2025.challenge.ThreadPoolController.TwoParamRunnable;



public class GA{

    /**
     * Algorithm parameters:
     */
    /*
     * The fraction of elite, crossover and mutation of individuals in the population.
     */
    final private int eliteSize;
    final private int mutationSize;

    /* The decoder used in GA */
    final private Decoder brkgaDecoder; 

    /*The number of generations and population size respectively */
    final private int ngen;
    final private int qGenWithoutImprovement;
    final private int psize;

    /**
     * This params are responsible by the crossover behavior of the GA.
     */
    final private CrossOverOp crossOp;
    final private Double pbetterParent;

    /**
     * The controller of the pool of threads that the GA can use
     */
    final private ThreadPool threadPool;
    final private int maxRunningThreads;

    final private ProblemData instanceData; // The instance data of the problem

    private void generateMutants(List<Pair<double[], ChallengeSolution>> nextGen, int quantity){
        TwoParamRunnable<Integer, Integer> mutationTask;
        int from;
        int to;
        int previousSize = nextGen.size();
        int increment = quantity/maxRunningThreads + 1;
        for ( int i = 0; i < quantity; i+=increment ){
            from = previousSize+i;
            to = previousSize + Math.min(i + increment, quantity);

            mutationTask = new TwoParamRunnable<>(from, to, ( pFrom, pTo ) -> {
                final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
                double[] currentRandomKeys;
                ChallengeSolution decodedSol;
                for (;pFrom < pTo; pFrom++ ){
                    currentRandomKeys = new double[brkgaDecoder.getRKeysSize(instanceData)];
                    for (int j = 0; j < brkgaDecoder.getRKeysSize(instanceData); j++){
                        currentRandomKeys[j] = RANDOM.nextDouble();
                    }
                    decodedSol = brkgaDecoder.decode(currentRandomKeys, instanceData);
                    synchronized (nextGen){
                        nextGen.add(Pair.of(
                            currentRandomKeys,
                            decodedSol
                        ));
                    }
                    
                }
            });
            threadPool.submit(mutationTask);
        }
    }

    /**
     * Construct an instance of GA to solve the problem of optimal suborder selection

     * @param brkgaDecoder The decoder responsible for decode a list of random keys to a concrete solution of problem instance
     * @param crossOp The crossOp that the code should be use to make crossOvers
     * @param maxRunningThreads The maximum number of threads that the GA can use in your operations (e.g crossOver, mutation)
     * @param ngen the number of generations that the GA will be simulated
     * @param psize The size of population
     * @param pbetterParent The prodability of the key will be inherited from the best parent
     * @param eliteFraction The fraction of the population that should be considered as elite
     * @param mutationFraction The factorial of the population that should be replace by random individuals in each generation
     * @param instanceData The instance data of the problem
     */
    public GA(
        Decoder brkgaDecoder,
        CrossOverOp crossOp,
        int maxRunningThreads,
        int ngen,
        int qGenWithoutImprovement,
        int psize,
        Double pbetterParent,
        Double eliteFraction,
        Double mutationFraction,
        ProblemData instanceData
    ){
        if (eliteFraction + mutationFraction >= 1.0){
            throw new IllegalArgumentException("The sum of eliteFraction and mutationFraction must be less than 1.0");
        }
        if ( pbetterParent >= 1.0 ){
            throw new IllegalArgumentException("The pbetterParent must be less than 1.0");
        }
        if (qGenWithoutImprovement <= 0 || qGenWithoutImprovement >= ngen){
            throw new IllegalArgumentException("The qGenWithoutImprovement must be greater than 0 and less than ngen");
        }
        this.threadPool = new ThreadPool(maxRunningThreads);
        this.maxRunningThreads = maxRunningThreads;
        this.brkgaDecoder = brkgaDecoder;
        this.ngen = ngen;
        this.psize = psize;
        this.pbetterParent = pbetterParent;
        this.crossOp = crossOp;
        this.instanceData = instanceData;
        this.eliteSize = (int)(psize * eliteFraction);
        this.mutationSize = (int)(psize * mutationFraction);
        this.qGenWithoutImprovement = qGenWithoutImprovement;

    }

    private ProbabilityWheel<Pair<double[], ChallengeSolution>> buildProbWheel(
        List<Pair<double[], ChallengeSolution>> pop,
        int from,
        int to
    ){
        return new ProbabilityWheel<>(pop.subList(from, to), (e) -> e.getRight().fo());
    }
    private void makeCrossOvers(List<Pair<double[], ChallengeSolution>> oldPop, List<Pair<double[], ChallengeSolution>> nextPop, int quantity){
        final ProbabilityWheel<Pair<double[], ChallengeSolution>> eliteWheel = buildProbWheel(oldPop, 0, eliteSize);
        final ProbabilityWheel<Pair<double[], ChallengeSolution>> nonEliteWheel = buildProbWheel(oldPop, eliteSize, psize);
        TwoParamRunnable<Integer, Integer> crossOverTask;
        Integer from;
        Integer to;
        int previousSize = nextPop.size();
        int increment = quantity/maxRunningThreads + 1;

        for (int i = 0; i < quantity; i+=increment ){
            from = previousSize+i;
            to = previousSize+Math.min(i + increment, quantity);

            crossOverTask = new TwoParamRunnable<>(
                from, to,
                ( pFrom, pTo ) -> {
                    final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
                    Pair<double[], ChallengeSolution> bestParent;
                    Pair<double[], ChallengeSolution> worstParent;
                    ChallengeSolution decodedSol;
                    double[] childKeys;
                    for(;pFrom < pTo; pFrom++){
                        bestParent = eliteWheel.get(RANDOM);
                        worstParent = nonEliteWheel.get(RANDOM);
                        childKeys = crossOp.makeCrossOver(bestParent.getLeft(), worstParent.getLeft(), pbetterParent, RANDOM);
                        decodedSol = brkgaDecoder.decode(childKeys, instanceData);
                        
                        synchronized (nextPop){
                            nextPop.add(Pair.of(
                                childKeys,
                                decodedSol
                            ));
                        }
                    }
                }
            );
            threadPool.submit(crossOverTask);
        }
    }
    public ChallengeSolution solve(){

        ArrayList<Pair<double[], ChallengeSolution>> pop = new ArrayList<>(psize);
        ArrayList<Pair<double[], ChallengeSolution>> newPop;
        ArrayList<Pair<double[], ChallengeSolution>> lastPromisingPop;
        ChallengeSolution bestOldPop, bestNewPop;
        ChallengeSolution bestSol;

        int genWithoutImprovement = 0;
    
        generateMutants(pop, psize);
        threadPool.waitAll();
        pop.sort(Comparator.comparingDouble((Pair<double[], ChallengeSolution> p) -> p.getRight().fo()).reversed());
        bestSol = pop.get(0).getRight();
        lastPromisingPop = pop;
        for ( int cGen = 0; cGen < ngen; cGen++){
            newPop = new ArrayList<>(pop.subList(0, eliteSize));
            newPop.ensureCapacity(psize);
            makeCrossOvers(pop, newPop, psize - eliteSize - mutationSize);
            generateMutants(newPop, mutationSize);
            threadPool.waitAll();
            newPop.sort(Comparator.comparingDouble((Pair<double[], ChallengeSolution> p) -> p.getRight().fo()).reversed());
    
            bestOldPop = pop.get(0).getRight();
            bestNewPop = newPop.get(0).getRight();
            if (bestOldPop.fo() >= bestNewPop.fo()){
                genWithoutImprovement++;
            }else{
                genWithoutImprovement = 0;
                lastPromisingPop = pop;
                if (bestNewPop.fo() > bestSol.fo()){
                    bestSol = bestNewPop;
                }
            }
            if (genWithoutImprovement == qGenWithoutImprovement){
                pop = lastPromisingPop;
                genWithoutImprovement = 0;
            }else{
                pop = newPop;
            }
  
        }
        threadPool.close();
        return bestSol;
    }
}