/*
 * Particles, a self-organizing particle system simulator.
 * Copyright (C) 2018  Cem Gokmen.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.cemgokmen.particles.algorithms;

import com.cemgokmen.particles.capabilities.MovementCapable;
import com.cemgokmen.particles.capabilities.NeighborDetectionCapable;
import com.cemgokmen.particles.capabilities.ParticleCapability;
import com.cemgokmen.particles.capabilities.UniformRandomDirectionCapable;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.AmoebotParticle;
import com.cemgokmen.particles.util.Utils;
import com.google.common.collect.ImmutableList;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.List;

public class CompressionAlgorithm extends ParticleAlgorithm {
    public static final List<Class<? extends ParticleCapability>> requiredCapabilities = ImmutableList.of(MovementCapable.class, UniformRandomDirectionCapable.class, NeighborDetectionCapable.class);

    public static final double DEFAULT_LAMBDA = 4.0;

    protected final DoubleProperty lambda = new SimpleDoubleProperty();

    public CompressionAlgorithm(double lambda) {
        this.setLambda(lambda);
    }

    public double getLambda() {
        return this.lambda.get();
    }

    public DoubleProperty lambdaProperty() {
        return this.lambda;
    }

    public void setLambda(double lambda) {
        this.lambda.set(lambda);
    }

    public CompressionAlgorithm() {
        this(DEFAULT_LAMBDA);
    }

    @Override
    public void onParticleActivation(Particle p) {
        UniformRandomDirectionCapable particle = (UniformRandomDirectionCapable) p;

        // Pick a random direction
        ParticleGrid.Direction randomDirection = particle.getUniformRandomDirection();

        //System.out.println("Time for move validation");

        // Run move validation
        if (p instanceof AmoebotParticle) {
            if (!RuleUtils.isMoveValidCompressionMove((AmoebotParticle) particle, randomDirection, false, true)) {
                //System.out.println("Invalid move, returning");
                return;
            }
        }

        //System.out.println("Time for probability calculation");

        double moveProbability = this.getMoveProbability((NeighborDetectionCapable) p, randomDirection);

        //System.out.printf("Move probability: %.2f%%. Now filtering.", moveProbability * 100);

        if (Utils.randomDouble() > moveProbability) {
            return;
        }

        //System.out.println("Passed filter");

        // Now make the move
        try {
            ((MovementCapable) particle).move(randomDirection);
            //System.out.println("Moved.");
        } catch (Exception ignored) {

        }
    }

    @Override
    public List<Class<? extends ParticleCapability>> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    @Override
    public boolean isGridValid(ParticleGrid grid) {
        return RuleUtils.checkParticleConnection(grid, particle -> true) && RuleUtils.checkParticleHoles(grid, particle -> true);
    }

    public double getMoveProbability(NeighborDetectionCapable p, ParticleGrid.Direction inDirection) {
        int currentNeighbors = p.getNeighborParticles(false, null).size();
        int futureNeighbors = p.getAdjacentPositionNeighborParticles(inDirection, false, particle -> particle
                != p).size();
        return Math.pow(this.getLambda(), futureNeighbors - currentNeighbors);
    }
}
