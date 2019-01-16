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
import com.cemgokmen.particles.models.amoebot.AmoebotGrid;
import com.cemgokmen.particles.models.amoebot.specializedparticles.ContinuousDirectedAmoebotParticle;
import com.cemgokmen.particles.util.RandomSelector;
import com.cemgokmen.particles.util.Utils;
import com.google.common.collect.ImmutableList;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ContinuousAlignmentAlgorithm extends ParticleAlgorithm {
    public static final double DEFAULT_ROTATION_BIAS = 20.0;
    public static final double DEFAULT_TRANSLATION_BIAS = 1.0;
    public static final double DEFAULT_FORWARD_BIAS = 1.1;
    public static final double DEFAULT_LAMBDA = 4.0;
    public static final List<Double> POSSIBLE_ANGLES = IntStream.range(0, 360).mapToDouble(x -> x * Math.PI / 180.0).boxed().collect(Collectors.toList());
    public static final List<Class<? extends ParticleCapability>> requiredCapabilities = ImmutableList.of(
            MovementCapable.class, NeighborDetectionCapable.class, UniformRandomDirectionCapable.class, SpinCapable.class);

    // TODO: SAMPLE FROM A WRAPPED CAUCHY DISTRIBUTION OR A TRUNCATED NORMAL OR A VON MISES

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

    public ContinuousAlignmentAlgorithm(double rotationBias, double translationBias, double forwardBias) {
        this.setRotationBias(rotationBias);
        this.setTranslationBias(translationBias);
        this.setForwardBias(forwardBias);
    }

    public ContinuousAlignmentAlgorithm() {
        this(DEFAULT_ROTATION_BIAS, DEFAULT_TRANSLATION_BIAS, DEFAULT_FORWARD_BIAS);
    }

    @Override
    public void onParticleActivation(Particle p) {
        ContinuousDirectedAmoebotParticle particle = (ContinuousDirectedAmoebotParticle) p;

        if (Utils.randomDouble() <= 0.1) {
            // With one half probability, we rotate
            double randomDirection = Utils.randomDouble() * Math.PI * 2;

            double moveProbability = this.getRotateMoveProbability(particle, randomDirection);
            if (Utils.randomDouble() > moveProbability) {
                return;
            }

            particle.setDirection(randomDirection);

            //RandomSelector<Double> selector = RandomSelector.weighted(POSSIBLE_ANGLES, direction -> this.getRotateMoveProbability(particle, direction));
            //particle.setDirection(selector.next(Utils.random));
        } else {
            // With the rest probability, we translate

            // Pick a random direction using the correct weights
            RandomSelector<ParticleGrid.Direction> selector = RandomSelector.weighted(particle.compass.getDirections(), direction -> {
                double angle = angleDifference(directionToRadians(direction, particle.compass), particle.getDirection());
                return Math.pow(this.getForwardBias(), normalizeDotProduct(Math.cos(angle)));
            });

            ParticleGrid.Direction randomDirection = selector.next(Utils.random);

            // Run move validation
            if (!particle.isDirectionWithinBounds(randomDirection)) {
                return;
            }

            if (false && !RuleUtils.isMoveValidCompressionMove(particle, randomDirection, false, true)) {
                return;
            }

            double moveProbability = this.getTranslateMoveProbability(particle, randomDirection);
            if (Utils.randomDouble() > moveProbability) {
                return;
            }

            // Now make the move
            try {
                particle.move(randomDirection);
                //particle.setDirection(particle.compass.shiftDirectionCounterclockwise(particle.getDirection(), 3));
                //System.out.println("Moved.");
            } catch (Exception ignored) {

            }
        }
    }

    @Override
    public List<Class<? extends ParticleCapability>> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    private double getRotateMoveProbability(ContinuousDirectedAmoebotParticle p, double inDirection) {
        List<Particle> neighbors = p.getNeighborParticles(false, null);

        double sumDotProducts = getDotProductSum(neighbors, p.getDirection());
        double newSumDotProducts = getDotProductSum(neighbors, inDirection);

        return Math.pow(this.getRotationBias(), newSumDotProducts - sumDotProducts);
    }

    private static double getDotProductSum(List<Particle> particles, double withDirection) {
        double sumDotProducts = 0;

        for (Particle nbrAnonymous : particles) {
            ContinuousDirectedAmoebotParticle nbr = (ContinuousDirectedAmoebotParticle) nbrAnonymous;
            sumDotProducts += normalizeDotProduct(Math.cos(angleDifference(withDirection, nbr.getDirection())));
        }

        return sumDotProducts;
    }

    private double getTranslateMoveProbability(ContinuousDirectedAmoebotParticle p, ParticleGrid.Direction inDirection) {
        AmoebotGrid.AmoebotCompass compass = (AmoebotGrid.AmoebotCompass) p.compass;
        double particleDirection = p.getDirection();
        double inDirectionRadians = directionToRadians(inDirection, p.compass);

        List<Particle> currentNeighbors = p.getNeighborParticles(false, null);
        List<Particle> futureNeighbors = p.getAdjacentPositionNeighborParticles(inDirection, false, particle -> particle
                != p);

        double translationBiasTerm = Math.pow(this.getTranslationBias(), futureNeighbors.size() - currentNeighbors.size());

        double rotationBiasExponent = getDotProductSum(futureNeighbors, p.getDirection()) - getDotProductSum(currentNeighbors, p.getDirection());
        double rotationBiasTerm = Math.pow(this.getRotationBias(), rotationBiasExponent);

        return translationBiasTerm * rotationBiasTerm;
    }

    private static double oldDotProductSum(List<Particle> particles, ParticleGrid.Direction withDirection) {
        double sumDotProducts = 0;

        for (Particle nbrAnonymous : particles) {
            ContinuousDirectedAmoebotParticle nbr = (ContinuousDirectedAmoebotParticle) nbrAnonymous;
            sumDotProducts += normalizeDotProduct(Math.cos(nbr.compass.getAngleBetweenDirections(withDirection, discretize((AmoebotGrid.AmoebotCompass) nbr.compass, nbr.getDirection()))));
        }

        return sumDotProducts;
    }

    private double oldTranslateMoveProbability(ContinuousDirectedAmoebotParticle p, ParticleGrid.Direction inDirection) {
        AmoebotGrid.AmoebotCompass compass = (AmoebotGrid.AmoebotCompass) p.compass;

        List<Particle> currentNeighbors = p.getNeighborParticles(false, null);
        List<Particle> futureNeighbors = p.getAdjacentPositionNeighborParticles(inDirection, false, particle -> particle
                != p);

        double translationBiasTerm = Math.pow(this.getTranslationBias(), futureNeighbors.size() - currentNeighbors.size());

        double rotationBiasExponent = oldDotProductSum(futureNeighbors, discretize(compass, p.getDirection())) - oldDotProductSum(currentNeighbors, discretize(compass, p.getDirection()));
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

    public static ParticleGrid.Direction discretize(AmoebotGrid.AmoebotCompass c, double d) {
        double val = d / (Math.PI * 2) * (c.getDirections().size() - 1);
        return c.getDirections().get(Math.round((float) val));
    }

    private static double directionToRadians(ParticleGrid.Direction d, ParticleGrid.Compass c) {
        // Assumes that the first element in compass is the 0 angle.
        // For now, also assumes uniform angles.
        double step = 360.0 / c.getDirections().size();
        return step * c.getDirections().indexOf(d);
    }

    private static double angleDifference(double a, double b) {
        double diff = Math.abs(a - b) % (Math.PI * 2);
        if (diff > Math.PI) diff = Math.PI * 2 - diff;
        return diff;
    }
}
