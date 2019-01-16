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

import com.cemgokmen.particles.algorithms.SeparationAlgorithm;
import com.cemgokmen.particles.graphics.GridGraphics;
import com.cemgokmen.particles.io.GridIO;
import com.cemgokmen.particles.io.SampleSystemMetadata;
import com.cemgokmen.particles.models.amoebot.AmoebotGrid;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CompressedSeparationTrialRunner {
    private static final Path basePath = Paths.get("/Users/cgokmen/research/results/compressedseparation/");

    public static void main(String[] args) throws Exception {
        // Prepare filesystem
        basePath.toFile().mkdirs();

        AmoebotGrid grid = (AmoebotGrid) GridIO.importSampleSystem(SampleSystemMetadata.AMOEBOT_100_2CLASS);
        GridGraphics.saveGridImage(grid, basePath.resolve(grid.getActivationsRun() + ".png").toFile());

        // Run 10 million iterations of lambda = 10 compression first
        System.out.println("Running compression");
        /*while (true) {
            grid.assignAllParticlesAlgorithm(new CompressionAlgorithm(10.0));
            grid.runActivations(1000000);
            GridGraphics.saveGridImage(grid, basePath.resolve(grid.getActivationsRun() + ".png").toFile());
        }
        System.out.println("Compression run successfully.");*/

        // Run 10 million iterations of alpha = 10 separation
        System.out.println("Running separation");
        while (true) {
            grid.assignAllParticlesAlgorithm(new SeparationAlgorithm(1.0, 10.0, true, false));
            grid.runActivations(1000000);
            GridGraphics.saveGridImage(grid, basePath.resolve(grid.getActivationsRun() + ".png").toFile());
        }
        //System.out.println("Separation run successfully.");
    }
}
