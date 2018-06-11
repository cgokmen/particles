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
import org.la4j.Vector;

import java.awt.*;
import java.awt.geom.Path2D;

public class DirectedAmoebotParticle extends AmoebotParticle {
    private ParticleGrid.Direction direction;
    public final AmoebotGrid.Compass compass;

    public DirectedAmoebotParticle(AmoebotGrid.Compass compass, ParticleGrid.Direction direction, boolean greyscale) {
        this.compass = compass;
        this.direction = direction;
    }

    public ParticleGrid.Direction getDirection() {
        return this.direction;
    }

    public void setDirection(ParticleGrid.Direction direction) {
        this.direction = direction;
    }

    public static final Color ARROW_COLOR = Color.RED;

    @Override
    public void drawParticle(Graphics2D graphics, Vector screenPosition, int edgeLength) {
        super.drawParticle(graphics, screenPosition, edgeLength);

        // Calculate the arrow
        Vector towardsArrowTip = this.grid.getUnitPixelCoordinates(this.direction.getVector().multiply(CIRCLE_RADIUS / 2));
        Vector towardsArrowLeft = this.grid.getUnitPixelCoordinates(this.compass.shiftDirectionCounterclockwise(this.direction, 2).getVector().multiply(
                CIRCLE_RADIUS / 2));
        Vector towardsArrowRight = this.grid.getUnitPixelCoordinates(this.compass.shiftDirectionCounterclockwise(this.direction, 4).getVector().multiply(
                CIRCLE_RADIUS / 2));

        Vector arrowLeft = screenPosition.add(towardsArrowLeft);
        Vector arrowTip = screenPosition.add(towardsArrowTip);
        Vector arrowRight = screenPosition.add(towardsArrowRight);
        Vector arrowBackTip = screenPosition.add(towardsArrowTip.multiply(0.5));

        // Now make the polygon
        Path2D.Double polygon = new Path2D.Double.Double();
        polygon.moveTo(arrowLeft.get(0), arrowLeft.get(1));
        polygon.lineTo(arrowTip.get(0), arrowTip.get(1));
        polygon.lineTo(arrowRight.get(0), arrowRight.get(1));
        polygon.lineTo(arrowBackTip.get(0), arrowBackTip.get(1));
        polygon.closePath();

        graphics.setColor(Color.RED);
        graphics.fill(polygon);

        //graphics.setColor(Color.BLACK);
        //graphics.drawString(this.direction.toString() + "", (int) screenPosition.get(0), (int) screenPosition.get(1));
    }
}
