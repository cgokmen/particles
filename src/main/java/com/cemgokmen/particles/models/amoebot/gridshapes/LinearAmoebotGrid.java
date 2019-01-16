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

import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.amoebot.AmoebotGrid;
import com.cemgokmen.particles.storage.BiMapParticleStorage;
import com.cemgokmen.particles.storage.ParticleStorage;
import com.cemgokmen.particles.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.la4j.Vector;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LinearAmoebotGrid extends AmoebotGrid {
    public static class LinearAmoebotCompass extends AmoebotCompass {
        private static final Direction N = new Direction(Utils.getVector(0, -1));
        private static final Direction S = new Direction(Utils.getVector(0, 1));

        private static final Direction[] directions = {N, S};

        @Override
        public ImmutableList<Direction> getDirections() {
            return ImmutableList.copyOf(directions);
        }

        @Override
        public Direction shiftDirectionCounterclockwise(Direction d, double times) {
            if (times % 2 == 0) {
                return d == N ? N : S;
            } else {
                return d == N ? S : N;
            }
        }

        @Override
        public int getMinorArcLength(Direction a, Direction b) {
            return a == b ? 0 : 1;
        }

        @Override
        public double getAngleFromMinorArcLength(int minorArcLength) {
            return minorArcLength == 0 ? 0 : Math.PI / 2;
        }

        @Override
        public double getAngleBetweenDirections(Direction a, Direction b) {
            return this.getAngleFromMinorArcLength(this.getMinorArcLength(a, b));
        }
    }

    private final LinearAmoebotGrid.LinearAmoebotCompass compass = new LinearAmoebotGrid.LinearAmoebotCompass();

    @Override
    public Compass getCompass() {
        return this.compass;
    }

    private final int halfLength;
    private final ParticleStorage storage;

    public LinearAmoebotGrid(int halfLength) {
        if (halfLength <= 0) {
            throw new RuntimeException("Half-length should be a positive integer.");
        }

        this.halfLength = halfLength;
        this.storage = new BiMapParticleStorage(2 * this.halfLength);
    }

    @Override
    protected ParticleStorage getStorage() {
        return this.storage;
    }

    @Override
    public boolean isPositionValid(Vector p, Particle forParticle) {
        return p.length() == 2 && p.get(0) == 0 && Math.abs(p.get(1)) <= this.halfLength;
    }

    @Override
    public Stream<Vector> getValidPositions() {
        final int dx = 0;

        return IntStream.rangeClosed(-this.halfLength, this.halfLength).mapToObj(dy -> {
            int dz = dx - dy;
            return Vector.fromArray(new double[]{dx, dz});
        });
    }

    @Override
    public List<Vector> getBoundaryVertices() {
        int coord = this.halfLength + 1;

        return Lists.newArrayList(
                Utils.getVector(1, coord),
                Utils.getVector(1, -coord),
                Utils.getVector(-1, -coord),
                Utils.getVector(-1, coord)
        );
    }

    public int getHalfLength() {
        return this.halfLength;
    }


}
