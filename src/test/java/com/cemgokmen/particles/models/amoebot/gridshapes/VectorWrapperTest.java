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

import org.junit.Test;
import org.la4j.Vector;

import static org.junit.Assert.*;

public class VectorWrapperTest {

    @Test
    public void noWrapZeroVector() {
        int dist = 5;

        Vector v = Vector.constant(1, 0);
        assertEquals(v, ToroidalAmoebotGrid.wrapVector(v, dist));
    }

    @Test
    public void noWrapPositiveVector() {
        int dist = 5;

        Vector v = Vector.constant(1, 2);
        assertEquals(v, ToroidalAmoebotGrid.wrapVector(v, dist));
    }

    @Test
    public void noWrapNegativeVector() {
        int dist = 5;

        Vector v = Vector.constant(1, -2);
        assertEquals(v, ToroidalAmoebotGrid.wrapVector(v, dist));
    }

    @Test
    public void wrapPositiveVector() {
        int dist = 5;

        Vector v = Vector.constant(1, 6);
        Vector expected = Vector.constant(1, -5);
        assertEquals(expected, ToroidalAmoebotGrid.wrapVector(v, dist));
    }

    @Test
    public void wrapNegativeVector() {
        int dist = 5;

        Vector v = Vector.constant(1, -12);
        Vector expected = Vector.constant(1, -1);
        assertEquals(expected, ToroidalAmoebotGrid.wrapVector(v, dist));
    }

    @Test
    public void wrapMoreNegativeVector() {
        int dist = 5;

        Vector v = Vector.constant(1, -18);
        Vector expected = Vector.constant(1, 4);
        assertEquals(expected, ToroidalAmoebotGrid.wrapVector(v, dist));
    }
}