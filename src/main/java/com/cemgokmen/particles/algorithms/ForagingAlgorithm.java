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
import com.cemgokmen.particles.models.amoebot.FoodAmoebotParticle;
import com.cemgokmen.particles.models.amoebot.ForagingAmoebotParticle;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.Map;

public class ForagingAlgorithm extends CompressionAlgorithm {
    public static final double DEFAULT_FED_LAMBDA = 4.0;
    public static final double DEFAULT_HUNGRY_LAMBDA = 1.0;

    public static final int DEFAULT_FOOD_LIFETIME = Integer.MAX_VALUE;
    public static final int DEFAULT_FOOD_TOKEN_LIFETIME = 4;
    public static final int DEFAULT_PARTICLE_MAXIMUM_FED_ACTIVATIONS = 500;

    protected final DoubleProperty fedLambda = new SimpleDoubleProperty();
    protected final DoubleProperty hungryLambda = new SimpleDoubleProperty();

    protected final IntegerProperty foodLifetime = new SimpleIntegerProperty();
    protected final IntegerProperty foodTokenLifetime = new SimpleIntegerProperty();
    protected final IntegerProperty particleMaximumFedActivations = new SimpleIntegerProperty();


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

    @Override
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
                    ParticleGrid.Direction randomDirection = particle.getRandomDirection();
                    ForagingAmoebotParticle nbr = (ForagingAmoebotParticle) particle.getNeighborInDirection(randomDirection, 0, particle1 -> particle1 instanceof ForagingAmoebotParticle);
                    if (nbr != null) {
                        nbr.giveFoodToken(token, this.getParticleMaximumFedActivations());
                    }
                }
            }

            particle.incrementLastFedActivationsAgo();
            particle.decrementFedActivations();

            super.onParticleActivation(p);
        }
    }

    @Override
    public boolean isSwapsAllowed() {
        return true;
    }

    @Override
    public boolean isNonSwapsAllowed() {
        return true;
    }

    @Override
    public boolean isParticleAllowed(Particle p) {
        return p instanceof ForagingAmoebotParticle || p instanceof FoodAmoebotParticle;
    }

    @Override
    public boolean isMoveValid(AmoebotParticle p, ParticleGrid.Direction d) {
        // Do not swap with food particles
        Particle nbr = p.getNeighborInDirection(d, 0, null);
        if (nbr != null && !nbr.getClass().equals(p.getClass())) {
            return false;
        }

        return super.isMoveValid(p, d);
    }

    @Override
    public Map<String, String> getInformation(ParticleGrid g) {
        Map<String, String> info = super.getInformation(g);

        int longestWaiting = 0;
        for (Particle p : g.getAllParticles()) {
            if (p instanceof ForagingAmoebotParticle) {
                longestWaiting = Math.max(longestWaiting, ((ForagingAmoebotParticle) p).getLongestLastFedActivationsAgo());
            }
        }

        info.put("Longest un-fed wait so far", longestWaiting + "");
        return info;
    }
}
