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

package com.cemgokmen.particles.models;

import com.cemgokmen.particles.algorithms.ParticleAlgorithm;
import com.google.common.collect.ImmutableList;
import org.la4j.Vector;

import java.awt.*;
import java.util.function.Function;

public abstract class Particle {
    protected ParticleGrid grid;
    protected ParticleAlgorithm algorithm;

    public void setGrid(ParticleGrid grid) {
        this.grid = grid;
    }

    public ParticleGrid getGrid() { return this.grid; }

    public ParticleAlgorithm getAlgorithm() {
        return this.algorithm;
    }

    public boolean shouldDrawEdges() {
        return true;
    }

    public void setAlgorithm(ParticleAlgorithm algorithm) {
        if (algorithm != null && !algorithm.isParticleAllowed(this)) {
            throw new RuntimeException("This particle is not allowed to run this algorithm.");
        }
        this.algorithm = algorithm;
    }

    public void activate() {
        if (this.algorithm != null) {
            this.algorithm.onParticleActivation(this);
        }
    }

    public abstract void drawParticle(Graphics2D graphics, Vector screenPosition, int edgeLength, Function<Vector, Vector> gridToScreenCoords);
}
