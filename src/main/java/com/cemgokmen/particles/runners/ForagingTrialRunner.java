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

import com.cemgokmen.particles.algorithms.ForagingAlgorithm;
import com.cemgokmen.particles.io.GridIO;
import com.cemgokmen.particles.io.HTMLGenerator;
import com.cemgokmen.particles.models.ParticleGrid;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;

public class ForagingTrialRunner {
    public static void main(String[] args) throws Exception {
        GridIO.ClassFilenameTuple system = GridIO.SAMPLE_SYSTEMS.get(GridIO.SampleSystems.AMOEBOT_100_1FOOD);

        /*
        PropertyName options for ForagingAlgorithm

        protected final DoubleProperty fedLambda = new SimpleDoubleProperty();
        protected final DoubleProperty hungryLambda = new SimpleDoubleProperty();

        protected final IntegerProperty foodLifetime = new SimpleIntegerProperty();
        protected final IntegerProperty foodTokenLifetime = new SimpleIntegerProperty();
        protected final IntegerProperty particleMaximumFedActivations = new SimpleIntegerProperty();
        */
        Path basePath = Paths.get("/Users/cgokmen/research/results/foragingtests/");

        ImmutableMap<String, List<Number>> propertyValues = new ImmutableMap.Builder<String, List<Number>>()
                .put("fedLambda", new ImmutableList.Builder<Number>().add(3.0, 4.0, 10.0).build())
                .put("hungryLambda", new ImmutableList.Builder<Number>().add(0.5, 0.75, 1.0).build())
                //.put("foodLifetime", new ImmutableList.Builder<Number>().add(3, 4, 10).build())
                .put("foodTokenLifetime", new ImmutableList.Builder<Number>().add(1, 4, 10).build())
                .put("particleMaximumFedActivations", new ImmutableList.Builder<Number>().add(100, 500, 1000).build())
                .build();

        // We want images at 0, 1000, 10000, 100000, 1000000, 10000000 iterations
        //int[] stopArray = new int[]{0, 1000, 10000, 100000, 1000000, 10000000};
        int[] stopArray = new int[]{0, 100};

        Supplier<ParticleGrid> gridSupplier = () -> {
            try {
                return GridIO.importParticlesFromResourceName(system.filename, system.klass);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        };

        for (Map.Entry<String, List<Number>> entry : propertyValues.entrySet()) {
            final String propertyName = entry.getKey();
            final Path propertyPath = basePath.resolve(propertyName);

            final Table<Number, Number, File> images = TrialUtils.runPropertyValueTrials(gridSupplier, ForagingAlgorithm.class, propertyName, entry.getValue(), stopArray, propertyPath, "png");

            File htmlFile = propertyPath.resolve("index.html").toFile();
            HTMLGenerator.saveHTML(htmlFile, "Foraging", propertyName, "Activations", images);
        }
    }
}
