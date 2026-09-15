package com.clearly.store.catalog.services;

import javax.imageio.ImageIO;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/** Stores catalogue photographs in one predictable, display-ready square format. */
public final class ProductImageNormalizer {
    public static final int CANVAS_SIZE = 1600;
    private static final int INNER_SIZE = 1480;

    private ProductImageNormalizer() { }

    public static void write(byte[] source, Path target) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(source));
        if (original == null) throw new IOException("The uploaded image could not be read.");

        BufferedImage normalized = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = normalized.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double scale = Math.min((double) INNER_SIZE / original.getWidth(), (double) INNER_SIZE / original.getHeight());
            int width = Math.max(1, (int) Math.round(original.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(original.getHeight() * scale));
            graphics.drawImage(original, (CANVAS_SIZE - width) / 2, (CANVAS_SIZE - height) / 2, width, height, null);
        } finally {
            graphics.dispose();
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) throw new IOException("JPEG image support is unavailable.");
        ImageWriter writer = writers.next();
        try (OutputStream stream = Files.newOutputStream(target); ImageOutputStream output = ImageIO.createImageOutputStream(stream)) {
            writer.setOutput(output);
            ImageWriteParam parameters = writer.getDefaultWriteParam();
            if (parameters.canWriteCompressed()) {
                parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                parameters.setCompressionQuality(0.9f);
            }
            writer.write(null, new IIOImage(normalized, null, null), parameters);
        } finally {
            writer.dispose();
        }
    }
}
