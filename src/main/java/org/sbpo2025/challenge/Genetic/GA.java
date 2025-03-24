package org.sbpo2025.challenge.Genetic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.sbpo2025.challenge.ChallengeSolution;
import org.sbpo2025.challenge.Genetic.BrkgaDecoders.Decoder;
import org.sbpo2025.challenge.Genetic.CrossOverOperators.CrossOverOp;
import org.sbpo2025.challenge.ProblemData;

public class GA{


    final private Random RANDOM = new Random(); 
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

    final private ProblemData instanceData; // The instance data of the problem

    private void generateMutants(ArrayList<Pair<List<Double>, ChallengeSolution>> nextGen, int quantity){
        List<Double> currentRandomKeys;
        ChallengeSolution currentSolution;
        for(int i = 0; i < quantity; i++){

            currentRandomKeys = new ArrayList<>();
            for (int j = 0; j < brkgaDecoder.getRKeysSize(instanceData); j++){
                currentRandomKeys.add(RANDOM.nextDouble());
            }
            currentSolution = brkgaDecoder.decode(currentRandomKeys, instanceData);
            nextGen.add(Pair.of(currentRandomKeys, currentSolution));
        }
    }

    /**
     * Construct an instance of GA to solve the problem of optimal suborder selection

     * @param brkgaDecoder The decoder responsible for decode a list of random keys to a concrete solution of problem instance
     * @param ngen the number of generations that the GA will be simulated
     * @param psize The size of population
     * @param pbetterParent The prodability of the key will be inherited from the best parent
     * @param eliteFraction The fraction of the population that should be considered as elite
     * @param mutationFraction The factorial of the population that should be replace by random individuals in each generation
     * @param instanceData The instance data of the problem
     * @param crossOp The crossOp that the code should be use to make crossOvers
     */
    public GA(
        Decoder brkgaDecoder,
        int ngen,
        int qGenWithoutImprovement,
        int psize,
        Double pbetterParent,
        Double eliteFraction,
        Double mutationFraction,
        ProblemData instanceData,
        CrossOverOp crossOp
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

    private ProbabilityWheel<Pair<List<Double>, ChallengeSolution>> buildProbWheel(
        List<Pair<List<Double>, ChallengeSolution>> pop,
        int from,
        int to
    ){
        return new ProbabilityWheel<>(pop.subList(from, to), (e) -> e.getRight().fo(), RANDOM);
    }
    private void makeCrossOvers(ArrayList<Pair<List<Double>, ChallengeSolution>> oldPop, ArrayList<Pair<List<Double>, ChallengeSolution>> nextPop, int quantity){
        ProbabilityWheel<Pair<List<Double>, ChallengeSolution>> eliteWheel = buildProbWheel(oldPop, 0, eliteSize);
        ProbabilityWheel<Pair<List<Double>, ChallengeSolution>> nonEliteWheel = buildProbWheel(oldPop, eliteSize, psize);
        
        Pair<List<Double>, ChallengeSolution> bestParent;
        Pair<List<Double>, ChallengeSolution> worstParent;
        List<Double> childKeys;
        for(int i = 0; i < quantity; i++){
            bestParent = eliteWheel.get();
            worstParent = nonEliteWheel.get();
            assert bestParent.getRight().fo() >= worstParent.getRight().fo();
            childKeys = crossOp.makeCrossOver(bestParent.getLeft(), worstParent.getLeft(), pbetterParent, RANDOM);
            nextPop.add(Pair.of(
                childKeys,
                brkgaDecoder.decode(childKeys, instanceData)
            ));
            
        }
    }
    public ChallengeSolution solve(){

        ArrayList<Pair<List<Double>, ChallengeSolution>> pop = new ArrayList<>();
        ArrayList<Pair<List<Double>, ChallengeSolution>> newPop;
        ArrayList<Pair<List<Double>, ChallengeSolution>> lastPromisingPop;
        ChallengeSolution bestOldPop, bestNewPop;
        ChallengeSolution bestSol;

        int genWithoutImprovement = 0;
    
        generateMutants(pop, psize);
        pop.sort(Comparator.comparingDouble((Pair<List<Double>, ChallengeSolution> p) -> p.getRight().fo()).reversed());
        bestSol = pop.get(0).getRight();
        lastPromisingPop = pop;
        for ( int cGen = 0; cGen < ngen; cGen++){
            newPop = new ArrayList<>(pop.subList(0, eliteSize));
            makeCrossOvers(pop, newPop, psize - eliteSize - mutationSize);
            generateMutants(newPop, mutationSize);
            newPop.sort(Comparator.comparingDouble((Pair<List<Double>, ChallengeSolution> p) -> p.getRight().fo()).reversed());
    
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
        return bestSol;
    }
}