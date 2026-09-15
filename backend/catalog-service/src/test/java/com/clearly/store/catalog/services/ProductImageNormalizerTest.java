package com.clearly.store.catalog.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductImageNormalizerTest {
    @TempDir Path temporaryDirectory;

    @Test
    void createsASquareWhiteCanvasWithoutCroppingTheSource() throws Exception {
        BufferedImage source = new BufferedImage(2400, 800, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = source.createGraphics();
        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, source.getWidth(), source.getHeight());
        graphics.dispose();
        Path sourceFile = temporaryDirectory.resolve("wide-product.jpg");
        Path outputFile = temporaryDirectory.resolve("normalized.jpg");
        ImageIO.write(source, "jpeg", sourceFile.toFile());

        ProductImageNormalizer.write(java.nio.file.Files.readAllBytes(sourceFile), outputFile);

        BufferedImage output = ImageIO.read(outputFile.toFile());
        assertEquals(1600, output.getWidth());
        assertEquals(1600, output.getHeight());
        assertEquals(Color.WHITE.getRGB(), output.getRGB(0, 0));
    }
}
