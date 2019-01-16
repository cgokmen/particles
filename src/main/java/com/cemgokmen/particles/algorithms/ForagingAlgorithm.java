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
import com.cemgokmen.particles.models.amoebot.specializedparticles.FoodAmoebotParticle;
import com.cemgokmen.particles.models.amoebot.specializedparticles.ForagingAmoebotParticle;
import com.cemgokmen.particles.util.Utils;
import com.google.common.collect.ImmutableList;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ForagingAlgorithm extends ParticleAlgorithm {
    public static final List<Class<? extends ParticleCapability>> requiredCapabilities = ImmutableList.of(MovementCapable.class, UniformRandomDirectionCapable.class, NeighborDetectionCapable.class);

    public static final double DEFAULT_FED_LAMBDA = 4.0;
    public static final double DEFAULT_HUNGRY_LAMBDA = 1.0;

    public static final int DEFAULT_FOOD_LIFETIME = Integer.MAX_VALUE;
    public static final int DEFAULT_FOOD_TOKEN_LIFETIME = 4;
    public static final int DEFAULT_PARTICLE_MAXIMUM_FED_ACTIVATIONS = 500;
    public static final double DEFAULT_LAMBDA = 4.0;

    protected final DoubleProperty fedLambda = new SimpleDoubleProperty();
    protected final DoubleProperty hungryLambda = new SimpleDoubleProperty();

    protected final IntegerProperty foodLifetime = new SimpleIntegerProperty();
    protected final IntegerProperty foodTokenLifetime = new SimpleIntegerProperty();
    protected final IntegerProperty particleMaximumFedActivations = new SimpleIntegerProperty();
    protected final DoubleProperty lambda = new SimpleDoubleProperty();


    public ForagingAlgorithm(double fedLambda, double hungryLambda, int foodLifetime, int foodTokenLifetime, int particleMaximumFedActivations) {
        this.setFedLambda(fedLambda);
        this.setHungryLambda(hungryLambda);
        this.setFoodLifetime(foodLifetime);
        this.setFoodTokenLifetime(foodTokenLifetime);
        this.setParticleMaximumFedActivations(particleMaximumFedActivations);
    }

    public ForagingAlgorithm() {
        this(DEFAULT_FED_LAMBDA, DEFAULT_HUNGRY_LAMBDA, DEFAULT_FOOD_LIFETIME, DEFAULT_FOOD_TOKEN_LIFETIME, DEFAULT_PARTICLE_MAXIMUM_FED_ACTIVATIONS);
    }

    public double getFedLambda() {
        return this.fedLambda.get();
    }

    public DoubleProperty fedLambdaProperty() {
        return this.fedLambda;
    }

    public void setFedLambda(double fedLambda) {
        this.fedLambda.set(fedLambda);
    }

    public double getHungryLambda() {
        return this.hungryLambda.get();
    }

    public DoubleProperty hungryLambdaProperty() {
        return this.hungryLambda;
    }

    public void setHungryLambda(double hungryLambda) {
        this.hungryLambda.set(hungryLambda);
    }

    public int getFoodLifetime() {
        return this.foodLifetime.get();
    }

    public IntegerProperty foodLifetimeProperty() {
        return this.foodLifetime;
    }

    public void setFoodLifetime(int foodLifetime) {
        this.foodLifetime.set(foodLifetime);
    }

    public int getFoodTokenLifetime() {
        return this.foodTokenLifetime.get();
    }

    public IntegerProperty foodTokenLifetimeProperty() {
        return this.foodTokenLifetime;
    }

    public void setFoodTokenLifetime(int foodTokenLifetime) {
        this.foodTokenLifetime.set(foodTokenLifetime);
    }

    public int getParticleMaximumFedActivations() {
        return this.particleMaximumFedActivations.get();
    }

    public IntegerProperty particleMaximumFedActivationsProperty() {
        return this.particleMaximumFedActivations;
    }

    public void setParticleMaximumFedActivations(int particleMaximumFedActivations) {
        this.particleMaximumFedActivations.set(particleMaximumFedActivations);
    }

    public double getCompressionBias(Particle p) {
        if (p instanceof FoodAmoebotParticle) {
            return 1;
        }

        return ((ForagingAmoebotParticle) p).isFed() ? this.getFedLambda() : this.getHungryLambda();
    }

    @Override
    public void onParticleActivation(Particle p) {
        if (p instanceof FoodAmoebotParticle) {
            ((FoodAmoebotParticle) p).getNeighborParticles(false, particle -> particle instanceof ForagingAmoebotParticle).forEach(particle -> ((ForagingAmoebotParticle) particle).giveFoodToken(this.getFoodTokenLifetime(), this.getParticleMaximumFedActivations()));
            ((FoodAmoebotParticle) p).decrementLifetime(this.getFoodLifetime());

            // Does p have neighbors?
            //boolean pConnected = ((FoodAmoebotParticle) p).getNeighborParticles(false, x -> !(x instanceof FoodAmoebotParticle)).size() > 0;

            // Message passing hack right here. First, clear everyone.
            /*ParticleGrid g = p.getGrid();
            if (pConnected)
                g.getAllParticles().filter(x -> x instanceof ForagingAmoebotParticle).forEach(x -> ((ForagingAmoebotParticle) x).giveFoodToken(this.getFoodTokenLifetime(), this.getParticleMaximumFedActivations()));
            else
                g.getAllParticles().filter(x -> x instanceof ForagingAmoebotParticle).forEach(x -> ((ForagingAmoebotParticle) x).giveFoodToken(0, 0));*/

            // Then, force the message onto everyone important.
            // TODO: Maybe do a search here?
        } else if (p instanceof ForagingAmoebotParticle) {
            // Do the feeding first
            ForagingAmoebotParticle particle = (ForagingAmoebotParticle) p;

            /*if (particle.getNeighborParticles(false, particle1 -> particle1 instanceof FoodAmoebotParticle).size() > 0) particle.feed();
            else particle.decrementFedActivations();*/

            if (particle.hasFoodToken()) {
                // Pass it on
                int token = particle.getFoodToken();
                --token;
                if (token > 0) {
                    ParticleGrid.Direction randomDirection = particle.getUniformRandomDirection();
                    ForagingAmoebotParticle nbr = (ForagingAmoebotParticle) particle.getNeighborInDirection(randomDirection, 0, particle1 -> particle1 instanceof ForagingAmoebotParticle);
                    if (nbr != null) {
                        nbr.giveFoodToken(token, this.getParticleMaximumFedActivations());
                    }
                }
            }

            particle.incrementLastFedActivationsAgo();
            particle.decrementFedActivations();

            // TODO: CALL COMPRESSION'S METHOD
            // Pick a random direction
            ParticleGrid.Direction randomDirection = particle.getUniformRandomDirection();

            // Run move validation
            if (!this.isMoveValid(particle, randomDirection)) {
                return;
            }

            double moveProbability = this.getMoveProbability(particle, randomDirection);
            if (Utils.randomDouble() > moveProbability) {
                return;
            }

            // Now make the move
            try {
                particle.move(randomDirection);
            } catch (Exception ignored) {

            }
        }
    }

    @Override
    public List<Class<? extends ParticleCapability>> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    public boolean isSwapsAllowed() {
        return true;
    }

    public boolean isNonSwapsAllowed() {
        return true;
    }

    public boolean isMoveValid(AmoebotParticle p, ParticleGrid.Direction d) {
        // Do not swap with food particles
        Particle nbr = p.getNeighborInDirection(d, 0, x -> x instanceof FoodAmoebotParticle);
        if (nbr != null) {
            return false;
        }

        return RuleUtils.isMoveValidCompressionMove(p, d, false, true, x -> x instanceof ForagingAmoebotParticle);
    }

    @Override
    public Map<String, String> getInformation(ParticleGrid g) {
        Map<String, String> info = super.getInformation(g);

        int longestWaiting = g.getAllParticles()
                .filter(p -> p instanceof ForagingAmoebotParticle)
                .map(p -> ((ForagingAmoebotParticle) p).getLongestLastFedActivationsAgo())
                .max(Comparator.comparing(Integer::valueOf))
                .get();

        info.put("Longest un-fed wait so far", longestWaiting + "");
        return info;
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
}
