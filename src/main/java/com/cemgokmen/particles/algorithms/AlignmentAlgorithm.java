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

import com.cemgokmen.particles.misc.RNG;
import com.cemgokmen.particles.misc.RandomSelector;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.AmoebotGrid;
import com.cemgokmen.particles.models.amoebot.AmoebotParticle;
import com.cemgokmen.particles.models.amoebot.DirectedAmoebotParticle;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AlignmentAlgorithm extends CompressionAlgorithm {
    public static final double DEFAULT_ROTATION_BIAS = 10.0;
    public static final double DEFAULT_TRANSLATION_BIAS = 5.0;
    public static final double DEFAULT_FORWARD_BIAS = 1.1;

    protected final DoubleProperty rotationBias = new SimpleDoubleProperty();
    protected final DoubleProperty translationBias = new SimpleDoubleProperty();
    protected final DoubleProperty forwardBias = new SimpleDoubleProperty();

    public double getRotationBias() {
        return this.rotationBias.get();
    }

    public DoubleProperty rotationBiasProperty() {
        return this.rotationBias;
    }

    public void setRotationBias(double rotationBias) {
        this.rotationBias.set(rotationBias);
    }

    public double getTranslationBias() {
        return this.translationBias.get();
    }

    public DoubleProperty translationBiasProperty() {
        return this.translationBias;
    }

    public void setTranslationBias(double translationBias) {
        this.translationBias.set(translationBias);
    }

    public double getForwardBias() {
        return this.forwardBias.get();
    }

    public DoubleProperty forwardBiasProperty() {
        return this.forwardBias;
    }

    public void setForwardBias(double forwardBias) {
        this.forwardBias.set(forwardBias);
    }

    public AlignmentAlgorithm(double rotationBias, double translationBias, double forwardBias) {
        this.setRotationBias(rotationBias);
        this.setTranslationBias(translationBias);
        this.setForwardBias(forwardBias);
    }

    public AlignmentAlgorithm() {
        this(DEFAULT_ROTATION_BIAS, DEFAULT_TRANSLATION_BIAS, DEFAULT_FORWARD_BIAS);
    }

    @Override
    public void onParticleActivation(Particle p) {
        if (!(p instanceof DirectedAmoebotParticle)) {
            throw new RuntimeException("This particle type is not compatible with AlignmentAlgorithm.");
        }

        DirectedAmoebotParticle particle = (DirectedAmoebotParticle) p;

        if (RNG.randomDouble() <= 0.5) {
            // With one half probability, we rotate
            ParticleGrid.Direction randomDirection = particle.getRandomDirection();

            double moveProbability = this.getRotateMoveProbability(particle, randomDirection);
            if (RNG.randomDouble() > moveProbability) {
                return;
            }

            particle.setDirection(randomDirection);
        } else {
            // With the rest probability, we translate

            // Pick a random direction using the correct weights
            RandomSelector<ParticleGrid.Direction> selector = RandomSelector.weighted(particle.compass.getDirections(), direction -> {
                double angle = particle.compass.getAngleBetweenDirections(direction, particle.getDirection());
                return Math.pow(this.getForwardBias(), normalizeDotProduct(Math.cos(angle)));
            });

            ParticleGrid.Direction randomDirection = selector.next(RNG.random);

            // Run move validation
            if (!this.isMoveValid(particle, randomDirection)) {
                return;
            }

            double moveProbability = this.getTranslateMoveProbability(particle, randomDirection);
            if (RNG.randomDouble() > moveProbability) {
                return;
            }

            // Now make the move
            try {
                particle.move(randomDirection, this.isSwapsAllowed(), this.isNonSwapsAllowed());
                //System.out.println("Moved.");
            } catch (Exception ignored) {

            }
        }
    }

    @Override
    public boolean isParticleAllowed(Particle p) {
        return p instanceof DirectedAmoebotParticle;
    }

    public double getRotateMoveProbability(DirectedAmoebotParticle p, ParticleGrid.Direction inDirection) {
        List<Particle> neighbors = p.getNeighborParticles(false, null);

        double sumDotProducts = getDotProductSum(neighbors, p.getDirection());
        double newSumDotProducts = getDotProductSum(neighbors, inDirection);

        return Math.pow(this.getRotationBias(), newSumDotProducts - sumDotProducts);
    }

    private static double getDotProductSum(List<Particle> particles, ParticleGrid.Direction withDirection) {
        double sumDotProducts = 0;

        for (Particle nbrAnonymous : particles) {
            DirectedAmoebotParticle nbr = (DirectedAmoebotParticle) nbrAnonymous;
            sumDotProducts += normalizeDotProduct(Math.cos(nbr.compass.getAngleBetweenDirections(withDirection, nbr.getDirection())));
        }

        return sumDotProducts;
    }

    public double getTranslateMoveProbability(DirectedAmoebotParticle p, ParticleGrid.Direction inDirection) {
        AmoebotGrid.AmoebotCompass compass = (AmoebotGrid.AmoebotCompass) p.compass;
        ParticleGrid.Direction particleDirection = p.getDirection();

        List<Particle> currentNeighbors = p.getNeighborParticles(false, null);
        List<Particle> futureNeighbors = p.getAdjacentPositionNeighborParticles(inDirection, false, particle -> particle
                != p);

        double translationBiasTerm = Math.pow(this.getTranslationBias(),
                futureNeighbors.size() - currentNeighbors.size());

        double rotationBiasExponent = getDotProductSum(futureNeighbors, p.getDirection()) - getDotProductSum(currentNeighbors, p.getDirection());
        double rotationBiasTerm = Math.pow(this.getRotationBias(), rotationBiasExponent);

        return translationBiasTerm * rotationBiasTerm;
    }

    @Override
    public double getCompressionBias(Particle p) {
        throw new RuntimeException("getCompressionBias method should not be used in AlignmentAlgorithm.");
    }

    @Override
    public double getMoveProbability(AmoebotParticle p, ParticleGrid.Direction inDirection) {
        throw new RuntimeException("getMoveProbability method should not be used in AlignmentAlgorithm.");
    }

    private static double normalizeDotProduct(double dot) {
        return (dot + 1) / 2.0;
    }
}
