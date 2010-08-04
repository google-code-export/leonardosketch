package org.joshy.gfx.node.control.skin;

import org.joshy.gfx.draw.Image;
import org.joshy.gfx.node.Skin;
import org.joshy.gfx.util.URLUtils;
import org.joshy.gfx.util.u;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 28, 2010
 * Time: 9:46:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageSkin extends Skin {
    private BufferedImage buff;
    private Image image;

    public ImageSkin(Element element) throws IOException {
        try {
            URI resolved = URLUtils.calculateURL(element,element.getAttribute("src"));
            buff = ImageIO.read(resolved.toURL());
        } catch (IOException ex) {
            u.p("exception reading file: " + element.getAttribute("src"));
            throw ex;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        image = Image.create(buff);
    }

    public double getHeight() {
        return buff.getHeight();
    }

    public double getWidth() {
        return buff.getWidth();
    }

    public Image getImage() {
        return image;
    }
}
