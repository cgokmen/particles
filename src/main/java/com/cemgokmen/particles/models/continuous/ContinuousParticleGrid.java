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

package com.cemgokmen.particles.models.continuous;

import com.cemgokmen.particles.capabilities.NeighborDetectionCapable;
import com.cemgokmen.particles.graphics.GridGraphics;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.continuous.boundary.ContinuousParticleGridBoundary;
import com.cemgokmen.particles.storage.BiMapParticleStorage;
import com.cemgokmen.particles.storage.ParticleStorage;
import com.cemgokmen.particles.util.Utils;
import com.google.common.collect.ImmutableList;
import org.la4j.Vector;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContinuousParticleGrid extends ParticleGrid {
    public static class ContinuousDirection extends Direction {
        private double ccwAngleFromXAxis;

        public ContinuousDirection(double ccwAngleFromXAxis) {
            super(Vector.fromArray(new double[]{Math.cos(ccwAngleFromXAxis), - Math.sin(ccwAngleFromXAxis)}));

            // Store a simplified version of the angle, so that the angle is in the range [-pi, pi)
            this.ccwAngleFromXAxis = (ccwAngleFromXAxis + Math.PI) % (2 * Math.PI);
            if (this.ccwAngleFromXAxis < 0) this.ccwAngleFromXAxis += (2 * Math.PI);
            this.ccwAngleFromXAxis -= Math.PI;
        }

        @Override
        public String toString() {
            return String.format("Direction: %.2f degrees ccw from X axis", Math.toDegrees(this.ccwAngleFromXAxis));
        }

        public double getCCWAngleFromXAxis() {
            return this.ccwAngleFromXAxis;
        }
    }

    public static class ContinuousCompass extends Compass {
        @Override
        public ImmutableList<Direction> getDirections() {
            throw new UnsupportedOperationException("This grid does not have discretized directions.");
        }

        @Override
        public Direction shiftDirectionCounterclockwise(Direction d, double times) {
            // The times argument here will be interpreted as radians
            double originalAngle = ((ContinuousDirection) d).ccwAngleFromXAxis;
            return new ContinuousDirection(originalAngle + times);
        }

        @Override
        public double getAngleBetweenDirections(Direction a, Direction b) {
            return Utils.getDifferenceBetweenAngles(((ContinuousDirection) a).getCCWAngleFromXAxis(), ((ContinuousDirection) b).getCCWAngleFromXAxis());
        }
    }

    private final ContinuousCompass compass = new ContinuousCompass();

    @Override
    public Compass getCompass() {
        return this.compass;
    } // TODO: Delegate this! Compass should be assigned to particles

    private ContinuousParticleGridBoundary boundary;
    private final ParticleStorage storage;

    public ContinuousParticleGrid(ContinuousParticleGridBoundary boundary) {
        this.boundary = boundary;

        // Upper bound on r=1 particles to fit here
        int upperBound = (int) (boundary.getArea() / Math.PI) + 1;

        this.storage = new BiMapParticleStorage(upperBound);
    }

    @Override
    protected ParticleStorage getStorage() {
        return this.storage;
    }
    @Override
    public boolean isPositionValid(Vector p, Particle forParticle) {
        double radius = 0;

        if (forParticle != null) {
            radius = ((ContinuousParticle) forParticle).getRadius();
        }

        return this.boundary.isVectorInBoundary(p, radius);
    }

    @Override
    public boolean isParticleValid(Particle p) {
        return p instanceof ContinuousParticle;
    }

    @Override
    public Stream<Vector> getValidPositions() {
        // We do this so as not to confuse the graphics.
        return Stream.empty();
    }

    @Override
    public Vector getRandomPosition(Particle particle) {
        // We do some rejection sampling here.
        List<Vector> zoomArea = this.boundary.getZoomAreaVertices();
        double minX = zoomArea.stream().mapToDouble(v -> v.get(0)).min().getAsDouble();
        double maxX = zoomArea.stream().mapToDouble(v -> v.get(0)).max().getAsDouble();

        double minY = zoomArea.stream().mapToDouble(v -> v.get(1)).min().getAsDouble();
        double maxY = zoomArea.stream().mapToDouble(v -> v.get(1)).max().getAsDouble();

        // Now renerate random coordinates in that range and try them
        while (true) {
            double x = Utils.randomDouble() * (maxX - minX) + minX;
            double y = Utils.randomDouble() * (maxY - minY) + minY;

            Vector candidate = Utils.getVector(x, y);
            if (this.isPositionValid(candidate, particle)) return candidate;
        }
    }

    @Override
    public List<Vector> getBoundaryVertices() {
        return this.boundary.getZoomAreaVertices();
    }

    @Override
    public Vector getUnitPixelCoordinates(Vector in) {
        return in.copy();
    }

    @Override
    public Particle getParticleAtPosition(Vector position) {
        // Get any particles that are occupying this exact coordinate
        return this.getAllParticles().filter(that -> {
            Vector thatPosition = this.getParticlePosition(that);
            return Utils.is2DVectorShorterThan(thatPosition.subtract(position), ((ContinuousParticle) that).getRadius());
        }).findAny().orElse(null);
    }

    @Override
    public boolean isPositionOccupied(Vector position) {
        return this.getParticleAtPosition(position) != null;
    }

    @Override
    public Stream<Vector> getAdjacentPositions(Vector p) {
        throw new UnsupportedOperationException("There are infinite adjacent positions on a continuous particle grid.");
    }

    @Override
    public boolean arePositionsAdjacent(Vector p1, Vector p2) {
        throw new UnsupportedOperationException("Not supported on a continuous grid. Try with distance.");
    }

    @Override
    public List<Particle> getParticleNeighbors(Particle p, boolean includeNulls) {
        // TODO: THIS COULD CAUSE INFINITE RECURSION
        return ((NeighborDetectionCapable) p).getNeighborParticles(includeNulls, null);
    }

    @Override
    public List<Particle> getParticleNeighbors(Particle p, @Nonnull Predicate<Particle> filter, boolean includeNulls) {
        // TODO: THIS COULD CAUSE INFINITE RECURSION
        return ((NeighborDetectionCapable) p).getNeighborParticles(includeNulls, filter);
    }

    @Override
    public Particle getParticleNeighborInDirection(Particle p, Direction d) {
        throw new UnsupportedOperationException("Not supported on a continuous grid.");
    }

    @Override
    public Particle getParticleNeighborInDirection(Particle p, Direction d, Predicate<Particle> filter) {
        throw new UnsupportedOperationException("Not supported on a continuous grid.");
    }

    @Override
    public List<Particle> getPositionNeighbors(Vector position, boolean includeNulls) {
        throw new UnsupportedOperationException("Not supported on a continuous grid. Try with distance.");
    }

    @Override
    public List<Particle> getPositionNeighbors(Vector position, @Nonnull Predicate<Particle> filter, boolean includeNulls) {
        throw new UnsupportedOperationException("Not supported on a continuous grid. Try with distance.");
    }

    @Override
    public Particle getPositionNeighborInDirection(Vector p, Direction d) {
        throw new UnsupportedOperationException("Not supported on a continuous grid.");
    }

    @Override
    public Particle getPositionNeighborInDirection(Vector p, Direction d, Predicate<Particle> filter) {
        throw new UnsupportedOperationException("Not supported on a continuous grid.");
    }

    // NOW FOR THE THINGS WE DO SUPPORT

    public boolean arePositionsAdjacent(Vector p1, Vector p2, double distance) {
        return Utils.is2DVectorShorterThan(p1.subtract(p2), distance);
    }

    public List<Particle> getParticleNeighbors(Particle p, double distance) {
        return this.getParticleNeighbors(p, distance, x -> true);
    }

    public List<Particle> getParticleNeighbors(Particle p, double distance, @Nonnull Predicate<Particle> filter) {
        return this.getPositionNeighbors(this.getParticlePosition(p), distance, filter);
    }

    public List<Particle> getPositionNeighbors(Vector position, double distance) {
        return this.getPositionNeighbors(position, distance, x -> true);
    }

    public List<Particle> getPositionNeighbors(Vector position, double distance, @Nonnull Predicate<Particle> filter) {
        // Note that the distance is between the centers
        return this.getAllParticles().filter(that -> {
            Vector thatPosition = this.getParticlePosition(that);
            return Utils.is2DVectorShorterThan(thatPosition.subtract(position), distance);
        }).filter(filter).collect(Collectors.toList());
    }

    @Override
    public void drawBoundary(Graphics2D graphics) {
        Shape shape = this.boundary.getShape();

        graphics.setColor(GridGraphics.BORDER_COLOR);
        graphics.setStroke(GridGraphics.BORDER_STROKE);
        graphics.draw(shape);
    }
}
