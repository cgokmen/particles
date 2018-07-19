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

import com.cemgokmen.particles.algorithms.ParticleAlgorithm;
import com.cemgokmen.particles.graphics.GridGraphics;
import com.cemgokmen.particles.util.PropertyUtils;
import com.cemgokmen.particles.util.Utils;
import com.cemgokmen.particles.models.ParticleGrid;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.primitives.Ints;
import javafx.beans.property.Property;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public class TrialUtils {
    public static Map<Number, File> runTrials(ParticleGrid grid, int[] stoppingPoints, Path targetPath, String imageExt) throws Exception {
        LinkedList<Integer> stops = new LinkedList<Integer>(Ints.asList(stoppingPoints));
        Collections.sort(stops);

        Map<Number, File> images = Maps.newHashMap();

        targetPath.toFile().mkdirs();

        while (!stops.isEmpty()) {
            int target = stops.remove();
            int toRun = target - grid.getActivationsRun();

            System.out.printf("    Next target: %d\n", target);
            grid.runActivations(toRun);
            File image = targetPath.resolve(grid.getActivationsRun() + "." + imageExt).toFile();
            GridGraphics.saveGridImage(grid, image);
            images.put(grid.getActivationsRun(), image);
            System.out.printf("    Saved target: %d\n\n", target);
        }

        return images;
    }

    public static Table<Number, Number, File> runPropertyValueTrials(Supplier<ParticleGrid> gridSupplier, Supplier<ParticleAlgorithm> algorithmSupplier, String propertyName, List<Number> propertyValues, int[] stoppingPoints, Path targetPath, String imageExt) throws Exception {
        Table<Number, Number, File> images = HashBasedTable.create();
        propertyValues.parallelStream().forEach(y -> {
            System.out.printf("Next value: %s=%s\n", propertyName, y.toString());
            try {
                ParticleAlgorithm algorithm = algorithmSupplier.get();

                final Property property = PropertyUtils.getPropertyWithName(algorithm, algorithm.getClass(), propertyName);
                property.setValue(y);

                final ParticleGrid grid = gridSupplier.get();
                grid.assignAllParticlesAlgorithm(algorithm);

                Map<Number, File> xImageMap = TrialUtils.runTrials(grid, stoppingPoints, targetPath.resolve(y + ""), imageExt);
                xImageMap.forEach((x, image) -> images.put(y, x, image));

                System.out.printf("Completed value: %s=%s\n\n", propertyName, y.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return images;
    }
}
