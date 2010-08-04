package org.joshy.gfx.stage.jogl;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.awt.AWTTextureIO;
import org.joshy.gfx.draw.PatternPaint;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 27, 2010
 * Time: 11:05:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class JOGLPatternPaint extends PatternPaint {
    BufferedImage image;
    private boolean initialized;
    Texture texture;

    public JOGLPatternPaint(File file) throws IOException {
        this.image = ImageIO.read(file);
        initialized = false;
    }
    public void initialize() {
        if(!initialized) {
            initialized = true;
            texture = AWTTextureIO.newTexture(image,true);
            texture.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            texture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            texture.setTexParameteri(GL.GL_TEXTURE_WRAP_S,GL.GL_REPEAT);
            texture.setTexParameteri(GL.GL_TEXTURE_WRAP_T,GL.GL_REPEAT);
        }
    }
}
