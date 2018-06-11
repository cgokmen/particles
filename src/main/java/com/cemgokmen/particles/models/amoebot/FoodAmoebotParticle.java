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

package com.cemgokmen.particles.models.amoebot;

import com.cemgokmen.particles.models.ParticleGrid;

import java.awt.*;

public class FoodAmoebotParticle extends AmoebotParticle {
    private Integer activationLifetime;

    public void decrementLifetime(int maxLifetime) {
        if (this.activationLifetime == null) this.activationLifetime = maxLifetime;

        this.activationLifetime--;

        if (this.activationLifetime <= 0) {
            System.out.println("Food expired");
            this.grid.removeParticle(this);
        }
    }

    @Override
    public void move(ParticleGrid.Direction inDirection, boolean swapsAllowed, boolean nonSwapsAllowed) throws InvalidMoveException {
        throw new InvalidMoveException("Cannot move food particle");
    }

    @Override
    public Color getCircleFillColor() {
        return Color.BLACK;
    }
}
