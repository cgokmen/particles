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

import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.AmoebotParticle;
import com.cemgokmen.particles.models.amoebot.specializedparticles.SeparableAmoebotParticle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.function.Predicate;

public class SeparationAlgorithm extends CompressionAlgorithm {
    public static final double DEFAULT_LAMBDA = 4.0;
    public static final double DEFAULT_ALPHA = 4.0;
    public static final boolean DEFAULT_SWAPS = true;
    public static final boolean DEFAULT_NONSWAPS = false;

    protected final DoubleProperty alpha = new SimpleDoubleProperty();
    protected final BooleanProperty swapsAllowed = new SimpleBooleanProperty();
    protected final BooleanProperty nonSwapsAllowed = new SimpleBooleanProperty();

    public SeparationAlgorithm(double lambda, double alpha, boolean swapsAllowed, boolean nonSwapsAllowed) {
        super(lambda);
        this.setAlpha(alpha);

        this.setSwapsAllowed(swapsAllowed);
        this.setNonSwapsAllowed(nonSwapsAllowed);
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

    @Override
    public boolean isSwapsAllowed() {
        return this.swapsAllowed.get();
    }

    public BooleanProperty swapsAllowedProperty() {
        return this.swapsAllowed;
    }

    public void setSwapsAllowed(boolean swapsAllowed) {
        this.swapsAllowed.set(swapsAllowed);
    }

    @Override
    public boolean isNonSwapsAllowed() {
        return this.nonSwapsAllowed.get();
    }

    public BooleanProperty nonSwapsAllowedProperty() {
        return this.nonSwapsAllowed;
    }

    public void setNonSwapsAllowed(boolean nonSwapsAllowed) {
        this.nonSwapsAllowed.set(nonSwapsAllowed);
    }

    public SeparationAlgorithm() {
        this(DEFAULT_LAMBDA, DEFAULT_ALPHA, DEFAULT_SWAPS, DEFAULT_NONSWAPS);
    }

    private class ClassNumberPredicate implements Predicate<Particle> {
        private final int classNumber;

        ClassNumberPredicate(int classNumber) {
            this.classNumber = classNumber;
        }

        @Override
        public boolean test(Particle particle) {
            return particle != null && ((SeparableAmoebotParticle) particle).getClassNumber() == this.classNumber;
        }
    }

    @Override
    public double getMoveProbability(AmoebotParticle p, ParticleGrid.Direction inDirection) {
        int classNumber = ((SeparableAmoebotParticle) p).getClassNumber();
        ClassNumberPredicate filter = new ClassNumberPredicate(classNumber);

        int currentHomogeneousNeighbors = p.getNeighborParticles(false, filter).size();
        int futureHomogeneousNeighbors = p.getAdjacentPositionNeighborParticles(inDirection, false, particle ->
                particle != p && filter.test(particle)).size();

        double probability = Math.pow(this.getAlpha(), futureHomogeneousNeighbors - currentHomogeneousNeighbors);

        Particle nbr = p.getNeighborInDirection(inDirection, 0, null);
        if (nbr == null) {
            int currentNeighbors = p.getNeighborParticles(false, null).size();
            int futureNeighbors = p.getAdjacentPositionNeighborParticles(inDirection, false, particle -> particle
                    != p).size();
            probability *= Math.pow(this.getCompressionBias(p), futureNeighbors - currentNeighbors);
        } else {
            int nbrClassNumber = ((SeparableAmoebotParticle) nbr).getClassNumber();
            ClassNumberPredicate nbrFilter = new ClassNumberPredicate(nbrClassNumber);

            int nbrCurrentHomogeneousNeighbors = p.getAdjacentPositionNeighborParticles(inDirection, false, nbrFilter).size();
            int nbrFutureHomogeneousNeighbors = p.getNeighborParticles(false, particle -> particle != p
                    && nbrFilter.test(particle)).size();

            probability *= Math.pow(this.getAlpha(), nbrFutureHomogeneousNeighbors - nbrCurrentHomogeneousNeighbors);
        }

        return probability;
    }

    @Override
    public boolean isParticleAllowed(Particle p) {
        return p instanceof SeparableAmoebotParticle;
    }
}
