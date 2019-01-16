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

import java.lang.reflect.Constructor;
import java.util.Random;

public class Utils {
    public static final Random random = new Random(1337);

    public static Vector getVector(int x, int y) {
        return Vector.fromArray(new double[]{x, y});
    }

    public static Vector getVector(double x, double y) {
        return Vector.fromArray(new double[]{x, y});
    }

    public static <T> Constructor<T> getZeroParameterPublicConstructor(Class<T> clazz) {
        /*for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return (Constructor<T>) constructor;
            }
        }
        return null;*/

        try {
            return clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static double randomDouble() {
        return random.nextDouble();
    }

    public static int randomInt(int bound) {
        return random.nextInt(bound);
    }

    public static double randomWrappedNorm(double sigma) {
        // Start in range [0, 2pi]
        double rnorm = (random.nextGaussian() * sigma) + Math.PI;
        double angle = rnorm % (2 * Math.PI);
        if (angle < 0) angle += 2 * Math.PI;

        // Convert to our correct range and return
        return angle - Math.PI;
    }

    public static double getDifferenceBetweenAngles(double b1, double b2) {
        double r = (b2 - b1) % (2 * Math.PI);
        if (r < -Math.PI)
            r += 2 * Math.PI;
        if (r >= Math.PI)
            r -= 2 * Math.PI;
        return r;
    }

    public static boolean is2DVectorShorterThan(Vector v, double length) {
        double x = v.get(0);
        double y = v.get(1);

        return x * x + y * y < length * length;
    }
}
