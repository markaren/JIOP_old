/*
 * Copyright (c) 2014, Aalesund University College 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package no.hials.jiop.base.candidates.encoding;

import java.util.Arrays;
import java.util.Random;
import no.hials.jiop.utils.ArrayUtil;
import no.hials.jiop.base.candidates.Candidate;

/**
 *
 * @author LarsIvar
 */
public class DoubleArrayParticleEncoding extends DoubleArrayEncoding implements ParticleEncoding<double[]> {

    private final Random rng = new Random();
    private final double[] velocity;
    private Candidate<double[]> localBest;

    public DoubleArrayParticleEncoding(double[] variables, Candidate<double[]> localBest) {
        super(variables);
        this.velocity = ArrayUtil.randomD(-1, 1, size());
        this.localBest = new Candidate<>(new DoubleArrayEncoding(localBest.getVariables()), localBest.getCost());
    }
    
    public DoubleArrayParticleEncoding(double[] variables, double[] velocity, Candidate<double[]> localBest) {
        super(variables);
        this.velocity = velocity;
        this.localBest = new Candidate<>(new DoubleArrayEncoding(localBest.getVariables().clone()), localBest.getCost());
    }

    @Override
    public DoubleArrayParticleEncoding copy() {
        return new DoubleArrayParticleEncoding(getVariables().clone(), velocity.clone(), new Candidate<>(localBest));
    }

    @Override
    public double[] getVelocity() {
        return velocity;
    }

    @Override
    public Candidate<double[]> getLocalBest() {
        return localBest;
    }

    @Override
    public void setLocalBest(Candidate<double[]> localBest) {
        this.localBest = new Candidate<>(localBest);
    }

    @Override
    public void update(double omega, double c1, double c2, double[] globalBest) {
        double r1 = rng.nextDouble();
        double r2 = rng.nextDouble();
        for (int i = 0; i < globalBest.length; i++) {
            double vi = getVelocity()[i];
            double li = getLocalBest().getVariables()[i];
            double pi = getVariables()[i];
            double gi = globalBest[i];
            double vel = (omega * vi) + ((rng.nextDouble() * c1 * (li - pi)) + (rng.nextDouble() * c2 * (gi - pi)));

            if (vel < -0.1) {
                vel = -0.1;
            } else if (vel > 0.1) {
                vel = 0.1;
            }

            double newPos = getVariables()[i] + vel;
            if (newPos < 0) {
                newPos = 0;
            } else if (newPos > 1) {
                newPos = 1;
            }
            getVariables()[i] = newPos;
            getVelocity()[i] = vel;
        }
    }

    @Override
    public String toString() {
        return "DoubleArrayParticleEncoding{" + super.toString() + '}';
    }

    
    
}
