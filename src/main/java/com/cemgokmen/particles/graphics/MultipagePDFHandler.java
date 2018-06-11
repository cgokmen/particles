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

import com.orsonpdf.PDFDocument;
import com.orsonpdf.PDFGraphics2D;
import com.orsonpdf.Page;

import java.awt.*;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Consumer;

public class MultipagePDFHandler implements Closeable {
    private final PDFDocument document;
    private final File file;
    private final int size;

    private boolean closed = false;

    public MultipagePDFHandler(File file, int size) throws FileNotFoundException {
        this.document = new PDFDocument();
        this.document.setTitle("Particles Output");
        this.document.setAuthor("Cem Gokmen");

        this.file = file;
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    public void runOnNewPage(String title, Consumer<Graphics2D> consumer) throws IOException {
        Page page = this.document.createPage(new Rectangle(this.size, this.size));
        PDFGraphics2D graphics = page.getGraphics2D();
        //g2.setRenderingHint(PDFHints.KEY_DRAW_STRING_TYPE, PDFHints.VALUE_DRAW_STRING_TYPE_VECTOR);

        consumer.accept(graphics);
    }

    @Override
    public void close() throws IOException {
        this.document.writeToFile(this.file);
    }
}
