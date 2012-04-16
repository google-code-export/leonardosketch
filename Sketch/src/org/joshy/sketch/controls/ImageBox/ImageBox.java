package org.joshy.sketch.controls.ImageBox;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.node.control.Control;

import java.io.IOException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Nov 15, 2010
 * Time: 7:15:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageBox extends Control {
    private Image image;


    public ImageBox setImage(URL resource) throws IOException {
        image = Image.getImageFromCache(resource);
        return this;
    }

    @Override
    public void doLayout() {

    }

    @Override
    public void doPrefLayout() {
        setWidth(image.getWidth());
        setHeight(image.getHeight());
    }

    @Override
    public void doSkins() {
    }

    @Override
    public void draw(GFX gfx) {
        gfx.drawImage(image,0,0);
    }
}
