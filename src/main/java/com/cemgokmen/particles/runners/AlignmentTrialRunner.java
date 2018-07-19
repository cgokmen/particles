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
import com.cemgokmen.particles.algorithms.ParticleAlgorithm;
import com.cemgokmen.particles.io.GridIO;
import com.cemgokmen.particles.io.SampleSystemMetadata;
import com.cemgokmen.particles.io.html.HTMLGenerator;
import com.cemgokmen.particles.models.ParticleGrid;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class AlignmentTrialRunner {
    public static void main(String[] args) throws Exception {
        SampleSystemMetadata system = SampleSystemMetadata.AMOEBOT_100_6DIRECTION_LARGE;

        /*
        PropertyName options for AlignmentAlgorithm

        protected final DoubleProperty rotationBias = new SimpleDoubleProperty();
        protected final DoubleProperty translationBias = new SimpleDoubleProperty();
        protected final DoubleProperty forwardBias = new SimpleDoubleProperty();
        */
        Path basePath = Paths.get("/Users/cgokmen/research/results/newalignmenttests/");

        ImmutableMap<String, List<Number>> propertyValues = new ImmutableMap.Builder<String, List<Number>>()
                .put("rotationBias", new ImmutableList.Builder<Number>().add(1.0, 5.0, 10.0, 20.0, 50.0).build())
                //.put("forwardBias", new ImmutableList.Builder<Number>().add(1.0, 1.1, 2.0, 5.0, 10.0).build())
                .build();

        // We want images at 0, 1000, 10000, 100000, 1000000, 10000000 iterations
        int[] stopArray = new int[]{0, 1000, 10000, 100000, 1000000, 10000000};

        Supplier<ParticleGrid> gridSupplier = () -> {
            try {
                return GridIO.importSampleSystem(system);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        };

        Supplier<ParticleAlgorithm> algorithmSupplier = new Supplier<ParticleAlgorithm>() {
            @Override
            public ParticleAlgorithm get() {
                return new AlignmentAlgorithm(20, 4, 1);
            }
        };

        for (Map.Entry<String, List<Number>> entry : propertyValues.entrySet()) {
            final String propertyName = entry.getKey();
            final Path propertyPath = basePath.resolve(propertyName);

            final Table<Number, Number, File> images = TrialUtils.runPropertyValueTrials(gridSupplier, algorithmSupplier, propertyName, entry.getValue(), stopArray, propertyPath, "png");

            File htmlFile = propertyPath.resolve("index.html").toFile();
            HTMLGenerator.saveHTML(htmlFile, "Alignment", propertyName, "Activations", images);
        }
    }
}
