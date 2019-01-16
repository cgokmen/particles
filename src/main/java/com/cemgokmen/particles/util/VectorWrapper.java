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

package com.cemgokmen.particles.util;

import org.la4j.Vector;

public class VectorWrapper {
    public static Vector wrapVector(Vector v, int wrapAroundDistance) {
        return wrapVector(v, Vector.constant(v.length(), wrapAroundDistance));
    }

    public static Vector wrapVector(Vector v, Vector wrapAroundDistance) {
        if (v.length() != wrapAroundDistance.length()) throw new RuntimeException("Vector dimensions should match.");

        Vector result = v.copy();

        for (int i = 0; i < v.length(); i++) {
            int x = (int) v.get(i);

            // Move it from the range -sHL <-> sHL to 0 <-> 2 * sHL, both inclusive
            x += wrapAroundDistance.get(i);

            // Find equivalence in mod (2 * sHL + 1)
            x = Math.floorMod(x, 2 * (int) wrapAroundDistance.get(i) + 1);

            // Move it back to the original range
            x -= wrapAroundDistance.get(i);

            // Save it
            result.set(i, x);
        }

        return result;
    }

    public static Vector unwrapVector(Vector v, int wrapAroundDistance, Vector levels) {
        return wrapVector(v, Vector.constant(v.length(), wrapAroundDistance));
    }

    public static Vector unwrapVector(Vector v, Vector wrapAroundDistance, Vector levels) {
        if (v.length() != wrapAroundDistance.length() || v.length() != levels.length()) throw new RuntimeException("Vector dimensions should match.");

        Vector result = v.copy();

        for (int i = 0; i < v.length(); i++) {
            int x = (int) v.get(i);
            int level = (int) levels.get(i);

            // Move it from the range -sHL <-> sHL to 0 <-> 2 * sHL, both inclusive
            x += wrapAroundDistance.get(i);

            // Add the level (2 * sHL + 1)
            x += level * (2 * (int) wrapAroundDistance.get(i) + 1);

            // Move it back to the original range
            x -= wrapAroundDistance.get(i);

            // Save it
            result.set(i, x);
        }

        return result;
    }
}
