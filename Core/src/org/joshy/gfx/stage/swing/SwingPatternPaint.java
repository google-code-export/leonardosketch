package org.joshy.gfx.stage.swing;

import org.joshy.gfx.draw.PatternPaint;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 27, 2010
 * Time: 11:05:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class SwingPatternPaint extends PatternPaint {
    BufferedImage image;

    public SwingPatternPaint(File file) throws IOException {
        super();
        image = ImageIO.read(file);
    }
}
