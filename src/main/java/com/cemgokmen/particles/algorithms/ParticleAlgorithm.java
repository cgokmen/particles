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

import com.cemgokmen.particles.capabilities.ParticleCapability;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.AmoebotGrid;
import com.cemgokmen.particles.models.amoebot.specializedparticles.DirectedAmoebotParticle;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ParticleAlgorithm {
    public static final List<Class<? extends ParticleAlgorithm>> IMPLEMENTATIONS = ImmutableList.of(
            CompressionAlgorithm.class,
            SeparationAlgorithm.class,
            ForagingAlgorithm.class,
            AlignmentAlgorithm.class
            //ContinuousAlignmentAlgorithm.class,
            //BobBotAlignmentAlgorithm.class
            );

    public abstract void onParticleActivation(Particle p);

    public boolean isParticleAllowed(Particle p) {
        return this.getRequiredCapabilities().stream().allMatch(req -> req.isInstance(p));
    }

    public abstract List<Class<? extends ParticleCapability>> getRequiredCapabilities();

    public abstract boolean isGridValid(ParticleGrid grid);

    public Map<String, String> getInformation(ParticleGrid g) {
        return new HashMap<>();
    }
}
