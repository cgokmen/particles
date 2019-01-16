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
import com.cemgokmen.particles.models.continuous.ContinuousParticle;
import com.cemgokmen.particles.models.continuous.ContinuousParticleGrid;
import com.cemgokmen.particles.models.continuous.boundary.CircularBoundary;
import com.cemgokmen.particles.models.continuous.boundary.ContinuousParticleGridBoundary;
import com.cemgokmen.particles.util.Utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GeneratedContinuousGridTrialRunner {
    private static final int PARTICLE_COUNT = 10000;
    private static final int GRID_SIZE = 2 * (int) Math.sqrt(PARTICLE_COUNT);
    private static final Path basePath = Paths.get("/Users/cgokmen/research/results/continuous-10-8--2/");

    public static void main(String[] args) throws Exception {
        ContinuousParticleGridBoundary boundary = new CircularBoundary(30);
        ContinuousParticleGrid grid = new ContinuousParticleGrid(boundary);

        AlignmentAlgorithm algorithm = new AlignmentAlgorithm(4, 1, 0.5);

        Stream<Particle> randomlyDirectedParticles = Utils.random.doubles().mapToObj(d -> new ContinuousParticle(0.5, d * Math.PI * 2));

        ContinuousParticleGridBoundary smallerBoundary = new CircularBoundary(15);
        RandomSystemGenerator.addParticles(grid, randomlyDirectedParticles, v -> smallerBoundary.isVectorInBoundary(v, 0.5), 100);

        grid.assignAllParticlesAlgorithm(algorithm);

        int iterations = 20000000;
        int seconds = 20;
        int fps = 24;

        int frames = seconds * fps;
        int step = iterations / frames;

        int[] stops = IntStream.rangeClosed(0, frames).map(k -> step * k).toArray();

        TrialUtils.runTrials(grid, stops, basePath, "pdf");
    }
}
