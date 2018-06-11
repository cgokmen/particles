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

package com.cemgokmen.particles.models.amoebot;

import com.cemgokmen.particles.algorithms.*;
import com.cemgokmen.particles.misc.Utils;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.google.common.collect.ImmutableList;
import org.la4j.Matrix;
import org.la4j.Vector;
import org.la4j.matrix.dense.Basic2DMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AmoebotGrid extends ParticleGrid {
    public static class AmoebotCompass extends Compass {
        private static final Direction SE = new Direction(Utils.getVector(1, 0));
        private static final Direction NE = new Direction(Utils.getVector(1, -1));
        private static final Direction N = new Direction(Utils.getVector(0, -1));
        private static final Direction NW = new Direction(Utils.getVector(-1, 0));
        private static final Direction SW = new Direction(Utils.getVector(-1, 1));
        private static final Direction S = new Direction(Utils.getVector(0, 1));

        private static final Direction[] directions = {SE, NE, N, NW, SW, S};
        private final HashMap<Direction, Integer> directionOrder;

        public AmoebotCompass() {
            this.directionOrder = new HashMap<>();
            for (int i = 0; i < directions.length; i++) {
                this.directionOrder.put(directions[i], i);
            }
        }

        @Override
        public ImmutableList<Direction> getDirections() {
            return ImmutableList.copyOf(directions);
        }

        @Override
        public Direction shiftDirectionCounterclockwise(Direction d, int times) {
            int index = this.directionOrder.get(d);
            return directions[Math.floorMod(index + times, directions.length)];
        }

        public int getMinorArcLength(Direction a, Direction b) {
            int diff = Math.abs(this.directionOrder.get(a) - this.directionOrder.get(b));
            if (diff > directions.length / 2) {
                diff = directions.length - diff;
            }

            return diff;
        }

        public double getAngleFromMinorArcLength(int minorArcLength) {
            return minorArcLength * Math.PI / 3;
        }

        @Override
        public double getAngleBetweenDirections(Direction a, Direction b) {
            return this.getAngleFromMinorArcLength(this.getMinorArcLength(a, b));
        }
    }

    private final AmoebotCompass compass = new AmoebotCompass();
    private int radius;

    public AmoebotGrid(int radius) {
        if (radius < 0) {
            throw new RuntimeException("Radius should be a non-negative integer.");
        }
        this.radius = radius;
    }

    @Override
    public Compass getCompass() {
        return this.compass;
    } // TODO: Delegate this! Compass should be assigned to particles

    @Override
    public boolean isPositionValid(Vector p) {
        if (p.length() != 2) {
            return false;
        }

        int dist = (int) (Math.abs(p.get(0)) + Math.abs(p.get(0) + p.get(1)) + Math.abs(p.get(1))) / 2;
        return dist <= this.radius;
    }

    @Override
    public boolean isParticleValid(Particle p) {
        return p instanceof AmoebotParticle;
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
    public List<Vector> getExtremities() {
        int dist = this.radius + 1;
        List<Vector> extremities = new ArrayList<>(6);
        extremities.add(Utils.getVector(0, -dist));
        extremities.add(Utils.getVector(dist, -dist));
        extremities.add(Utils.getVector(dist, 0));
        extremities.add(Utils.getVector(0, dist));
        extremities.add(Utils.getVector(-dist, dist));
        extremities.add(Utils.getVector(-dist, 0));

        return extremities;
    }

    @Override
    public List<Class<? extends ParticleAlgorithm>> getAlgorithms() {
        List<Class<? extends ParticleAlgorithm>> algorithms = new ArrayList<>();
        algorithms.add(CompressionAlgorithm.class);
        algorithms.add(SeparationAlgorithm.class);
        algorithms.add(AlignmentAlgorithm.class);
        algorithms.add(ForagingAlgorithm.class);

        return algorithms;
    }

    private static final Matrix axialToPixel = new Basic2DMatrix(new double[][]{
            {3.0 / 2.0, 0.0},
            {Math.sqrt(3) / 2.0, Math.sqrt(3)}
    });

    @Override
    public Vector getUnitPixelCoordinates(Vector in) {
        return axialToPixel.multiply(in);
    }
}