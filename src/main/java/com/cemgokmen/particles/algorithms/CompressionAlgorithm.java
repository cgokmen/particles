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
import com.cemgokmen.particles.util.Utils;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class CompressionAlgorithm extends ParticleAlgorithm {
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

    public double getCompressionBias(Particle p) {
        return this.getLambda();
    }

    @Override
    public void onParticleActivation(Particle p) {
        if (!(p instanceof AmoebotParticle)) {
            throw new RuntimeException("This particle type is not compatible with CompressionAlgorithm.");
        }

        AmoebotParticle particle = (AmoebotParticle) p;

        // Pick a random direction
        ParticleGrid.Direction randomDirection = particle.getRandomDirection();

        //System.out.println("Time for move validation");

        // Run move validation
        if (!this.isMoveValid(particle, randomDirection)) {
            //System.out.println("Invalid move, returning");
            return;
        }

        //System.out.println("Time for probability calculation");

        double moveProbability = this.getMoveProbability(particle, randomDirection);

        //System.out.printf("Move probability: %.2f%%. Now filtering.", moveProbability * 100);

        if (Utils.randomDouble() > moveProbability) {
            return;
        }

        //System.out.println("Passed filter");

        // Now make the move
        try {
            particle.move(randomDirection, this.isSwapsAllowed(), this.isNonSwapsAllowed());
            //System.out.println("Moved.");
        } catch (Exception ignored) {

        }
    }

    public boolean isSwapsAllowed() {
        return false;
    }

    public boolean isNonSwapsAllowed() {
        return true;
    }

    @Override
    public boolean isParticleAllowed(Particle p) {
        return p instanceof AmoebotParticle;
    }

    @Override
    public boolean isGridValid(ParticleGrid grid) {
        return RuleUtils.checkParticleConnection(grid, particle -> true) && RuleUtils.checkParticleHoles(grid, particle -> true);
    }

    public double getMoveProbability(AmoebotParticle p, ParticleGrid.Direction inDirection) {
        int currentNeighbors = p.getNeighborParticles(false, null).size();
        int futureNeighbors = p.getAdjacentPositionNeighborParticles(inDirection, false, particle -> particle
                != p).size();
        return Math.pow(this.getCompressionBias(p), futureNeighbors - currentNeighbors);
    }

    public boolean isMoveValid(AmoebotParticle p, ParticleGrid.Direction d) {
        if (!p.isDirectionWithinBounds(d)) {
            return false;
        }
        boolean isOccupied = p.getNeighborInDirection(d, 0, null) != null;
        if ((isOccupied && !this.isSwapsAllowed()) || (!isOccupied && !this.isNonSwapsAllowed())) {
            return false;
        }

        boolean cond1 = p.getNeighborParticles(false, null).size() < 5;
        boolean cond2 = RuleUtils.checkProperty1(p, d, null);
        boolean cond3 = RuleUtils.checkProperty2(p, d, null);

        return cond1 && (cond2 || cond3);
    }

}
