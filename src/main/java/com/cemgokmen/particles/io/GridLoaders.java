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

import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.gridshapes.*;
import com.cemgokmen.particles.models.continuous.ContinuousParticleGrid;
import com.cemgokmen.particles.models.continuous.boundary.CircularBoundary;
import com.google.common.collect.ImmutableMap;

import java.util.Scanner;
import java.util.function.Function;

public class GridLoaders {
    protected static final ImmutableMap<Class<? extends ParticleGrid>, Function<Scanner, ParticleGrid>> GRID_LOADER_MAP =
            new ImmutableMap.Builder<Class<? extends ParticleGrid>, Function<Scanner, ParticleGrid>>()
                    .put(HexagonalAmoebotGrid.class, GridLoaders::loadHexagonalAmoebotGrid)
                    .put(QuadrilateralAmoebotGrid.class, GridLoaders::loadQuadrilateralAmoebotGrid)
                    .put(ToroidalAmoebotGrid.class, GridLoaders::loadToroidalAmoebotGrid)
                    .put(LinearAmoebotGrid.class, GridLoaders::loadLinearAmoebotGrid)
                    .put(CircularAmoebotGrid.class, GridLoaders::loadCircularAmoebotGrid)
                    .put(ContinuousParticleGrid.class, GridLoaders::loadContinuousGrid)
                    .build();


    static ParticleGrid loadHexagonalAmoebotGrid(Scanner input) {
        int radius = input.nextInt();

        return new HexagonalAmoebotGrid(radius);
    }

    static ParticleGrid loadQuadrilateralAmoebotGrid(Scanner input) {
        int sideHalfLength = input.nextInt();

        return new QuadrilateralAmoebotGrid(sideHalfLength);
    }

    static ParticleGrid loadToroidalAmoebotGrid(Scanner input) {
        int sideHalfLength = input.nextInt();

        return new ToroidalAmoebotGrid(sideHalfLength);
    }

    static ParticleGrid loadLinearAmoebotGrid(Scanner input) {
        int halfLength = input.nextInt();

        return new LinearAmoebotGrid(halfLength);
    }

    static ParticleGrid loadCircularAmoebotGrid(Scanner input) {
        int halfLength = input.nextInt();

        return new CircularAmoebotGrid(halfLength);
    }

    static ParticleGrid loadContinuousGrid(Scanner input) {
        double boundaryRadius = input.nextDouble();

        CircularBoundary boundary = new CircularBoundary(boundaryRadius);

        return new ContinuousParticleGrid(boundary);
    }
}
