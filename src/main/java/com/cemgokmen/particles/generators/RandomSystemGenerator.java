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

package com.cemgokmen.particles.generators;

import com.cemgokmen.particles.algorithms.ParticleAlgorithm;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.util.RandomSelector;
import com.cemgokmen.particles.util.Utils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.la4j.Vector;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class RandomSystemGenerator {
    public static void addParticles(ParticleGrid grid, Map<Supplier<Particle>, Double> suppliers, int count) {
        int addedCount = 0;

        // Make the supplier randomizer
        RandomSelector<Supplier<Particle>> randomSupplier = RandomSelector.weighted(suppliers.keySet(), suppliers::get);
        RandomSelector<Vector> positionSupplier = RandomSelector.uniform(grid.getValidPositions());

        while (addedCount < count) {
            try {
                // Get a valid position
                Vector position = positionSupplier.next(Utils.random);

                // Check if it is occupied
                if (grid.isPositionOccupied(position)) continue;

                // Get a particle supplier
                Supplier<Particle> supplier = randomSupplier.next(Utils.random);

                // Get a particle
                Particle particle = supplier.get();

                // Add the particle
                grid.addParticle(particle, position);

                // Validate the grid
                ParticleAlgorithm algorithm = particle.getAlgorithm();
                if (algorithm != null) {
                    if (!algorithm.isGridValid(grid)) {
                        grid.removeParticle(particle);

                        continue;
                    }
                }
                addedCount++;

                System.out.println(addedCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addUniformWeightedParticles(ParticleGrid grid, List<Supplier<Particle>> suppliers, int count) {
        final Map<Supplier<Particle>, Double> map = Maps.newHashMap();
        suppliers.forEach(supplier -> map.put(supplier, 1.0));

        addParticles(grid, map, count);
    }

    public static void addSingleTypeParticles(ParticleGrid grid, Supplier<Particle> supplier, int count) {
        addParticles(grid, new ImmutableMap.Builder<Supplier<Particle>, Double>().put(supplier, 1.0).build(), count);
    }
}
