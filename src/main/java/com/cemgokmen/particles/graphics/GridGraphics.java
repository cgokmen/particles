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

package com.cemgokmen.particles.graphics;

import com.cemgokmen.particles.algorithms.ParticleAlgorithm;
import com.cemgokmen.particles.util.PropertyUtils;
import com.cemgokmen.particles.util.Utils;
import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.orsonpdf.PDFDocument;
import com.orsonpdf.PDFGraphics2D;
import com.orsonpdf.Page;
import org.apache.commons.io.FilenameUtils;
import org.la4j.Matrix;
import org.la4j.Vector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GridGraphics {
    public static final int EMPTY_POSITION_RADIUS = 3;
    public static final Vector EMPTY_POSITION_TOP_LEFT_VECTOR = Utils.getVector(-EMPTY_POSITION_RADIUS, -EMPTY_POSITION_RADIUS);
    public static final Color EMPTY_POSITION_COLOR = new Color(0.75f, 0.75f, 0.75f);

    public static final int EDGE_LENGTH = 24;

    public static final int EDGE_WIDTH = 4;
    public static final Color EDGE_COLOR = new Color(0.5f, 0.5f, 0.5f);
    public static final BasicStroke EDGE_STROKE = new BasicStroke(EDGE_WIDTH, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);

    public static final Color BORDER_COLOR = new Color(0.1f, 0.1f, 0.1f);
    public static final int BORDER_WIDTH = 4;
    public static final BasicStroke BORDER_STROKE = new BasicStroke(BORDER_WIDTH, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);

    public static final int COM_RADIUS = 10;
    public static final Vector COM_TOP_LEFT_VECTOR = Utils.getVector(-COM_RADIUS, -COM_RADIUS);

    public static final int COM_STROKE_WIDTH = 2;
    public static final BasicStroke COM_STROKE = new BasicStroke(COM_STROKE_WIDTH, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);

    public static final Color COM_FILL_COLOR = Color.ORANGE;
    public static final Color COM_STROKE_COLOR = Color.GREEN;

    public static final int PAGE_SIZE = 300;

    public static Dimension getGridImageDimensions(ParticleGrid grid) {
        List<Vector> extremities = grid.getBoundaryVertices();

        // Store the extremities in a matrix
        Matrix m = Matrix.zero(2, extremities.size());
        for (int i = 0; i < extremities.size(); i++) {
            m.setColumn(i, grid.getUnitPixelCoordinates(extremities.get(i)).multiply(EDGE_LENGTH));
        }

        // Get the x and y coordinates as vectors
        Vector x = m.getRow(0);
        Vector y = m.getRow(1);

        // Get the range of both coordinates
        double xRange = x.max() - x.min() + BORDER_WIDTH;
        double yRange = y.max() - y.min() + BORDER_WIDTH;

        return new Dimension((int) xRange, (int) yRange);
    }

    public static void saveGridImage(ParticleGrid grid, File file) throws Exception {
        final String ext = FilenameUtils.getExtension(file.getName());

        if (ext.equalsIgnoreCase("pdf")) saveGridAsVectorImage(grid, file);
        else if (Arrays.stream(ImageIO.getWriterFileSuffixes()).anyMatch(ext::equalsIgnoreCase)) saveGridAsRasterImage(grid, file, ext);
        else throw new Exception(String.format("Invalid file extension: %s", ext));
    }

    public static void saveGridAsVectorImage(ParticleGrid grid, File file) {
        PDFDocument pdfDoc = new PDFDocument();
        pdfDoc.setTitle("Particles Output");
        pdfDoc.setAuthor("Cem Gokmen");

        Page page = pdfDoc.createPage(new Rectangle(PAGE_SIZE, PAGE_SIZE));
        PDFGraphics2D graphics = page.getGraphics2D();
        //g2.setRenderingHint(PDFHints.KEY_DRAW_STRING_TYPE, PDFHints.VALUE_DRAW_STRING_TYPE_VECTOR);
        drawGridInfoOntoGraphics(grid, graphics, PAGE_SIZE);
        drawGridOntoGraphics(grid, graphics, PAGE_SIZE);

        pdfDoc.writeToFile(file);
    }

    public static MultipagePDFHandler createMultipagePDF(File file) throws FileNotFoundException {
        return new MultipagePDFHandler(file, PAGE_SIZE);
    }

    public static void drawGridOntoMultipagePDF(ParticleGrid grid, MultipagePDFHandler multipagePDFHandler) throws IOException {
        multipagePDFHandler.runOnNewPage(grid.getActivationsRun() + "", graphics -> {
            drawGridInfoOntoGraphics(grid, graphics, PAGE_SIZE);
            drawGridOntoGraphics(grid, graphics, multipagePDFHandler.getSize());
        });
    }

    public static void saveGridAsRasterImage(ParticleGrid grid, File file, String format) throws IOException {
        Dimension dimensions = getGridImageDimensions(grid);
        int pixels = Math.max(dimensions.width, dimensions.height);

        BufferedImage img = new BufferedImage(pixels, pixels, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imgGraphics = (Graphics2D) img.getGraphics();

        drawGridInfoOntoGraphics(grid, imgGraphics, pixels);
        drawGridOntoGraphics(grid, imgGraphics, pixels);

        ImageIO.write(img, format, file);
    }

    public static void drawGridInfoOntoGraphics(ParticleGrid grid, Graphics2D graphics, double size) {
        Map<String, String> info = new LinkedHashMap<>(grid.getGridInformation());
        for (ParticleAlgorithm algorithm: grid.getRunningAlgorithms()) {
            try {
                info.putAll(PropertyUtils.getPropertyValues(algorithm, ParticleAlgorithm.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        graphics.setPaint(Color.BLACK);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, (int) (size / 50.0)));
        final int step = graphics.getFont().getSize();

        final int x = step;
        int curY = step;
        for (Map.Entry e : info.entrySet()) {
            graphics.drawString(String.format("%s: %s", e.getKey(), e.getValue()), x, curY);
            curY += step * 1.5;
        }
    }

    public static void drawGridOntoGraphics(ParticleGrid grid, Graphics2D graphics, double size, Color background) {
        graphics.setColor(background);
        graphics.fill(new Rectangle2D.Double(0, 0, size, size));

        drawGridOntoGraphics(grid, graphics, size);
    }

    public static void drawGridOntoGraphics(ParticleGrid grid, Graphics2D graphics, double size) {
        List<Vector> extremities = grid.getBoundaryVertices();

        // Store the extremities in a matrix
        Matrix m = Matrix.zero(2, extremities.size());
        for (int i = 0; i < extremities.size(); i++) {
            m.setColumn(i, grid.getUnitPixelCoordinates(extremities.get(i)).multiply(EDGE_LENGTH));
        }

        // Get the x and y coordinates as vectors
        Vector x = m.getRow(0);
        Vector y = m.getRow(1);

        // Get the range of both coordinates
        double xRange = x.max() - x.min() + BORDER_WIDTH;
        double yRange = y.max() - y.min() + BORDER_WIDTH;

        // Get the largest range
        double maxRange = Math.max(xRange, yRange);

        // Translate the thing so that the longer edge is contained fully within the context
        AffineTransform firstTransform = new AffineTransform();
        firstTransform.translate(-x.min() + BORDER_WIDTH / 2.0, -y.min() + BORDER_WIDTH / 2.0);

        // Center the shorter edge
        AffineTransform secondTransform = new AffineTransform();
        if (maxRange == xRange) {
            secondTransform.translate(0, (maxRange - yRange / 2.0));
        } else {
            secondTransform.translate((maxRange - xRange) / 2.0, 0);
        }

        // Now scale the thing so that it fits in the graphics context.
        AffineTransform lastTransform = new AffineTransform();
        lastTransform.scale(size / maxRange, size / maxRange);

        firstTransform.preConcatenate(secondTransform);
        firstTransform.preConcatenate(lastTransform);

        graphics.setTransform(firstTransform);
        drawGrid(graphics, grid);
    }

    private static void drawGrid(Graphics2D graphics, ParticleGrid grid) {
        drawBackground(graphics, grid);
        drawBorders(graphics, grid);
        drawEdges(graphics, grid);
        drawParticles(graphics, grid);
        //drawCenterOfMass(graphics, grid);
    }

    private static void drawBackground(Graphics2D graphics, ParticleGrid grid) {
        graphics.setColor(EMPTY_POSITION_COLOR);

        for (Vector v : grid.getValidPositions()) {
            Vector screenPosition = grid.getUnitPixelCoordinates(v).multiply(EDGE_LENGTH);
            Vector topLeft = screenPosition.add(EMPTY_POSITION_TOP_LEFT_VECTOR);

            Ellipse2D.Double circle = new Ellipse2D.Double(topLeft.get(0), topLeft.get(1),
                    2 * EMPTY_POSITION_RADIUS, 2 * EMPTY_POSITION_RADIUS);
            graphics.fill(circle);
        }
    }

    private static void drawBorders(Graphics2D graphics, ParticleGrid grid) {
        List<Vector> extremities = grid.getBoundaryVertices();
        Path2D.Double polygon = new Path2D.Double.Double();

        Vector firstPosition = grid.getUnitPixelCoordinates(extremities.get(0).multiply(EDGE_LENGTH));
        polygon.moveTo(firstPosition.get(0), firstPosition.get(1));

        for (Vector position : extremities) {
            Vector screenPosition = grid.getUnitPixelCoordinates(position).multiply(EDGE_LENGTH);
            polygon.lineTo(screenPosition.get(0), screenPosition.get(1));
        }

        polygon.closePath();

        graphics.setColor(BORDER_COLOR);
        graphics.setStroke(BORDER_STROKE);
        graphics.draw(polygon);
    }

    private static void drawEdges(Graphics2D graphics, ParticleGrid grid) {
        graphics.setColor(EDGE_COLOR);
        graphics.setStroke(EDGE_STROKE);

        Set<Particle> drawnParticles = new HashSet<>();

        for (Particle p : grid.getAllParticles()) {
            Vector position = grid.getUnitPixelCoordinates(grid.getParticlePosition(p)).multiply(EDGE_LENGTH);

            for (Particle nbr : grid.getParticleNeighbors(p, false)) {
                if (!drawnParticles.contains(nbr)) {
                    Vector nbrPosition = grid.getUnitPixelCoordinates(grid.getParticlePosition(nbr)).multiply(EDGE_LENGTH);

                    Line2D.Double line = new Line2D.Double(position.get(0), position.get(1), nbrPosition.get(0), nbrPosition.get(1));
                    graphics.draw(line);
                }
            }

            drawnParticles.add(p);
        }
    }

    private static void drawParticles(Graphics2D graphics, ParticleGrid grid) {
        for (Particle p : grid.getAllParticles()) {
            Vector position = grid.getUnitPixelCoordinates(grid.getParticlePosition(p)).multiply(EDGE_LENGTH);
            p.drawParticle(graphics, position, EDGE_LENGTH);
        }
    }

    private static void drawCenterOfMass(Graphics2D graphics, ParticleGrid grid) {
        Vector com = grid.getCenterOfMass();
        Vector screenPosition = grid.getUnitPixelCoordinates(com).multiply(EDGE_LENGTH);

        graphics.setColor(COM_STROKE_COLOR);
        graphics.setPaint(COM_FILL_COLOR);
        graphics.setStroke(COM_STROKE);

        Vector topLeft = screenPosition.add(COM_TOP_LEFT_VECTOR);

        Ellipse2D.Double circle = new Ellipse2D.Double(topLeft.get(0), topLeft.get(1),
                2 * COM_RADIUS, 2 * COM_RADIUS);
        graphics.fill(circle);
    }
}