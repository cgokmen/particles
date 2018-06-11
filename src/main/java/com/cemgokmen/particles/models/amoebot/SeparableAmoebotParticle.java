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

import org.la4j.Vector;

import java.awt.*;

public class SeparableAmoebotParticle extends AmoebotParticle {
    private final int classNumber;
    private final boolean greyscale;

    public SeparableAmoebotParticle(int classNumber, boolean greyscale) {
        this.classNumber = classNumber;
        this.greyscale = greyscale;
    }

    public int getClassNumber() {
        return this.classNumber;
    }

    private static final Color[] BRIGHT_COLORS = new Color[]{
            new Color(230, 25, 75),
            new Color(60, 180, 75),
            new Color(0, 130, 200),
            new Color(255, 225, 25),
            new Color(0, 0, 0),
            new Color(145, 30, 180),
            new Color(70, 240, 240),
            new Color(240, 50, 230),
            new Color(210, 245, 60),
            new Color(250, 190, 190),
            new Color(0, 128, 128),
            new Color(230, 190, 255),
            new Color(170, 110, 40),
            new Color(255, 250, 200),
            new Color(128, 0, 0),
            new Color(170, 255, 195),
            new Color(128, 128, 0),
            new Color(255, 215, 180),
            new Color(0, 0, 128),
            new Color(128, 128, 128),
            new Color(245, 130, 48)
    };

    private static final Color[] GREYSCALE_COLORS = new Color[]{
            new Color(230, 230, 230),
            new Color(0, 0, 0),
            new Color(115, 115, 115)
    };

    @Override
    public Color getCircleFillColor() {
        return (this.greyscale) ? GREYSCALE_COLORS[this.classNumber] : BRIGHT_COLORS[this.classNumber];
    }

    @Override
    public void drawParticle(Graphics2D graphics, Vector screenPosition, int edgeLength) {
        super.drawParticle(graphics, screenPosition, edgeLength);

        graphics.setColor(Color.BLACK);
        int classNumber = this.getClassNumber();
        int currentHomogeneousNeighbors = this.getNeighborParticles(false, (particle) ->
                ((SeparableAmoebotParticle) particle).getClassNumber() == classNumber).size();
        //graphics.drawString(currentHomogeneousNeighbors + "", (int) screenPosition.get(0), (int) screenPosition.get(1));
    }
}
