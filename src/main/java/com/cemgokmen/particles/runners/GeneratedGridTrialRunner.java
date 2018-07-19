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
import com.cemgokmen.particles.algorithms.ParticleAlgorithm;
import com.cemgokmen.particles.generators.RandomSystemGenerator;
import com.cemgokmen.particles.io.GridIO;
import com.cemgokmen.particles.io.SampleSystemMetadata;
import com.cemgokmen.particles.io.html.HTMLGenerator;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.AmoebotGrid;
import com.cemgokmen.particles.models.amoebot.AmoebotParticle;
import com.cemgokmen.particles.models.amoebot.gridshapes.QuadrilateralAmoebotGrid;
import com.cemgokmen.particles.models.amoebot.specializedparticles.DirectedAmoebotParticle;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GeneratedGridTrialRunner {
    private static final int PARTICLE_COUNT = 10000;
    private static final int GRID_SIZE = 2 * (int) Math.sqrt(PARTICLE_COUNT);
    private static final Path basePath = Paths.get("/Users/cgokmen/research/results/generationtests/");

    public static void main(String[] args) throws Exception {
        AmoebotGrid grid = new QuadrilateralAmoebotGrid(GRID_SIZE);
        ParticleGrid.Compass compass = grid.getCompass();
        List<ParticleGrid.Direction> directions = compass.getDirections();
        AlignmentAlgorithm alg = new AlignmentAlgorithm();

        List<Supplier<Particle>> suppliers = IntStream.range(0, 6).boxed()
                .map(i ->
                        (Supplier<Particle>) () -> {
                            Particle p = new DirectedAmoebotParticle(compass, directions.get(i), false);
                            p.setAlgorithm(alg);
                            return p; }
                ).collect(Collectors.toList());

        RandomSystemGenerator.addUniformWeightedParticles(grid, suppliers, PARTICLE_COUNT);

        grid.assignAllParticlesAlgorithm(new AlignmentAlgorithm());

        TrialUtils.runTrials(grid, new int[]{0}, basePath, "pdf");
    }
}
