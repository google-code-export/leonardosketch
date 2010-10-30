package org.joshy.sketch.controls;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Togglebutton;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Apr 6, 2010
 * Time: 5:15:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToolbarButton extends Togglebutton {
    private Image icon;

    public ToolbarButton(URL iconURL) throws IOException {
        super("");
        this.icon = Image.create(ImageIO.read(iconURL));
        setWidth(this.icon.getWidth());
    }

    @Override
    public void doPrefLayout() {
        super.doPrefLayout();
        this.setWidth(this.icon.getWidth());
        this.setHeight(this.icon.getHeight());
    }

    @Override
    public void doLayout() {
        this.setWidth(this.icon.getWidth());
        this.setHeight(this.icon.getHeight());
    }

    @Override
    public Bounds getLayoutBounds() {
        return new Bounds(getTranslateX(), getTranslateY(), this.icon.getWidth(), this.icon.getHeight());
    }

    @Override
    public void draw(GFX g) {
        if(selected) {
            g.setPaint(FlatColor.WHITE);
        } else {
            g.setPaint(FlatColor.GRAY);
        }
        g.fillRect(0,0,getWidth(),getHeight());
        g.setPaint(FlatColor.BLACK);
        g.drawRect(0,0,getWidth(),getHeight());

        g.drawImage(icon,0,0);

    }
}
