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

package com.cemgokmen.particles.io;

import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.*;
import com.cemgokmen.particles.models.amoebot.gridshapes.HexagonalAmoebotGrid;
import com.cemgokmen.particles.models.amoebot.gridshapes.QuadrilateralAmoebotGrid;
import com.cemgokmen.particles.models.amoebot.gridshapes.ToroidalAmoebotGrid;
import com.cemgokmen.particles.models.amoebot.specializedparticles.DirectedAmoebotParticle;
import com.cemgokmen.particles.models.amoebot.specializedparticles.ForagingAmoebotParticle;
import com.cemgokmen.particles.models.amoebot.specializedparticles.SeparableAmoebotParticle;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class GridIO {
    public static ParticleGrid importSampleSystem(SampleSystemMetadata system) throws InvalidGridClassException, InvalidParticleClassException, IOException {
        return importParticlesFromResourceName(system.filename, system.gridClass, system.particleClass);
    }

    public static ParticleGrid importParticlesFromResourceName(String resourceName, Class<? extends ParticleGrid> gridClass, Class<? extends Particle> particleClass) throws IOException, InvalidParticleClassException, InvalidGridClassException {
        return importParticlesFromInputStream(GridIO.class.getClassLoader().getResourceAsStream(resourceName), gridClass, particleClass);
    }

    public static ParticleGrid importParticlesFromFilename(String filename, Class<? extends ParticleGrid> gridClass, Class<? extends Particle> particleClass) throws IOException, InvalidParticleClassException, InvalidGridClassException {
        File file = new File(filename);
        return importParticlesFromFile(file, gridClass, particleClass);
    }

    public static ParticleGrid importParticlesFromFile(File file, Class<? extends ParticleGrid> gridClass, Class<? extends Particle> particleClass) throws IOException, InvalidParticleClassException, InvalidGridClassException {
        return importParticlesFromInputStream(new FileInputStream(file), gridClass, particleClass);
    }

    public static ParticleGrid importParticlesFromInputStream(InputStream in, Class<? extends ParticleGrid> gridClass, Class<? extends Particle> particleClass) throws InvalidParticleClassException, InvalidGridClassException {
        if (particleClass == null) {
            throw new GridIO.InvalidParticleClassException();
        }

        if (gridClass == null) {
            throw new GridIO.InvalidGridClassException();
        }

        Scanner input = new Scanner(in);

        ParticleGrid grid;
        if (GridLoaders.GRID_LOADER_MAP.containsKey(gridClass)) {
            grid = GridLoaders.GRID_LOADER_MAP.get(gridClass).apply(input);
        } else {
            throw new InvalidParticleClassException(gridClass);
        }

        input.nextLine();

        int count = input.nextInt();
        input.nextLine();

        int line = 0;

        while (input.hasNextLine()) {
            try {
                if (ParticleLoaders.PARTICLE_TYPE_LOADER_MAP.containsKey(particleClass)) {
                    ParticleLoaders.PARTICLE_TYPE_LOADER_MAP.get(particleClass).accept(grid, input);
                } else {
                    throw new InvalidParticleClassException(particleClass);
                }
            } catch (InputMismatchException e) {
                throw new RuntimeException("Error loading particle number " + line);
            }

            input.nextLine();
        }

        input.close();

        return grid;
    }

    public static final ImmutableList<Class<? extends Particle>> ALLOWED_PARTICLE_TYPES = ImmutableList.copyOf(ParticleLoaders.PARTICLE_TYPE_LOADER_MAP.keySet());
    public static final ImmutableList<Class<? extends ParticleGrid>> ALLOWED_GRID_TYPES = ImmutableList.copyOf(GridLoaders.GRID_LOADER_MAP.keySet());

    public static class InvalidParticleClassException extends Exception {
        public InvalidParticleClassException() {
            super("No class passed");
        }

        public InvalidParticleClassException(Class<?> klass) {
            super(klass.getName());
        }
    }

    public static class InvalidGridClassException extends Exception {
        public InvalidGridClassException() {
            super("No class passed");
        }

        public InvalidGridClassException(Class<?> klass) {
            super(klass.getName());
        }
    }
}
