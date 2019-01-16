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
import com.cemgokmen.particles.algorithms.RuleUtils;
import com.cemgokmen.particles.capabilities.ParticleCapability;
import com.cemgokmen.particles.graphics.GridGraphics;
import com.cemgokmen.particles.storage.ParticleStorage;
import com.cemgokmen.particles.util.RandomSelector;
import com.cemgokmen.particles.util.Utils;
import com.google.common.collect.*;
import org.la4j.Vector;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ParticleGrid {
    private int activationsRun = 0;
    private int movesMade = 0;

    public static class DataPoint {
        public Vector position;
        public double weight;
    }

    private List<DataPoint> history = Lists.newArrayList();
    private Particle chosenParticle = null;

    public static class Direction {
        private final Vector vector;

        public Direction(Vector p) {
            this.vector = p;
        }

        public Vector getVector() {
            return this.vector.copy();
        }

        @Override
        public String toString() {
            return this.vector.toString();
        }
    }

    public abstract static class Compass {
        abstract public ImmutableList<Direction> getDirections();

        abstract public Direction shiftDirectionCounterclockwise(Direction d, double times);

        abstract public double getAngleBetweenDirections(Direction a, Direction b);
    }

    abstract protected ParticleStorage getStorage();

    abstract public Compass getCompass();

    abstract public boolean isPositionValid(Vector p, Particle forParticle);

    abstract public boolean isParticleValid(Particle p);

    abstract public Stream<Vector> getValidPositions();

    abstract public Vector getRandomPosition(Particle particle);

    abstract public List<Vector> getBoundaryVertices();

    public boolean isParticleOnGrid(Particle p) {
        return this.getStorage().containsParticle(p);
    }

    public void addParticle(Particle p, Vector position) throws Exception {
        if (!this.isPositionValid(position, p)) {
            throw new Exception("Invalid add - position " + position + " out of bounds.");
        }

        if (this.isPositionOccupied(position)) {
            throw new Exception("Invalid add - there already is a particle at position " + position);
        }
        this.getStorage().addParticle(p, position);
        p.setGrid(this);

        if (this.chosenParticle == null) {
            this.chosenParticle = p;
        }
    }

    public void removeParticle(Particle p) throws Exception {
        if (!this.isParticleOnGrid(p)) {
            throw new Exception("Invalid remove - the provided particle is not on the grid.");
        }
        this.getStorage().removeParticle(p);
        p.setGrid(null);
    }

    public void moveParticle(Particle p, Vector v) throws Exception {
        if (!this.isPositionValid(v, p)) {
            throw new Exception("Invalid move - position " + v + " out of bounds.");
        }

        if (this.isPositionOccupied(v)) {
            throw new Exception("Cannot move to occupied position " + v);
        }

        this.removeParticle(p);
        this.addParticle(p, v);
        this.movesMade++;

        // Get the largest component
        Set<Particle> largestComponent = RuleUtils.getLargestComponent(this);
        Vector centroid = largestComponent.parallelStream().map(this::getParticlePosition)
                .reduce(Vector.zero(2), Vector::add)
                .divide(largestComponent.size());

        // Add to the datapoints now
        // Find the thing
        double componentSize = largestComponent.size();
        double weight = componentSize / this.getParticleCount();

        DataPoint dp = new DataPoint();
        dp.position = centroid;
        dp.weight = weight;

        this.history.add(dp);
    }

    public Particle getParticleAtPosition(Vector position) {
        return this.getStorage().getParticleAtPosition(position);
    }

    public Vector getParticlePosition(Particle p) {
        return this.getStorage().getParticlePosition(p);
    }

    public boolean isPositionOccupied(Vector position) {
        return this.getStorage().isPositionOccupied(position);
    }

    // Position arithmetic
    public Vector getPositionInDirection(Vector p, Direction d) {
        return p.add(d.getVector());
    }

    public Stream<Vector> getAdjacentPositions(Vector p) {
        return this.getCompass().getDirections().stream().map(d -> this.getPositionInDirection(p, d));
    }

    public boolean arePositionsAdjacent(Vector p1, Vector p2) {
        return this.getAdjacentPositions(p1).anyMatch(p -> p == p2);
    }

    public int getParticleCount() {
        return this.getStorage().getParticleCount();
    }

    public int getParticleCount(@Nonnull Predicate<Particle> filter) {
        return (int) this.getAllParticles(filter).count();
    }

    public Stream<Particle> getAllParticles() {
        return this.getStorage().getAllParticles();
    }

    public Stream<Particle> getAllParticles(@Nonnull Predicate<Particle> filter) {
        Stream<Particle> particles = this.getAllParticles();
        return particles.filter(filter);
    }

    public List<Particle> getParticleNeighbors(Particle p, boolean includeNulls) {
        return this.getPositionNeighbors(this.getParticlePosition(p), includeNulls);
    }

    public List<Particle> getParticleNeighbors(Particle p, @Nonnull Predicate<Particle> filter, boolean includeNulls) {
        return this.getPositionNeighbors(this.getParticlePosition(p), filter, includeNulls);
    }

    public Particle getParticleNeighborInDirection(Particle p, Direction d) {
        return this.getPositionNeighborInDirection(this.getParticlePosition(p), d);
    }

    public Particle getParticleNeighborInDirection(Particle p, Direction d, Predicate<Particle> filter) {
        return this.getPositionNeighborInDirection(this.getParticlePosition(p), d, filter);
    }

    public List<Particle> getPositionNeighbors(Vector position, boolean includeNulls) {
        Stream<Particle> stream = this.getAdjacentPositions(position).map(this::getParticleAtPosition);

        if (!includeNulls) stream = stream.filter(Objects::nonNull);

        return stream.collect(Collectors.toList());
    }

    public List<Particle> getPositionNeighbors(Vector position, @Nonnull Predicate<Particle> filter, boolean includeNulls) {
        List<Particle> allNeighbors = this.getPositionNeighbors(position, includeNulls);
        List<Particle> filteredNeighbors = new ArrayList<>(6);

        for (Particle nbr : allNeighbors) {
            if (filter.test(nbr)) {
                filteredNeighbors.add(nbr);
            } else if (includeNulls) {
                filteredNeighbors.add(null);
            }
        }

        return filteredNeighbors;
    }

    public Particle getPositionNeighborInDirection(Vector p, Direction d) {
        return this.getParticleAtPosition(this.getPositionInDirection(p, d));
    }

    public Particle getPositionNeighborInDirection(Vector p, Direction d, Predicate<Particle> filter) {
        Particle neighbor = this.getPositionNeighborInDirection(p, d);
        if (filter.test(neighbor)) {
            return neighbor;
        }
        return null;
    }

    public void assignAllParticlesAlgorithm(ParticleAlgorithm algorithm) {
        this.getAllParticles().forEach(p -> {
            p.setAlgorithm(algorithm);
        });
    }

    public void runActivations(int numActivations) {
        // We make the assumption that no particles will be added.
        List<Particle> particleList = this.getAllParticles().collect(Collectors.toCollection(ArrayList::new));
        RandomSelector<Particle> selector = RandomSelector.uniform(particleList);

        for (int i = 0; i < numActivations; i++) {
            Particle p = null;

            while (p == null) {
                p = selector.next(Utils.random);
                if (!this.isParticleOnGrid(p)) p = null;
            }

            p.activate();
            this.activationsRun++;
        }
    }

    public List<Class<? extends ParticleAlgorithm>> getCompatibleAlgorithms() {
        return ParticleAlgorithm.IMPLEMENTATIONS.stream()
                .filter(impl -> {
                    try {
                        // Every algorithm that allows all particles on our grid is compatible.
                        ParticleAlgorithm instance = Utils.getZeroParameterPublicConstructor(impl).newInstance();
                        return ParticleGrid.this.getAllParticles().allMatch(instance::isParticleAllowed);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    public Stream<ParticleAlgorithm> getRunningAlgorithms() {
        Set<ParticleAlgorithm> runningAlgorithms = new HashSet<>();

        return this.getAllParticles().map(Particle::getAlgorithm).filter(Objects::nonNull).distinct();
    }

    public abstract Vector getUnitPixelCoordinates(Vector in);

    public Vector getCenterOfMass() {
        return this.getAllParticles()
                .map(this::getParticlePosition)
                .reduce(Vector.zero(2), Vector::add)
                .divide(this.getParticleCount());
    }

    public int getActivationsRun() {
        return this.activationsRun;
    }

    public int getMovesMade() {
        return this.movesMade;
    }

    public Map<String, String> getGridInformation() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        map.put("Particle count", this.getParticleCount() + "");
        map.put("Activations run", this.activationsRun + "");
        map.put("Moves made", this.movesMade + "");
        map.put("Center of mass", this.getCenterOfMass().toString());

        this.getRunningAlgorithms().forEach(algorithm -> map.putAll(algorithm.getInformation(this)));

        return map;
    }

    public void drawBoundary(Graphics2D graphics) {
        List<Vector> extremities = this.getBoundaryVertices();
        Path2D.Double polygon = new Path2D.Double.Double();

        Vector firstPosition = this.getUnitPixelCoordinates(extremities.get(0).multiply(GridGraphics.EDGE_LENGTH));
        polygon.moveTo(firstPosition.get(0), firstPosition.get(1));

        for (Vector position : extremities) {
            Vector screenPosition = this.getUnitPixelCoordinates(position).multiply(GridGraphics.EDGE_LENGTH);
            polygon.lineTo(screenPosition.get(0), screenPosition.get(1));
        }

        polygon.closePath();

        graphics.setColor(GridGraphics.BORDER_COLOR);
        graphics.setStroke(GridGraphics.BORDER_STROKE);
        graphics.draw(polygon);
    }

    public List<DataPoint> getAdditionalPlotPoints() {
        // Return points and strengths between 0 and 1 to be plotted.
        return ImmutableList.copyOf(this.history);
    }
}
