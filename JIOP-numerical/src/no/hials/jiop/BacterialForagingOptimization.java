/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.hials.jiop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 *
 * @author LarsIvar
 */
public class BacterialForagingOptimization extends Algorithm {

    private int size;
    private int nc = 8; //chemotactic steps
    private int ns = 3; //maximum number of times a bacterium will swim in the same direction
    private int nre = 3; //the number of reproduction steps
    private double ped = 0.25; // probability of a particular bacterium being dispersed
    private double ci = 0.05; //basic swim length for each bacterium

    private Colony colony;

    private Candidate bestCandidate;

    public BacterialForagingOptimization(int size, int dimension, Evaluator evaluator) {
        super("Bacterial Foraging Optimization", dimension, evaluator);
        this.size = size;
    }

    @Override
    public void init() {
        super.init();
        this.colony = new Colony(size);
        Collections.sort(colony);
        this.bestCandidate = colony.get(0).copy();
    }

    @Override
    public void init(DoubleArray... seeds) {
        super.init();
        this.colony = new Colony(size - seeds.length);
        for (DoubleArray seed : seeds) {
            colony.add(new Bacteria(seed, getEvaluator().evaluate(seed)));
        }
        Collections.sort(colony);
        this.bestCandidate = colony.get(0).copy();
    }

    @Override
    protected Candidate singleIteration() {
        final Random rng = new Random();

        for (int k = 0; k < nre; k++) // reproduce-eliminate loop
        {
            for (int j = 0; j < nc; j++) // chemotactic loop; the lifespan of each bacterium
            {
                // reset the health of each bacterium to 0.0 
                for (int i = 0; i < size; i++) {
                    colony.get(i).health = 0.0;
                }

                for (int i = 0; i < size; i++) // each bacterium
                {
                    double[] tumble = new double[getDimension()]; // tumble (point in a new direction)
                    for (int p = 0; p < getDimension(); p++) {
                        tumble[p] = 2.0 * rng.nextDouble() - 1.0;
                    } // (hi - lo) * r + lo => random i [-1, +1]
                    double rootProduct = 0.0;
                    for (int p = 0; p < getDimension(); p++) {
                        rootProduct += (tumble[p] * tumble[p]);
                    }

                    for (int p = 0; p < getDimension(); p++) {
                        double value = colony.get(i).get(p) + (ci * tumble[p]) / rootProduct;
                        if (value < 0) {
                            value = 0;
                        } else if (value > 1) {
                            value = 1;
                        }
                        colony.get(i).set(p, value);
                    } // move in new direction

                    // update costs of new position
                    colony.get(i).prevCost = colony.get(i).getCost();
                    colony.get(i).setCost(getEvaluator().evaluate(colony.get(i)));
                    colony.get(i).health += colony.get(i).getCost(); // health is an accumulation of costs during bacterium's life

                    // new best?
                    if (colony.get(i).getCost() < bestCandidate.getCost()) {
                        bestCandidate = colony.get(i).copy();
                    }

                    int m = 0; // swim or not based on prev and curr costs
                    while (m < ns && colony.get(i).getCost() < colony.get(i).prevCost) // we are improving
                    {
                        m++; // swim counter
                        for (int p = 0; p < getDimension(); p++) {
                            double value = colony.get(i).get(p) + (ci * tumble[p]) / rootProduct;
                            if (value < 0) {
                                value = 0;
                            } else if (value > 1) {
                                value = 1;
                            }
                            colony.get(i).set(p, value);

                        } // move in current direction
                        colony.get(i).prevCost = colony.get(i).getCost(); // update costs
                        colony.get(i).setCost(getEvaluator().evaluate(colony.get(i)));
                        if (colony.get(i).getCost() < bestCandidate.getCost()) // did we find a new best?
                        {
                            bestCandidate = colony.get(i).copy();
                        }
                    } // while improving

                } // i, each bacterium in the chemotactic loop

            } // j, chemotactic loop

            // reproduce the healthiest half of bacteria, eliminate the other half
            Collections.sort(colony, new Comparator<Bacteria>() {

                @Override
                public int compare(Bacteria o1, Bacteria o2) {
                    if (o1.health == o2.health) {
                        return 0;
                    } else if (o1.health < o2.health) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }); // sort from smallest health (best) to highest health (worst)
            for (int left = 0; left < size / 2; left++) // left points to a bacterium that will reproduce
            {
                int right = left + size / 2; // right points to a bad bacterium in the rigt side of array that will die
                colony.set(right, colony.get(left).copy());
            }

        } // k, reproduction loop

        // eliminate-disperse
        for (int i = 0; i < size; ++i) {
            double prob = rng.nextDouble();
            if (prob < ped) // disperse this bacterium to a random position
            {
                for (int p = 0; p < getDimension(); p++) {
                    double x = rng.nextDouble();
                    colony.get(i).set(p, x);
                }
                // update costs
                double cost = getEvaluator().evaluate(colony.get(i)); // compute
                colony.get(i).setCost(cost);
                colony.get(i).prevCost = cost;
                colony.get(i).health = 0.0;

                // new best by pure luck?
                if (colony.get(i).getCost() < bestCandidate.getCost()) {
                    bestCandidate = colony.get(i).copy();
                }
            }
        }

        return bestCandidate.copy();
    }

    public int getNc() {
        return nc;
    }

    public void setNc(int nc) {
        this.nc = nc;
    }

    public int getNs() {
        return ns;
    }

    public void setNs(int ns) {
        this.ns = ns;
    }

    public int getNre() {
        return nre;
    }

    public void setNre(int nre) {
        this.nre = nre;
    }

    public double getPed() {
        return ped;
    }

    public void setPed(double ped) {
        this.ped = ped;
    }

    public double getCi() {
        return ci;
    }

    public void setCi(double ci) {
        this.ci = ci;
    }

    @Override
    public int getNumberOfFreeParameters() {
        return 6;
    }

    @Override
    public void setFreeParameters(DoubleArray array) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DoubleArray getFreeParameters() {
        return new DoubleArray(size, nc, nre, ns, ped, ci);
    }

    private class Colony extends ArrayList<Bacteria> {

        public Colony(int size) {
            super(size);
            for (int i = 0; i < size; i++) {
                DoubleArray random = DoubleArray.random(getDimension());
                Bacteria bacteria = new Bacteria(random, getEvaluator().evaluate(random));
                double cost = getEvaluator().evaluate(bacteria);
                bacteria.setCost(cost);
                bacteria.prevCost = cost;
                add(bacteria);
            }
        }
    }

    private class Bacteria extends Candidate {

        public double prevCost;
        public double health;

        public Bacteria(DoubleArray array, double cost) {
            super(array, cost);
        }

        protected Bacteria(Bacteria bacteria) {
            super(bacteria);
            this.health = bacteria.health;
            this.prevCost = bacteria.prevCost;
        }

        @Override
        public Bacteria copy() {
            return new Bacteria(this);
        }

    }
}