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

package com.cemgokmen.particles.storage;

import com.cemgokmen.particles.models.Particle;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.la4j.Matrix;
import org.la4j.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class TableParticleStorage implements ParticleStorage {
    /**
     * A Matrix that can be used to transform from coordinate vectors to array indices
     */
    private final Matrix transformationMatrix;

    /**
     * A map from particles to coordinates (and NOT array indices)
     */
    private final HashMap<Particle, Vector> particlePositions;

    /**
     * A map from transformed coordinates to particles
     */
    private final Table<Integer, Integer, Particle> particles;

    public TableParticleStorage(List<Vector> extremities) {
        // Store the extremities in a matrix
        Matrix m = Matrix.zero(2, extremities.size());
        for (int i = 0; i < extremities.size(); i++) {
            m.setColumn(i, extremities.get(i));
        }

        // Get the x and y coordinates as vectors
        Vector x = m.getRow(0);
        Vector y = m.getRow(1);

        // Get the range of both coordinates
        int xMin = (int) x.min();
        int yMin = (int) y.min();
        int xRange = (int) x.max() - xMin;
        int yRange = (int) y.max() - yMin;

        // Calculate the transformationMatrix
        double[][] transform = new double[][]{
                {1, 0, -xMin},
                {0, 1, -yMin},
                {0, 0, 1}
        };

        this.transformationMatrix = Matrix.from2DArray(transform);

        // Create the maps
        this.particles = HashBasedTable.create(yRange, xRange);
        this.particlePositions = Maps.newHashMap();
    }

    @Override
    public Stream<Particle> getAllParticles() {
        return this.particlePositions.keySet().stream();
    }

    @Override
    public int getParticleCount() {
        return this.particlePositions.size();
    }

    @Override
    public Vector getParticlePosition(Particle p) {
        return this.particlePositions.get(p);
    }

    @Override
    public Particle getParticleAtPosition(Vector v) {
        Vector transformed = this.transform(v);
        return this.particles.get(transformed.get(1), transformed.get(0));
    }

    @Override
    public boolean containsParticle(Particle p) {
        return this.particlePositions.containsKey(p);
    }

    @Override
    public boolean isPositionOccupied(Vector v) {
        Vector transformed = this.transform(v);
        return this.particles.contains(transformed.get(1), transformed.get(0));
    }

    @Override
    public void addParticle(Particle p, Vector v) {
        Vector transformed = this.transform(v);

        this.particles.put((int) transformed.get(1), (int) transformed.get(0), p);
        this.particlePositions.put(p, v);
    }

    @Override
    public void removeParticle(Particle p) {
        Vector position = this.particlePositions.get(p);
        Vector transformed = this.transform(position);

        this.particlePositions.remove(p);
        this.particles.remove((int) transformed.get(1), (int) transformed.get(0));
    }

    private Vector transform(Vector in) {
        Vector v = in.copyOfLength(3);
        v.set(2, 1);
        return this.transformationMatrix.multiply(v).copyOfLength(2);
    }
}
