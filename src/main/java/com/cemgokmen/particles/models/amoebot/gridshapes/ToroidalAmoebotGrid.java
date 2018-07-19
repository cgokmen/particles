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
import com.google.common.collect.Maps;
import org.la4j.Vector;

import java.util.List;
import java.util.Map;

public class ToroidalAmoebotGrid extends QuadrilateralAmoebotGrid {
    private Map<Particle, Vector> levels = Maps.newHashMap();
    public ToroidalAmoebotGrid(int sideHalfLength) {
        super(sideHalfLength);
    }

    @Override
    public Vector getPositionInDirection(Vector p, Direction d) {
        Vector newPos = super.getPositionInDirection(p, d);

        return wrapVector(newPos, this.getSideHalfLength());
    }

    public static Vector wrapVector(Vector v, int wrapAroundDistance) {
        Vector result = v.copy();

        for (int i = 0; i < v.length(); i++) {
            int x = (int) v.get(i);

            // Move it from the range -sHL <-> sHL to 0 <-> 2 * sHL, both inclusive
            x += wrapAroundDistance;

            // Find equivalence in mod (2 * sHL + 1)
            x = Math.floorMod(x, 2 * wrapAroundDistance + 1);

            // Move it back to the original range
            x -= wrapAroundDistance;

            // Save it
            result.set(i, x);
        }

        return result;
    }

    public static Vector unwrapVector(Vector v, int wrapAroundDistance, Vector levels) {
        if (v.length() != levels.length()) throw new RuntimeException("Vector levels should match.");

        Vector result = v.copy();

        for (int i = 0; i < v.length(); i++) {
            int x = (int) v.get(i);
            int level = (int) levels.get(i);

            // Move it from the range -sHL <-> sHL to 0 <-> 2 * sHL, both inclusive
            x += wrapAroundDistance;

            // Add the level (2 * sHL + 1)
            x += level * (2 * wrapAroundDistance + 1);

            // Move it back to the original range
            x -= wrapAroundDistance;

            // Save it
            result.set(i, x);
        }

        return result;
    }

    @Override
    public void addParticle(Particle p, Vector position) throws Exception {
        super.addParticle(p, position);

        this.levels.put(p, Vector.zero(2));
    }

    @Override
    public void removeParticle(Particle p) throws Exception {
        super.removeParticle(p);

        this.levels.remove(p);
    }

    @Override
    public void moveParticle(Particle p, Vector v) throws Exception {
        Vector initialPosition = this.getParticlePosition(p);
        Direction d = null;

        // Find the direction that takes us to the new position
        for (Direction d2: this.getCompass().getDirections()) {
            if (this.getPositionInDirection(initialPosition, d2).equals(v)) {
                d = d2;
                break;
            }
        }

        if (d == null) throw new RuntimeException("Jump move!");

        // Find the non-wrapped position
        Vector expectedPosition = initialPosition.add(d.getVector());

        // Execute the move
        super.moveParticle(p, v);

        // Now worry about the levels
        if (!v.equals(initialPosition)) {
            // A move has been made.
            if (!expectedPosition.equals(v)) {
                System.out.println("A particle crossed a border");
                // There was a wraparound. Run level calculation.
                Vector particleLevel = this.levels.get(p);
                for (int i = 0; i < v.length(); i++) {
                    int actual = (int) v.get(i);
                    int expected = (int) expectedPosition.get(i);

                    int level = (int) particleLevel.get(i);

                    if (actual > expected) {
                        System.out.printf("From %s low to high, down one level\n", i == 0 ? "x" : "y");
                        // This is a warp from lower edge to upper edge, so went down a level
                        level--;
                    } else if (actual < expected) {
                        System.out.printf("From %s high to low, up one level\n", i == 0 ? "x" : "y");
                        // This is a warp from an upper edge to a lower edge, so went up a level
                        level++;
                    }

                    particleLevel.set(i, level);
                }
            }
        }
    }

    public Vector getParticleLevel(Particle p) {
        return this.levels.get(p);
    }

    public Vector getLeveledParticlePosition(Particle p) {
        // This is useful for CoM calculation
        Vector position = this.getParticlePosition(p);
        Vector level = this.getParticleLevel(p);

        return unwrapVector(position, this.getSideHalfLength(), level);
    }

    @Override
    public Vector getCenterOfMass() {
        List<Particle> particles = this.getAllParticles();
        return particles.stream()
                .map(this::getLeveledParticlePosition)
                .reduce(Vector.zero(2), Vector::add)
                .divide(particles.size());
    }
}
