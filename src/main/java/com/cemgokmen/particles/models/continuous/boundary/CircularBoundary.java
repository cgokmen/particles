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

package com.cemgokmen.particles.models.continuous.boundary;

import com.cemgokmen.particles.graphics.GridGraphics;
import com.cemgokmen.particles.util.Utils;
import com.google.common.collect.ImmutableList;
import org.la4j.Vector;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.List;

public class CircularBoundary implements ContinuousParticleGridBoundary {
    private double radius;

    public CircularBoundary(double r) {
        this.radius = r;
    }

    @Override
    public boolean isVectorInBoundary(Vector v, double radius) {
        return Utils.is2DVectorShorterThan(v, this.radius - radius);
    }

    @Override
    public double getArea() {
        return Math.PI * this.radius * this.radius;
    }

    @Override
    public List<Vector> getZoomAreaVertices() {
        return ImmutableList.of(
                Utils.getVector(-this.radius, -this.radius),
                Utils.getVector(this.radius, -this.radius),
                Utils.getVector(this.radius, this.radius),
                Utils.getVector(-this.radius, this.radius)
        );
    }

    @Override
    public Shape getShape() {
        // Calculate the position of a pixel at the boundary
        double screenRadius = this.radius * GridGraphics.EDGE_LENGTH;

        return new Ellipse2D.Double(-screenRadius, -screenRadius,2 * screenRadius, 2 * screenRadius);
    }
}
