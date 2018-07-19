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

package com.cemgokmen.particles.io.html;

import com.google.common.base.Charsets;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HTMLGenerator {
    public static void saveHTML(File toFile, String title, String yAxis, String xAxis, Table<Number, Number, File> images) throws IOException {
        toFile.getParentFile().mkdirs();

        Jinjava jinjava = new Jinjava();
        Map<String, Object> context = Maps.newHashMap();
        context.put("title", title);
        context.put("yAxisLabel", yAxis);
        context.put("xAxisLabel", xAxis);

        List<Number> xAxisEntries = Lists.newArrayList();
        for (Number xAxisEntry : images.columnKeySet()) xAxisEntries.add(xAxisEntry);
        xAxisEntries.sort(new NumberComparator());
        context.put("xAxisEntries", xAxisEntries);

        List<Number> yAxisEntries = Lists.newArrayList();
        for (Number yAxisEntry : images.rowKeySet()) yAxisEntries.add(yAxisEntry);
        yAxisEntries.sort(new NumberComparator());
        context.put("yAxisEntries", yAxisEntries);

        Table<Number, Number, String> imagePaths = HashBasedTable.create();
        for (Table.Cell<Number, Number, File> image : images.cellSet()) {
            Path imagePath = image.getValue().toPath();
            Path htmlPath = toFile.getParentFile().toPath();

            Path relativePath = htmlPath.relativize(imagePath);
            imagePaths.put(image.getRowKey(), image.getColumnKey(), relativePath.toString());
        }
        context.put("images", imagePaths);

        String template = Resources.toString(Resources.getResource("template.html"), Charsets.UTF_8);

        String renderedTemplate = jinjava.render(template, context);
        try (PrintWriter out = new PrintWriter(toFile)) {
            out.println(renderedTemplate);
        }
    }

    public static void main(String[] args) throws IOException {
        String basePath = "/Users/cgokmen/research/results/foragingtests/fedLambda/";
        String htmlName = "index.html";
        File htmlFile = Paths.get(basePath, htmlName).toFile();

        Integer[] xValues = new Integer[]{0, 1000, 10000, 100000, 1000000, 10000000};
        Double[] yValues = new Double[]{3d, 4d, 10d};

        Table<Number, Number, File> images = HashBasedTable.create();

        for (Double y : yValues) {
            for (Integer x : xValues) {
                File imageFile = Paths.get(basePath, y.toString(), x + ".png").toFile();
                images.put(y, x, imageFile);
            }
        }

        saveHTML(htmlFile, "Foraging", "fedLambda", "iterations", images);
    }

    static class NumberComparator<T extends Number & Comparable> implements Comparator<T> {
        public int compare( T a, T b ) throws ClassCastException {
            return a.compareTo( b );
        }
    }
}
