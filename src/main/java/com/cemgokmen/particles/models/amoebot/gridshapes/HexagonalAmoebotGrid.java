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

package com.cemgokmen.particles.models.amoebot.gridshapes;

import com.cemgokmen.particles.storage.BiMapParticleStorage;
import com.cemgokmen.particles.storage.ParticleStorage;
import com.cemgokmen.particles.storage.TableParticleStorage;
import com.cemgokmen.particles.util.Utils;
import com.cemgokmen.particles.models.amoebot.AmoebotGrid;
import com.google.common.collect.Lists;
import org.la4j.Vector;

import java.util.ArrayList;
import java.util.List;

public class HexagonalAmoebotGrid extends AmoebotGrid {
    private final int radius;
    private final ParticleStorage storage;

    public HexagonalAmoebotGrid(int radius) {
        if (radius < 0) {
            throw new RuntimeException("Radius should be a non-negative integer.");
        }
        this.radius = radius;

        this.storage = new BiMapParticleStorage(3 * this.radius * this.radius); //new TableParticleStorage(this.getBoundaryVertices());
    }

    @Override
    protected ParticleStorage getStorage() {
        return this.storage;
    }

    @Override
    public boolean isPositionValid(Vector p) {
        if (p.length() != 2) {
            return false;
        }

        int dist = (int) (Math.abs(p.get(0)) + Math.abs(p.get(0) + p.get(1)) + Math.abs(p.get(1))) / 2;
        return dist <= this.radius;
    }

    @Override
    public List<Vector> getValidPositions() {
        List<Vector> vectors = new ArrayList<>();

        for (int dx = -this.radius; dx <= this.radius; dx++) {
            for (int dy = Math.max(-this.radius, -dx - this.radius);
                 dy <= Math.min(this.radius, -dx + this.radius); dy++) {
                int dz = -dx - dy;
                vectors.add(Utils.getVector(dx, dz));
            }
        }

        return vectors;
    }

    @Override
    public List<Vector> getBoundaryVertices() {
        int dist = this.radius + 1;

        return Lists.newArrayList(
            Utils.getVector(0, -dist),
            Utils.getVector(dist, -dist),
            Utils.getVector(dist, 0),
            Utils.getVector(0, dist),
            Utils.getVector(-dist, dist),
            Utils.getVector(-dist, 0)
        );
    }
}
