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
import com.cemgokmen.particles.misc.RNG;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import org.la4j.Vector;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;

public abstract class ParticleGrid {
    private final BiMap<Vector, Particle> particles;
    private int activationsRun;

    public ParticleGrid() {
        this.particles = HashBiMap.create();
        this.activationsRun = 0;
    }

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

        abstract public Direction shiftDirectionCounterclockwise(Direction d, int times);

        abstract public double getAngleBetweenDirections(Direction a, Direction b);
    }

    abstract public Compass getCompass();

    abstract public boolean isPositionValid(Vector p);

    abstract public boolean isParticleValid(Particle p);

    abstract public List<Vector> getValidPositions();

    abstract public List<Vector> getExtremities();

    public boolean isParticleOnGrid(Particle p) {
        return this.particles.inverse().containsKey(p);
    }

    public void addParticle(Particle p, Vector position) {
        if (this.isPositionOccupied(position)) {
            throw new RuntimeException("Invalid add - there already is a particle at position " + position);
        }
        this.particles.put(position, p);
        p.setGrid(this);
    }

    public void removeParticle(Particle p) {
        if (!this.particles.inverse().containsKey(p)) {
            throw new RuntimeException("Invalid remove - the provided particle is not on the grid.");
        }
        this.particles.inverse().remove(p);
        p.setGrid(null);
    }

    public void moveParticle(Particle p, Vector v) {
        if (this.isPositionOccupied(v)) {
            throw new RuntimeException("Cannot move to occupied position " + v);
        }

        this.removeParticle(p);
        this.addParticle(p, v);
    }

    public Particle getParticleAtPosition(Vector position) {
        return this.particles.get(position);
    }

    public Vector getParticlePosition(Particle p) {
        return this.particles.inverse().get(p);
    }

    public boolean isPositionOccupied(Vector position) {
        return this.particles.containsKey(position);
    }

    // Position arithmetic
    public Vector getPositionInDirection(Vector p, Direction d) {
        return p.add(d.getVector());
    }

    public Vector[] getAdjacentPositions(Vector p) {
        List<Direction> validDirections = this.getCompass().getDirections();
        Vector[] adjacentPositions = new Vector[validDirections.size()];
        for (int i = 0; i < validDirections.size(); i++) {
            adjacentPositions[i] = p.add(validDirections.get(i).getVector());
        }

        return adjacentPositions;
    }

    public boolean arePositionsAdjacent(Vector p1, Vector p2) {
        Vector diff = p1.subtract(p2);
        for (Direction d : this.getCompass().getDirections()) {
            if (d.getVector().equals(diff) || d.getVector().equals(diff.multiply(-1))) {
                return true;
            }
        }

        return false;
    }

    public List<Particle> getAllParticles() {
        return new ArrayList<>(this.particles.values());
    }

    public List<Particle> getAllParticles(@Nonnull Predicate<Particle> filter) {
        List<Particle> particles = this.getAllParticles();
        particles.removeIf(filter);

        return particles;
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
        List<Particle> neighbors = new ArrayList<>(6);
        for (Vector v : this.getAdjacentPositions(position)) {
            Particle neighbor = this.getParticleAtPosition(v);
            if (includeNulls || neighbor != null) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
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
        for (Particle p : this.getAllParticles()) {
            p.setAlgorithm(algorithm);
        }
    }

    public void runActivations(int numActivations) {
        // We make the assumption that no particles will be added.
        for (int i = 0; i < numActivations; i++) {
            ArrayList<Particle> particleList = new ArrayList<>(this.particles.values());

            Particle p = particleList.get(RNG.randomInt(particleList.size()));
            //System.out.println("Now activating " + p);
            p.activate();
            //System.out.println("Activated " + p);
            this.activationsRun++;
        }
    }

    public abstract List<Class<? extends ParticleAlgorithm>> getAlgorithms();

    public List<ParticleAlgorithm> getRunningAlgorithms() {
        Set<ParticleAlgorithm> runningAlgorithms = new HashSet<>();

        for (Particle p : this.getAllParticles()) {
            ParticleAlgorithm algorithm = p.getAlgorithm();
            if (algorithm != null) {
                runningAlgorithms.add(algorithm);
            }
        }
        return new ArrayList<>(runningAlgorithms);
    }

    public abstract Vector getUnitPixelCoordinates(Vector in);

    public int getActivationsRun() {
        return this.activationsRun;
    }

    public Map<String, String> getGridInformation() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        map.put("Particle count", this.getAllParticles().size() + "");
        map.put("Activations run", this.activationsRun + "");

        this.getRunningAlgorithms().forEach(algorithm -> map.putAll(algorithm.getInformation(this)));

        return map;
    }
}
