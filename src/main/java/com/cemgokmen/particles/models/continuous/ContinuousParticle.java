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

package com.cemgokmen.particles.models.continuous;

import com.cemgokmen.particles.capabilities.*;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.InvalidMoveException;
import com.cemgokmen.particles.util.Utils;
import org.la4j.Vector;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

public class ContinuousParticle extends Particle implements MovementCapable, NeighborDetectionCapable, UniformRandomDirectionCapable, SpinCapable, WrappedNormalRandomDirectionCapable {
    private double radius;
    private ParticleGrid.Direction direction;

    private static final double NBR_GAP_BETWEEN_BOUNDARIES = 0.5; // <=1 radius between two circles is a nbrhood

    public double getRadius() {
        return this.radius;
    }

    @Override
    public ParticleGrid.Direction getDirection() {
        return this.direction;
    }

    @Override
    public void setDirection(ParticleGrid.Direction d) {
        this.direction = d;
    }

    @Override
    public ParticleGrid.Compass getCompass() {
        return this.grid.getCompass();
    }

    public ContinuousParticle(double radius, ContinuousParticleGrid.ContinuousDirection direction) {
        this.radius = radius;
        this.direction = direction;
    }

    public ContinuousParticle(double radius, double degrees) {
        this(radius, new ContinuousParticleGrid.ContinuousDirection(degrees));
    }

    public java.util.List<Particle> getNeighborParticles(boolean includeNulls, Predicate<Particle> filter) {
        ContinuousParticleGrid g = (ContinuousParticleGrid) this.grid;
        if (filter == null) {
            return g.getParticleNeighbors(this, (2 + NBR_GAP_BETWEEN_BOUNDARIES) * this.getRadius());
        }
        return g.getParticleNeighbors(this, (2 + NBR_GAP_BETWEEN_BOUNDARIES) * this.getRadius(), filter);
    }

    public java.util.List<Particle> getAdjacentPositionNeighborParticles(ParticleGrid.Direction d, boolean includeNulls, Predicate<Particle> filter) {
        ContinuousParticleGrid g = (ContinuousParticleGrid) this.grid;
        Vector thisPosition = this.grid.getParticlePosition(this);
        Vector adjacentPosition = this.grid.getPositionInDirection(thisPosition, d);
        if (filter == null) {
            return g.getPositionNeighbors(adjacentPosition, (2 + NBR_GAP_BETWEEN_BOUNDARIES) * this.getRadius());
        }
        return g.getPositionNeighbors(adjacentPosition, (2 + NBR_GAP_BETWEEN_BOUNDARIES) * this.getRadius(), filter);
    }

    @Override
    public boolean isDirectionInBounds(ParticleGrid.Direction d) {
        Vector curPos = this.grid.getParticlePosition(this);
        Vector inDirection = this.grid.getPositionInDirection(curPos, d);
        return this.grid.isPositionValid(inDirection, this);
    }

    public ParticleGrid.Direction getUniformRandomDirection() {
        return new ContinuousParticleGrid.ContinuousDirection(Utils.randomDouble() * Math.PI * 2);
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

    public boolean isDirectionWithinBounds(ParticleGrid.Direction d) {
        return this.grid.isPositionValid(this.grid.getPositionInDirection(this.grid.getParticlePosition(this), d), this);
    }

    public static final int CIRCLE_RADIUS = 12;
    public static final Vector CIRCLE_TOP_LEFT_VECTOR = Utils.getVector(-CIRCLE_RADIUS, -CIRCLE_RADIUS);

    public static final int CIRCLE_STROKE_WIDTH = 1;
    public static final BasicStroke CIRCLE_STROKE = new BasicStroke(CIRCLE_STROKE_WIDTH);

    public static final Color CIRCLE_FILL_COLOR = Color.WHITE;
    public static final Color CIRCLE_STROKE_COLOR = Color.BLACK;

    public static final int ARROW_WIDTH = 3;
    public static final BasicStroke ARROW_STROKE = new BasicStroke(ARROW_WIDTH);

    public static final Color ARROW_COLOR = Color.RED;

    public Color getCircleFillColor() {
        return CIRCLE_FILL_COLOR;
    }

    @Override
    public ParticleGrid.Direction getWrappedNormalRandomDirection(ParticleGrid.Direction mean, double standardDeviation) {
        // Suppose that the mean is a Continous Direction
        ContinuousParticleGrid.ContinuousDirection cmean = (ContinuousParticleGrid.ContinuousDirection) mean;

        double sample = Utils.randomWrappedNorm(standardDeviation);
        return new ContinuousParticleGrid.ContinuousDirection(cmean.getCCWAngleFromXAxis() + sample);
        // TODO: Maybe the compass should do this ^
    }

    @Override
    public void drawParticle(Graphics2D graphics, Vector screenPosition, int edgeLength, Function<Vector, Vector> gridToScreenCoords) {
        graphics.setPaint(this.getCircleFillColor());
        Vector topLeft = screenPosition.add(CIRCLE_TOP_LEFT_VECTOR.multiply(this.getRadius()));

        Ellipse2D.Double circle = new Ellipse2D.Double(topLeft.get(0), topLeft.get(1),
                2 * CIRCLE_RADIUS * this.getRadius(), 2 * CIRCLE_RADIUS * this.getRadius());
        //graphics.fill(circle);

        // Draw the arrow
        Vector towardsArrowTip = this.direction.getVector().multiply(this.getRadius() * CIRCLE_RADIUS * 0.75);
        Vector towardsArrowLeft = Vector.fromArray(new double[]{towardsArrowTip.get(1), -towardsArrowTip.get(0)});
        Vector towardsArrowRight = towardsArrowLeft.multiply(-1);
        Vector towardsArrowBase = towardsArrowTip.multiply(-1);

        Vector arrowLeft = screenPosition.add(towardsArrowLeft);
        Vector arrowTip = screenPosition.add(towardsArrowTip);
        Vector arrowRight = screenPosition.add(towardsArrowRight);
        Vector arrowBase = screenPosition.add(towardsArrowBase);

        // Now make the polygon
        Line2D.Double[] lines = new Line2D.Double[]{
                new Line2D.Double(arrowBase.get(0), arrowBase.get(1), arrowTip.get(0), arrowTip.get(1)),
                new Line2D.Double(arrowLeft.get(0), arrowLeft.get(1), arrowTip.get(0), arrowTip.get(1)),
                new Line2D.Double(arrowRight.get(0), arrowRight.get(1), arrowTip.get(0), arrowTip.get(1)),
        };

        graphics.setColor(ARROW_COLOR);
        graphics.setStroke(ARROW_STROKE);
        graphics.setColor(Color.RED);
        Arrays.stream(lines).forEach(graphics::draw);

        //graphics.setColor(CIRCLE_STROKE_COLOR);
        //graphics.setStroke(CIRCLE_STROKE);
        //graphics.draw(circle);
    }

    @Override
    public boolean shouldDrawEdges() {
        return false;
    }
}
