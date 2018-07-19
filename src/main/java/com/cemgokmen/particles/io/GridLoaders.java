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
import com.cemgokmen.particles.models.amoebot.gridshapes.HexagonalAmoebotGrid;
import com.cemgokmen.particles.models.amoebot.gridshapes.QuadrilateralAmoebotGrid;
import com.cemgokmen.particles.models.amoebot.gridshapes.ToroidalAmoebotGrid;

import java.util.Scanner;

public class GridLoaders {
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
}
