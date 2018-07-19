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

package com.cemgokmen.particles.io;

import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.AmoebotGrid;
import com.cemgokmen.particles.models.amoebot.AmoebotParticle;
import com.cemgokmen.particles.models.amoebot.gridshapes.HexagonalAmoebotGrid;
import com.cemgokmen.particles.models.amoebot.gridshapes.QuadrilateralAmoebotGrid;
import com.cemgokmen.particles.models.amoebot.gridshapes.ToroidalAmoebotGrid;
import com.cemgokmen.particles.models.amoebot.specializedparticles.DirectedAmoebotParticle;
import com.cemgokmen.particles.models.amoebot.specializedparticles.ForagingAmoebotParticle;
import com.cemgokmen.particles.models.amoebot.specializedparticles.SeparableAmoebotParticle;

public enum SampleSystemMetadata {
    AMOEBOT_1000_2CLASS("Amoebot/1000 2-class particles", HexagonalAmoebotGrid.class, SeparableAmoebotParticle.class, "sample_systems/separation/1000particles-2class-spread.txt"),
    AMOEBOT_100_2CLASS("Amoebot/100 2-class particles", HexagonalAmoebotGrid.class, SeparableAmoebotParticle.class, "sample_systems/separation/100particles-2class.txt"),
    AMOEBOT_100_6DIRECTION("Amoebot/100 6-direction particles", ToroidalAmoebotGrid.class, DirectedAmoebotParticle.class, "sample_systems/alignment/100particles-randomdir.txt"),
    AMOEBOT_100_6DIRECTION_LARGE("Amoebot/100 6-direction particles w/ large grid", ToroidalAmoebotGrid.class, DirectedAmoebotParticle.class, "sample_systems/alignment/100particles-randomdir-largegrid.txt"),
    AMOEBOT_100_1FOOD("Amoebot/100 particles and 1 food", HexagonalAmoebotGrid.class, ForagingAmoebotParticle.class, "sample_systems/foraging/100particles-1food.txt"),
    AMOEBOT_3PARTICLES("Amoebot/3 particles", HexagonalAmoebotGrid.class, AmoebotParticle.class, "sample_systems/compression/3particles.txt");

    public final String humanReadableName;
    public final Class<? extends ParticleGrid> gridClass;
    public final Class<? extends Particle> particleClass;
    public final String filename;

    SampleSystemMetadata(String humanReadableName, Class<? extends ParticleGrid> gridClass, Class<? extends Particle> particleClass, String filename) {
        this.humanReadableName = humanReadableName;
        this.gridClass = gridClass;
        this.particleClass = particleClass;
        this.filename = filename;
    }

    @Override
    public String toString() {
        return this.humanReadableName;
    }
}
