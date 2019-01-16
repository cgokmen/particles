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

import com.cemgokmen.particles.capabilities.*;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.AmoebotParticle;
import com.cemgokmen.particles.models.amoebot.specializedparticles.SeparableAmoebotParticle;
import com.cemgokmen.particles.util.Utils;
import com.google.common.collect.ImmutableList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.List;
import java.util.function.Predicate;

public class SeparationAlgorithm extends ParticleAlgorithm {
    public static final List<Class<? extends ParticleCapability>> requiredCapabilities = ImmutableList.of(
            MovementCapable.class, NeighborDetectionCapable.class, UniformRandomDirectionCapable.class, SwapMovementCapable.class);

    public static final double DEFAULT_LAMBDA = 4.0;
    public static final double DEFAULT_ALPHA = 4.0;
    public static final boolean DEFAULT_SWAPS = true;
    public static final boolean DEFAULT_NONSWAPS = false;

    protected final DoubleProperty alpha = new SimpleDoubleProperty();
    protected final BooleanProperty swapsAllowed = new SimpleBooleanProperty();
    protected final BooleanProperty nonSwapsAllowed = new SimpleBooleanProperty();
    protected final DoubleProperty lambda = new SimpleDoubleProperty();

    public SeparationAlgorithm(double lambda, double alpha, boolean swapsAllowed, boolean nonSwapsAllowed) {
        this.setLambda(lambda);
        this.setAlpha(alpha);

        this.setSwapsAllowed(swapsAllowed);
        this.setNonSwapsAllowed(nonSwapsAllowed);
    }

    public SeparationAlgorithm() {
        this(DEFAULT_LAMBDA, DEFAULT_ALPHA, DEFAULT_SWAPS, DEFAULT_NONSWAPS);
    }

    public double getAlpha() {
        return this.alpha.get();
    }

    public DoubleProperty alphaProperty() {
        return this.alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha.set(alpha);
    }

    public boolean isSwapsAllowed() {
        return this.swapsAllowed.get();
    }

    public BooleanProperty swapsAllowedProperty() {
        return this.swapsAllowed;
    }

    public void setSwapsAllowed(boolean swapsAllowed) {
        this.swapsAllowed.set(swapsAllowed);
    }

    public boolean isNonSwapsAllowed() {
        return this.nonSwapsAllowed.get();
    }

    public BooleanProperty nonSwapsAllowedProperty() {
        return this.nonSwapsAllowed;
    }

    public void setNonSwapsAllowed(boolean nonSwapsAllowed) {
        this.nonSwapsAllowed.set(nonSwapsAllowed);
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

    @Override
    public void onParticleActivation(Particle p) {
        AmoebotParticle particle = (AmoebotParticle) p;

        // Pick a random direction
        ParticleGrid.Direction randomDirection = particle.getUniformRandomDirection();

        //System.out.println("Time for move validation");

        // Run move validation
        if (!RuleUtils.isMoveValidCompressionMove(particle, randomDirection, this.isSwapsAllowed(), this.isNonSwapsAllowed())) {
            //System.out.println("Invalid move, returning");
            return;
        }

        //System.out.println("Time for probability calculation");

        double moveProbability = this.getMoveProbability(particle, randomDirection);

        //System.out.printf("Move probability: %.2f%%. Now filtering.", moveProbability * 100);

        if (Utils.randomDouble() > moveProbability) {
            return;
        }

        // Now make the move
        if (this.isSwapsAllowed())
            particle.swapMove(randomDirection);
        else
            particle.move(randomDirection);
    }

    @Override
    public List<Class<? extends ParticleCapability>> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    @Override
    public boolean isGridValid(ParticleGrid grid) {
        return RuleUtils.checkParticleConnection(grid, particle -> true) && RuleUtils.checkParticleHoles(grid, particle -> true);
    }

    private class ClassNumberPredicate implements Predicate<Particle> {
        private final Particle particle;

        ClassNumberPredicate(Particle particle) {
            this.particle = particle;
        }

        @Override
        public boolean test(Particle p) {
            return p != null &&
                    p != this.particle &&
                    ((SeparableAmoebotParticle) p).getClassNumber() == ((SeparableAmoebotParticle) this.particle).getClassNumber();
        }
    }

    public double getMoveProbability(AmoebotParticle p, ParticleGrid.Direction inDirection) {
        ClassNumberPredicate filter = new ClassNumberPredicate(p);

        Particle nbr = p.getNeighborInDirection(inDirection, 0, null);
        if (nbr == null) {
            // This is a regular move
            int currentHomogeneousNeighbors = p.getNeighborParticles(false, filter).size();
            int futureHomogeneousNeighbors = p.getAdjacentPositionNeighborParticles(inDirection, false, filter).size();

            int currentNeighbors = p.getNeighborParticles(false, null).size();
            int futureNeighbors = p.getAdjacentPositionNeighborParticles(inDirection, false, particle -> particle
                    != p).size();

            return Math.pow(this.getAlpha(), futureHomogeneousNeighbors - currentHomogeneousNeighbors) * // Separation
                    Math.pow(this.getLambda(), futureNeighbors - currentNeighbors); // Compression
        } else {
            // This is a swap move
            ClassNumberPredicate nbrFilter = new ClassNumberPredicate(nbr);

            int currentHomogeneousNeighbors = p.getNeighborParticles(false, filter).size();
            int futureHomogeneousNeighbors = p.getAdjacentPositionNeighborParticles(inDirection, false, filter).size();
            futureHomogeneousNeighbors += (filter.test(nbr)) ? 1 : 0; // Consider the neighbor too since we swap with it

            int nbrCurrentHomogeneousNeighbors = p.getAdjacentPositionNeighborParticles(inDirection, false, nbrFilter).size();
            int nbrFutureHomogeneousNeighbors = p.getNeighborParticles(false, nbrFilter).size();
            nbrFutureHomogeneousNeighbors += (nbrFilter.test(p)) ? 1 : 0; // Consider p too since we swap with it

            return Math.pow(this.getAlpha(), futureHomogeneousNeighbors - currentHomogeneousNeighbors) * // For this particle
                    Math.pow(this.getAlpha(), nbrFutureHomogeneousNeighbors - nbrCurrentHomogeneousNeighbors); // For the swapped particle
        }
    }
}
