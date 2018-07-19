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
import java.util.stream.IntStream;

public class QuadrilateralAmoebotGrid extends AmoebotGrid {
    private final int sideHalfLength;
    private final ParticleStorage storage;

    public QuadrilateralAmoebotGrid(int sideHalfLength) {
        if (sideHalfLength <= 0) {
            throw new RuntimeException("Side half-length should be a positive integer.");
        }

        this.sideHalfLength = sideHalfLength;
        this.storage = new BiMapParticleStorage(this.sideHalfLength * this.sideHalfLength); //new TableParticleStorage(this.getBoundaryVertices());
    }

    @Override
    protected ParticleStorage getStorage() {
        return this.storage;
    }

    @Override
    public boolean isPositionValid(Vector p) {
        return p.length() == 2 && Math.abs(p.get(0)) <= this.sideHalfLength && Math.abs(p.get(1)) <= this.sideHalfLength;
    }

    @Override
    public List<Vector> getValidPositions() {
        List<Vector> vectors = new ArrayList<>();

        int max = this.sideHalfLength;

        IntStream.rangeClosed(-max, max).forEach(x -> {
            IntStream.rangeClosed(-max, max).forEach(y -> {
                vectors.add(Utils.getVector(x, y));
            });
        });

        return vectors;
    }

    @Override
    public List<Vector> getBoundaryVertices() {
        int coord = this.sideHalfLength + 1;

        return Lists.newArrayList(
                Utils.getVector(coord, coord),
                Utils.getVector(coord, -coord),
                Utils.getVector(-coord, -coord),
                Utils.getVector(-coord, coord)
        );
    }

    public int getSideHalfLength() {
        return this.sideHalfLength;
    }
}
