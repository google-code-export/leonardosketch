package org.joshy.gfx.node.control;

import org.joshy.gfx.SkinManager;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.skin.FontSkin;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 25, 2010
 * Time: 5:50:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class Label extends Control {
    private String text = "Label";
    private FontSkin font;
    private double baseline;
    private FlatColor fill;

    public Label(String text) {
        this.fill = FlatColor.BLACK;
        this.text = text;
    }

    @Override
    public void doSkins() {
        font = (FontSkin) SkinManager.getShared().getSkin(this, null, "main", "jogltext.font", null, FontSkin.DEFAULT);
    }

    @Override
    public void doLayout() {
        double tw = font.getFont().calculateWidth(text);
        double th = font.getFont().calculateHeight(text);
        baseline = font.getFont().getAscender();
        setWidth(tw);
        setHeight(th);
    }

    @Override
    public void draw(GFX g) {
        if(!isVisible()) return;
        if(fill != null && text != null) {
            g.setPaint(fill);
            g.drawText(text, font.getFont(), 0, baseline);
        }
    }

    public void setText(String text) {
        this.text = text;
        setLayoutDirty();
    }

    @Override
    public Bounds getLayoutBounds() {
        return new Bounds(getTranslateX(), getTranslateY(), getWidth(), baseline);
    }

    public Label setFill(FlatColor fill) {
        this.fill = fill;
        return this;
    }
}
