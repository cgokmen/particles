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

import com.cemgokmen.particles.capabilities.MovementCapable;
import com.cemgokmen.particles.capabilities.NeighborDetectionCapable;
import com.cemgokmen.particles.capabilities.UniformRandomDirectionCapable;
import com.cemgokmen.particles.capabilities.SwapMovementCapable;
import com.cemgokmen.particles.util.Utils;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import org.la4j.Vector;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class AmoebotParticle extends Particle implements UniformRandomDirectionCapable, MovementCapable, SwapMovementCapable, NeighborDetectionCapable {
    public List<Particle> getNeighborParticles(boolean includeNulls, Predicate<Particle> filter) {
        if (filter == null) {
            return this.grid.getParticleNeighbors(this, includeNulls);
        }
        return this.grid.getParticleNeighbors(this, filter, includeNulls);
    }

    public List<Particle> getAdjacentPositionNeighborParticles(ParticleGrid.Direction d, boolean includeNulls, Predicate<Particle> filter) {
        Vector thisPosition = this.grid.getParticlePosition(this);
        Vector adjacentPosition = this.grid.getPositionInDirection(thisPosition, d);
        if (filter == null) {
            return this.grid.getPositionNeighbors(adjacentPosition, includeNulls);
        }
        return this.grid.getPositionNeighbors(adjacentPosition, filter, includeNulls);
    }

    @Override
    public boolean isDirectionInBounds(ParticleGrid.Direction d) {
        Vector curPos = this.grid.getParticlePosition(this);
        Vector inDirection = this.grid.getPositionInDirection(curPos, d);
        return this.grid.isPositionValid(inDirection, this);
    }

    public Particle getNeighborInDirection(ParticleGrid.Direction d, int counterclockwiseShift, Predicate<Particle> filter) {
        if (counterclockwiseShift != 0) {
            d = this.grid.getCompass().shiftDirectionCounterclockwise(d, counterclockwiseShift);
        }

        Particle neighbor = this.grid.getParticleNeighborInDirection(this, d);
        return (filter == null || filter.test(neighbor)) ? neighbor : null;
    }

    public Particle getAdjacentPositionNeighborInDirection(ParticleGrid.Direction d, int counterclockwiseShift, Predicate<Particle> filter) {
        Vector thisPosition = this.grid.getParticlePosition(this);
        Vector adjacentPosition = this.grid.getPositionInDirection(thisPosition, d);

        if (counterclockwiseShift != 0) {
            d = this.grid.getCompass().shiftDirectionCounterclockwise(d, counterclockwiseShift);
        }

        Particle neighbor = this.grid.getPositionNeighborInDirection(adjacentPosition, d);
        return (filter == null || filter.test(neighbor)) ? neighbor : null;
    }

    public ParticleGrid.Direction getUniformRandomDirection() {
        List<ParticleGrid.Direction> directions = this.grid.getCompass().getDirections();
        return directions.get(Utils.randomInt(directions.size()));
    }

    public void move(ParticleGrid.Direction inDirection) {
        Vector current = this.grid.getParticlePosition(this);
        Vector target = this.grid.getPositionInDirection(current, inDirection);

        //System.out.printf("Moving particle of color %d from %s to %s\n", ((SeparableAmoebotParticle)this).getClassNumber(), current, target);

        Particle atTarget = this.grid.getParticleAtPosition(target);
        try {
            if (atTarget == null) {
                // Make the move
                this.grid.moveParticle(this, target);
            } else {
                throw new InvalidMoveException("Position is occupied.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void swapMove(ParticleGrid.Direction inDirection) {
        Vector current = this.grid.getParticlePosition(this);
        Vector target = this.grid.getPositionInDirection(current, inDirection);

        //System.out.printf("Moving particle of color %d from %s to %s\n", ((SeparableAmoebotParticle)this).getClassNumber(), current, target);

        Particle atTarget = this.grid.getParticleAtPosition(target);
        try {
            if (atTarget == null) {
                // Make the move
                this.grid.moveParticle(this, target);
            } else {
                if (!this.getClass().equals(atTarget.getClass())) {
                    throw new InvalidMoveException("swap between different types");
                }

                // Remove the occupying particle
                this.grid.removeParticle(atTarget);

                // Move there
                this.grid.moveParticle(this, target);

                // Add occupying particle
                this.grid.addParticle(atTarget, current);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean isDirectionWithinBounds(ParticleGrid.Direction d) {
        return this.grid.isPositionValid(this.grid.getPositionInDirection(this.grid.getParticlePosition(this), d), this);
    }

    public static final int CIRCLE_RADIUS = 12;
    public static final Vector CIRCLE_TOP_LEFT_VECTOR = Utils.getVector(-CIRCLE_RADIUS, -CIRCLE_RADIUS);

    public static final int CIRCLE_STROKE_WIDTH = 2;
    public static final BasicStroke CIRCLE_STROKE = new BasicStroke(CIRCLE_STROKE_WIDTH, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);

    public static final Color CIRCLE_FILL_COLOR = new Color(0f, 1.0f, 0f);
    public static final Color CIRCLE_STROKE_COLOR = new Color(0f, 1.0f, 0f);

    public Color getCircleFillColor() {
        return CIRCLE_FILL_COLOR;
    }

    @Override
    public void drawParticle(Graphics2D graphics, Vector screenPosition, int edgeLength, Function<Vector, Vector> gridToScreenCoords) {
        graphics.setColor(CIRCLE_STROKE_COLOR);
        graphics.setPaint(this.getCircleFillColor());
        graphics.setStroke(CIRCLE_STROKE);

        Vector topLeft = screenPosition.add(CIRCLE_TOP_LEFT_VECTOR);

        Ellipse2D.Double circle = new Ellipse2D.Double(topLeft.get(0), topLeft.get(1),
                2 * CIRCLE_RADIUS, 2 * CIRCLE_RADIUS);
        graphics.fill(circle);
    }
}
