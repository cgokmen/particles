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

import com.cemgokmen.particles.models.amoebot.specializedparticles.*;
import com.cemgokmen.particles.models.continuous.ContinuousParticle;
import com.cemgokmen.particles.models.continuous.ContinuousParticleGrid;
import com.cemgokmen.particles.util.Utils;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.*;
import com.google.common.collect.ImmutableMap;
import org.la4j.Vector;

import java.util.Scanner;
import java.util.function.BiConsumer;

public class ParticleLoaders {
    protected static final ImmutableMap<Class<? extends Particle>, BiConsumer<ParticleGrid, Scanner>> PARTICLE_TYPE_LOADER_MAP =
            new ImmutableMap.Builder<Class<? extends Particle>, BiConsumer<ParticleGrid, Scanner>>()
                    .put(AmoebotParticle.class, ParticleLoaders::loadAmoebotParticle)
                    .put(SeparableAmoebotParticle.class, ParticleLoaders::loadSeparableAmoebotParticle)
                    .put(DirectedAmoebotParticle.class, ParticleLoaders::loadDirectedAmoebotParticle)
                    .put(ForagingAmoebotParticle.class, ParticleLoaders::loadForagingAmoebotParticle)
                    .put(ContinuousDirectedAmoebotParticle.class, ParticleLoaders::loadRandomContinuousDirectedAmoebotParticle)
                    .put(ContinuousParticle.class, ParticleLoaders::loadContinuousParticle)
                    .build();

    static void loadAmoebotParticle(ParticleGrid grid, Scanner input) {
        int x = input.nextInt();
        int y = input.nextInt();

        Particle p = new AmoebotParticle();
        try {
            grid.addParticle(p, Utils.getVector(x, y));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void loadSeparableAmoebotParticle(ParticleGrid grid, Scanner input) {
        int x = input.nextInt();
        int y = input.nextInt();
        int classNumber = input.nextInt();

        Particle p = new SeparableAmoebotParticle(classNumber, false);
        try {
            grid.addParticle(p, Utils.getVector(x, y));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void loadDirectedAmoebotParticle(ParticleGrid grid, Scanner input) {
        int x = input.nextInt();
        int y = input.nextInt();
        int rotation = input.nextInt();

        if (rotation >= grid.getCompass().getDirections().size()) {
            throw new RuntimeException("Invalid rotation.");
        }

        Particle p = new DirectedAmoebotParticle(grid.getCompass(), grid.getCompass().getDirections().get(rotation), false);
        try {
            grid.addParticle(p, Utils.getVector(x, y));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void loadRandomContinuousDirectedAmoebotParticle(ParticleGrid grid, Scanner input) {
        int x = input.nextInt();
        int y = input.nextInt();

        Particle p = new ContinuousDirectedAmoebotParticle(grid.getCompass(), Utils.randomDouble() * Math.PI * 2, false);
        try {
            grid.addParticle(p, Utils.getVector(x, y));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void loadForagingAmoebotParticle(ParticleGrid grid, Scanner input) {
        String type = input.next();
        int x = input.nextInt();
        int y = input.nextInt();

        if (!type.equals("f") && !type.equals("p")) {
            throw new RuntimeException("Invalid type. Use f for food and p for particle.");
        }

        Particle p = (type.equals("p")) ? new ForagingAmoebotParticle(false) : new FoodAmoebotParticle();
        try {
            grid.addParticle(p, Utils.getVector(x, y));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void loadContinuousParticle(ParticleGrid grid, Scanner input) {
        // TODO: This loader parses a legacy format. Fix it.
        double x = input.nextDouble();
        double y = input.nextDouble();

        // Apply the hex coord conversion
        Vector pos = Utils.getVector(x, y);
        Vector cartesian = AmoebotGrid.convertAxialToCartesian(pos);

        double rotation = input.nextDouble() * (Math.PI / 3);

        Particle p = new ContinuousParticle(0.5, rotation);
        try {
            grid.addParticle(p, cartesian);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
