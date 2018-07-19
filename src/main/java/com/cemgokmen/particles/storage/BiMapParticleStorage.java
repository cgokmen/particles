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
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import org.la4j.Vector;

import java.util.List;
import java.util.stream.Stream;

public class BiMapParticleStorage implements ParticleStorage {
    private final BiMap<Vector, Particle> map;

    public BiMapParticleStorage(int expectedSize) {
        this.map = HashBiMap.create(expectedSize); // TODO: Use extremities for size approximation
    }

    @Override
    public Stream<Particle> getAllParticles() {
        return this.map.values().stream();
    }

    @Override
    public int getParticleCount() {
        return this.map.size();
    }

    @Override
    public Vector getParticlePosition(Particle p) {
        return this.map.inverse().get(p);
    }

    @Override
    public Particle getParticleAtPosition(Vector v) {
        return this.map.get(v);
    }

    @Override
    public boolean containsParticle(Particle p) {
        return this.map.containsValue(p);
    }

    @Override
    public boolean isPositionOccupied(Vector v) {
        return this.map.containsKey(v);
    }

    @Override
    public void addParticle(Particle p, Vector v) {
        this.map.put(v, p);
    }

    @Override
    public void removeParticle(Particle p) {
        this.map.remove(this.getParticlePosition(p));
    }
}
