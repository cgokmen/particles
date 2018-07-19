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

package com.cemgokmen.particles.algorithms;

import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.AmoebotParticle;
import com.cemgokmen.particles.models.amoebot.gridshapes.QuadrilateralAmoebotGrid;
import com.cemgokmen.particles.util.Utils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RuleUtilsTest {
    private ParticleGrid grid;

    @Before
    public void setUp() throws Exception {
        grid = new QuadrilateralAmoebotGrid(5);

        grid.addParticle(new AmoebotParticle(), Utils.getVector(0, 0));
        grid.addParticle(new AmoebotParticle(), Utils.getVector(0, 1));
        grid.addParticle(new AmoebotParticle(), Utils.getVector(0, 2));
    }

    @Test
    public void checkParticleConnection() {
        assertTrue(RuleUtils.checkParticleConnection(grid, particle -> true));
    }
}