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
import com.cemgokmen.particles.models.amoebot.specializedparticles.DirectedAmoebotParticle;
import com.cemgokmen.particles.util.RandomSelector;
import com.cemgokmen.particles.util.Utils;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class BobBotAlignmentAlgorithm extends ParticleAlgorithm {
    // TODO: FIX THIS
    public static final List<Class<? extends ParticleCapability>> requiredCapabilities = ImmutableList.of(
            MovementCapable.class, NeighborDetectionCapable.class, UniformRandomDirectionCapable.class, SpinCapable.class);

    @Override
    public void onParticleActivation(Particle p) {
        // Do the rejection here
        DirectedAmoebotParticle particle = (DirectedAmoebotParticle) p;
        double dotProdSum = particle.getCompass().getDirections().stream()
                .mapToDouble(d -> {
                    boolean occupied = particle.getNeighborInDirection(d, 0, null) != null;
                    if (!occupied) return 0;

                    double angle = particle.getCompass().getAngleBetweenDirections(d, ((DirectedAmoebotParticle) p).getDirection());
                    return -1 * Math.cos(angle);
                })
                .sum();

        double moveProb = 0.2 + 0.1 * dotProdSum;
        if (Utils.randomDouble() > moveProb) return;

        if (Utils.randomDouble() <= 0.5) {
            // With one half probability, we rotate
            ParticleGrid.Direction randomDirection = particle.getUniformRandomDirection();

            particle.setDirection(randomDirection);
        } else {
            // With the rest probability, we translate

            // Pick a random direction using the correct weights
            RandomSelector<ParticleGrid.Direction> selector = RandomSelector.uniform(particle.getCompass().getDirections());

            ParticleGrid.Direction randomDirection = selector.next(Utils.random);

            // Run move validation
            if (!particle.isDirectionWithinBounds(randomDirection)) {
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

    @Override
    public boolean isGridValid(ParticleGrid grid) {
        return true;
    }
}
