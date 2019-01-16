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
import com.cemgokmen.particles.util.Utils;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.AmoebotGrid;
import com.cemgokmen.particles.models.amoebot.specializedparticles.DirectedAmoebotParticle;
import com.google.common.collect.ImmutableList;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.List;

public class AlignmentAlgorithm extends ParticleAlgorithm {
    public static final double DEFAULT_ROTATION_BIAS = 20.0;
    public static final double DEFAULT_TRANSLATION_BIAS = 1.0;
    public static final double DEFAULT_FORWARD_BIAS = 1.1;
    public static final double DEFAULT_LAMBDA = 4.0;

    public static final List<Class<? extends ParticleCapability>> requiredCapabilities = ImmutableList.of(
            MovementCapable.class, NeighborDetectionCapable.class, UniformRandomDirectionCapable.class, SpinCapable.class, WrappedNormalRandomDirectionCapable.class);

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
        double spinProbability = 0.1;

        // Check if we're about to hit the wall
        /* ParticleGrid.Direction faceDir = ((SpinCapable) p).getDirection();
        if (!((NeighborDetectionCapable) p).isDirectionInBounds(faceDir)) {
            spinProbability = 0.5;
        } */

        if (Utils.randomDouble() <= spinProbability) {
            // With one half probability, we rotate
            ParticleGrid.Direction randomDirection = ((UniformRandomDirectionCapable) p).getUniformRandomDirection();

            double moveProbability = this.getRotateMoveProbability(p, randomDirection);
            if (Utils.randomDouble() > moveProbability) {
                return;
            }

            ((SpinCapable) p).setDirection(randomDirection);
        } else {
            // With the rest probability, we translate

            // Pick a random direction using the correct weights
            ParticleGrid.Direction currentDirection = ((SpinCapable) p).getDirection();
            ParticleGrid.Direction randomDirection = ((WrappedNormalRandomDirectionCapable) p).getWrappedNormalRandomDirection(currentDirection, this.getForwardBias());

            // Run move validation
            if (!((MovementCapable) p).isDirectionWithinBounds(randomDirection)) {
                return;
            }

            /*if (false && !RuleUtils.isMoveValidCompressionMove(particle, randomDirection, false, true)) {
                return;
            }*/

            double moveProbability = this.getTranslateMoveProbability(p, randomDirection);
            if (Utils.randomDouble() > moveProbability) {
                return;
            }

            // Now make the move
            try {
                ((MovementCapable) p).move(randomDirection);
            } catch (Exception ignored){}
        }
    }

    @Override
    public List<Class<? extends ParticleCapability>> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    private double getRotateMoveProbability(Particle p, ParticleGrid.Direction inDirection) {
        List<Particle> neighbors = ((NeighborDetectionCapable) p).getNeighborParticles(false, null);

        double sumDotProducts = getDotProductSum(neighbors, ((SpinCapable) p).getDirection());
        double newSumDotProducts = getDotProductSum(neighbors, inDirection);

        return Math.pow(this.getRotationBias(), newSumDotProducts - sumDotProducts);
    }

    private static double getDotProductSum(List<Particle> particles, ParticleGrid.Direction withDirection) {
        double sumDotProducts = 0;

        for (Particle nbrAnonymous : particles) {
            SpinCapable nbr = (SpinCapable) nbrAnonymous;
            sumDotProducts += normalizeDotProduct(Math.cos(nbr.getCompass().getAngleBetweenDirections(withDirection, nbr.getDirection())));
        }

        return sumDotProducts;
    }

    private double getTranslateMoveProbability(Particle p, ParticleGrid.Direction inDirection) {
        ParticleGrid.Direction particleDirection = ((SpinCapable) p).getDirection();

        List<Particle> currentNeighbors = ((NeighborDetectionCapable) p).getNeighborParticles(false, null);
        List<Particle> futureNeighbors = ((NeighborDetectionCapable) p).getAdjacentPositionNeighborParticles(inDirection, false, particle -> particle
                != p);

        double translationBiasTerm = Math.pow(this.getTranslationBias(), futureNeighbors.size() - currentNeighbors.size());

        double rotationBiasExponent = getDotProductSum(futureNeighbors, ((SpinCapable) p).getDirection()) - getDotProductSum(currentNeighbors, ((SpinCapable) p).getDirection());
        double rotationBiasTerm = Math.pow(this.getRotationBias(), rotationBiasExponent);

        return translationBiasTerm * rotationBiasTerm;
    }

    private static double normalizeDotProduct(double dot) {
        return (dot + 1) / 2.0;
    }

    @Override
    public boolean isGridValid(ParticleGrid grid) {
        return true;
    }
}
