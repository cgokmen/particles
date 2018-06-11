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

public class ForagingAmoebotParticle extends AmoebotParticle {
    private int maximumFedActivations;
    private int fedActivations;
    private final boolean greyscale;
    private int timesFed;
    private int lastFedActivationsAgo;
    private int longestLastFedActivationsAgo;

    private Integer foodToken;

    public ForagingAmoebotParticle(boolean greyscale) {
        this.maximumFedActivations = 1;
        this.fedActivations = 0;
        this.timesFed = 0;
        this.greyscale = greyscale;
        this.foodToken = null;
        this.lastFedActivationsAgo = 0;
        this.longestLastFedActivationsAgo = 0;
    }

    public int getFoodToken() {
        int token = this.foodToken;
        this.foodToken = null;
        return token;
    }

    public boolean hasFoodToken() {
        return this.foodToken != null;
    }

    public void giveFoodToken(int foodToken, int fedActivations) {
        this.foodToken = foodToken;
        this.feed(fedActivations);
    }

    public int getFedActivations() {
        return this.fedActivations;
    }

    public int getTimesFed() {
        return this.timesFed;
    }

    public void feed(int fedActivations) {
        this.maximumFedActivations = fedActivations;
        this.fedActivations = fedActivations;
        this.lastFedActivationsAgo = 0;
        this.timesFed++;
    }

    public void decrementFedActivations() {
        this.fedActivations--;
        if (this.fedActivations < 0) {
            this.fedActivations = 0;
        }
    }

    public boolean isFed() {
        return this.fedActivations > 0;
    }

    public int getLongestLastFedActivationsAgo() {
        return this.longestLastFedActivationsAgo;
    }

    public void incrementLastFedActivationsAgo() {
        this.lastFedActivationsAgo++;
        this.longestLastFedActivationsAgo = Math.max(this.longestLastFedActivationsAgo, this.lastFedActivationsAgo);
    }

    private static final float[][] BRIGHT_COLORS = new float[][]{
            new float[]{0, 1, 1},
            new float[]{0.33f, 1, 1},
    };

    private static final float[][] GREYSCALE_COLORS = new float[][]{
            new float[]{0, 0, 0},
            new float[]{0, 0, 0.4f},
    };

    @Override
    public Color getCircleFillColor() {
        float[] fromColor = (this.greyscale) ? GREYSCALE_COLORS[0] : BRIGHT_COLORS[0];
        float[] toColor = (this.greyscale) ? GREYSCALE_COLORS[1] : BRIGHT_COLORS[1];

        Vector fromColorVector = Vector.fromArray(new double[]{fromColor[0], fromColor[1], fromColor[2]});
        Vector toColorVector = Vector.fromArray(new double[]{toColor[0], toColor[1], toColor[2]});

        Vector finalColorVector = toColorVector.multiply(this.fedActivations).add(fromColorVector.multiply(
                this.maximumFedActivations - this.fedActivations)).divide(this.maximumFedActivations);

        return Color.getHSBColor((float) finalColorVector.get(0), (float) finalColorVector.get(1), (float) finalColorVector.get(2));
    }

    @Override
    public void drawParticle(Graphics2D graphics, Vector screenPosition, int edgeLength) {
        super.drawParticle(graphics, screenPosition, edgeLength);

        graphics.setColor(Color.BLACK);

        //graphics.drawString(this.timesFed + "", (int) screenPosition.get(0), (int) screenPosition.get(1));
        graphics.drawString(this.hasFoodToken() ? "T" : "", (int) screenPosition.get(0), (int) screenPosition.get(1));
    }
}
