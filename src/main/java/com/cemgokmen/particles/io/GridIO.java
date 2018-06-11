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

import com.cemgokmen.particles.misc.Utils;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.function.BiConsumer;

public class GridIO {
    public static class ClassFilenameTuple<T> {
        public final Class<? extends T> klass;
        public final String filename;

        public ClassFilenameTuple(Class<? extends T> klass, String filename) {
            this.klass = klass;
            this.filename = filename;
        }
    }

    public enum SampleSystems {
        AMOEBOT_1000_2CLASS("Amoebot/1000 2-class particles"),
        AMOEBOT_100_2CLASS("Amoebot/100 2-class particles"),
        AMOEBOT_100_6DIRECTION("Amoebot/100 6-direction particles"),
        AMOEBOT_100_6DIRECTION_LARGE("Amoebot/100 6-direction particles w/ large grid"),
        AMOEBOT_100_1FOOD("Amoebot/100 particles and 1 food");

        private final String humanReadable;

        SampleSystems(String humanReadable) {
            this.humanReadable = humanReadable;
        }

        @Override
        public String toString() {
            return this.humanReadable;
        }
    }

    public static final ImmutableMap<SampleSystems, ClassFilenameTuple<Particle>> SAMPLE_SYSTEMS = new ImmutableMap.Builder<SampleSystems, ClassFilenameTuple<Particle>>()
            .put(SampleSystems.AMOEBOT_1000_2CLASS, new ClassFilenameTuple<>(SeparableAmoebotParticle.class, "sample_systems/separation/1000particles-2class-spread.txt"))
            .put(SampleSystems.AMOEBOT_100_2CLASS, new ClassFilenameTuple<>(SeparableAmoebotParticle.class, "sample_systems/separation/100particles-2class.txt"))
            .put(SampleSystems.AMOEBOT_100_6DIRECTION, new ClassFilenameTuple<>(DirectedAmoebotParticle.class, "sample_systems/alignment/100particles-randomdir.txt"))
            .put(SampleSystems.AMOEBOT_100_6DIRECTION_LARGE, new ClassFilenameTuple<>(DirectedAmoebotParticle.class, "sample_systems/alignment/100particles-randomdir-largegrid.txt"))
            .put(SampleSystems.AMOEBOT_100_1FOOD, new ClassFilenameTuple<>(ForagingAmoebotParticle.class, "sample_systems/foraging/100particles-1food.txt"))
            .build();

    public static ParticleGrid importParticlesFromResourceName(String resourceName, Class<? extends Particle> particleClass) throws IOException, InvalidParticleClassException {
        return importParticlesFromInputStream(GridIO.class.getClassLoader().getResourceAsStream(resourceName), particleClass);
    }

    public static ParticleGrid importParticlesFromFilename(String filename, Class<? extends Particle> particleClass) throws IOException, InvalidParticleClassException {
        File file = new File(filename);
        return importParticlesFromFile(file, particleClass);
    }

    public static ParticleGrid importParticlesFromFile(File file, Class<? extends Particle> particleClass) throws IOException, InvalidParticleClassException {
        return importParticlesFromInputStream(new FileInputStream(file), particleClass);
    }

    public static ParticleGrid importParticlesFromInputStream(InputStream in, Class<? extends Particle> particleClass) throws InvalidParticleClassException {
        Scanner input = new Scanner(in);

        int radius = input.nextInt();
        input.nextLine();

        ParticleGrid grid = new AmoebotGrid(radius);

        int count = input.nextInt();
        input.nextLine();

        int line = 0;

        while (input.hasNextLine()) {
            try {
                if (PARTICLE_TYPE_LOADER_MAP.containsKey(particleClass)) {
                    PARTICLE_TYPE_LOADER_MAP.get(particleClass).accept(grid, input);
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

    private static void loadAmoebotParticle(ParticleGrid grid, Scanner input) {
        int x = input.nextInt();
        int y = input.nextInt();

        Particle p = new AmoebotParticle();
        grid.addParticle(p, Utils.getVector(x, y));
    }

    private static void loadSeparableAmoebotParticle(ParticleGrid grid, Scanner input) {
        int x = input.nextInt();
        int y = input.nextInt();
        int classNumber = input.nextInt();

        Particle p = new SeparableAmoebotParticle(classNumber, false);
        grid.addParticle(p, Utils.getVector(x, y));
    }

    private static void loadDirectedAmoebotParticle(ParticleGrid grid, Scanner input) {
        int x = input.nextInt();
        int y = input.nextInt();
        int rotation = input.nextInt();

        if (rotation >= grid.getCompass().getDirections().size()) {
            throw new RuntimeException("Invalid rotation.");
        }

        Particle p = new DirectedAmoebotParticle(grid.getCompass(), grid.getCompass().getDirections().get(rotation), false);
        grid.addParticle(p, Utils.getVector(x, y));
    }

    private static void loadForagingAmoebotParticle(ParticleGrid grid, Scanner input) {
        String type = input.next();
        int x = input.nextInt();
        int y = input.nextInt();

        if (!type.equals("f") && !type.equals("p")) {
            throw new RuntimeException("Invalid type. Use f for food and p for particle.");
        }

        Particle p = (type.equals("p")) ? new ForagingAmoebotParticle(false) : new FoodAmoebotParticle();
        grid.addParticle(p, Utils.getVector(x, y));
    }

    private static final ImmutableMap<Class<? extends Particle>, BiConsumer<ParticleGrid, Scanner>> PARTICLE_TYPE_LOADER_MAP =
            new ImmutableMap.Builder<Class<? extends Particle>, BiConsumer<ParticleGrid, Scanner>>()
                    .put(AmoebotParticle.class, GridIO::loadAmoebotParticle)
                    .put(SeparableAmoebotParticle.class, GridIO::loadSeparableAmoebotParticle)
                    .put(DirectedAmoebotParticle.class, GridIO::loadDirectedAmoebotParticle)
                    .put(ForagingAmoebotParticle.class, GridIO::loadForagingAmoebotParticle)
                    .build();

    public static final ImmutableList<Class<? extends Particle>> ALLOWED_PARTICLE_TYPES = ImmutableList.copyOf(PARTICLE_TYPE_LOADER_MAP.keySet());
}
