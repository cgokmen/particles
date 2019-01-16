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

package com.cemgokmen.particles.runners;

import com.cemgokmen.particles.algorithms.AlignmentAlgorithm;
import com.cemgokmen.particles.generators.RandomSystemGenerator;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.gridshapes.ToroidalAmoebotGrid;
import com.cemgokmen.particles.models.amoebot.specializedparticles.DirectedAmoebotParticle;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GenerationRunner {
    public static void main(String[] args) {
        ToroidalAmoebotGrid g = new ToroidalAmoebotGrid(25);
        final ParticleGrid.Compass compass = g.getCompass();
        List<Supplier<Particle>> directedParticleSuppliers = compass.getDirections().stream().map(d -> {
            return (Supplier<Particle>) () -> {
                DirectedAmoebotParticle p = new DirectedAmoebotParticle(compass, d, false);
                p.setAlgorithm(new AlignmentAlgorithm());
                return p;
            };
        }).collect(Collectors.toList());
        RandomSystemGenerator.addUniformWeightedParticles(g, directedParticleSuppliers, 100);
    }
}
