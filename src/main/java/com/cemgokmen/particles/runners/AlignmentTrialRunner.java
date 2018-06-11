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
import com.cemgokmen.particles.algorithms.ForagingAlgorithm;
import com.cemgokmen.particles.io.GridIO;
import com.cemgokmen.particles.io.HTMLGenerator;
import com.cemgokmen.particles.models.ParticleGrid;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.google.common.primitives.Ints;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AlignmentTrialRunner {
    public static void main(String[] args) throws Exception {
        GridIO.ClassFilenameTuple system = GridIO.SAMPLE_SYSTEMS.get(GridIO.SampleSystems.AMOEBOT_100_6DIRECTION_LARGE);

        Path basePath = Paths.get("/Users/cgokmen/research/results/alignmenttests/");

        // We want images at 0, 1000, 10000, 100000, 1000000, 10000000 iterations
        int[] stopArray = new int[]{0, 20000, 40000, 60000, 80000, 100000};

        Supplier<ParticleGrid> gridSupplier = () -> {
            try {
                return GridIO.importParticlesFromResourceName(system.filename, system.klass);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        };

        // Prepare the algorithm
        AlignmentAlgorithm algorithm = new AlignmentAlgorithm();
        algorithm.setForwardBias(1.0);

        // Prepare the grid
        ParticleGrid grid = gridSupplier.get();
        grid.assignAllParticlesAlgorithm(algorithm);

        // Run without drift
        Table<Number, Number, File> images = HashBasedTable.create();
        Map<Number, File> beforeDrift = TrialUtils.runTrials(grid, stopArray, basePath, "nodrift.png");
        beforeDrift.forEach((x, image) -> images.put(0, x, image));

        /*algorithm.setForwardBias(1.1);

        int[] newStopArray = Ints.toArray(Ints.asList(stopArray).stream().map(i -> i + grid.getActivationsRun()).collect(Collectors.toList()));
        Map<Number, File> afterDrift = TrialUtils.runTrials(grid, newStopArray, basePath, "drift.png");
        afterDrift.forEach((x, image) -> images.put(0, x, image));*/

        File htmlFile = basePath.resolve("index.html").toFile();
        HTMLGenerator.saveHTML(htmlFile, "Alignment", "N/A", "Activations", images);
    }
}
