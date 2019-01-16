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

package com.cemgokmen.particles.capabilities;

import com.cemgokmen.particles.models.ParticleGrid;

public interface WrappedNormalRandomDirectionCapable extends ParticleCapability {
    // Note that the support of this is between -Pi and Pi. Implementations need to keep the scale of the stdev same.
    ParticleGrid.Direction getWrappedNormalRandomDirection(ParticleGrid.Direction mean, double standardDeviation);
}
