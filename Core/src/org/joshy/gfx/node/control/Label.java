package org.joshy.gfx.node.control;

import org.joshy.gfx.SkinManager;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Bounds;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 25, 2010
 * Time: 5:50:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class Label extends Control {
    private String text = "Label";
    private Font font;
    private double baseline;
    private FlatColor fill;

    public Label(String text) {
        this.fill = FlatColor.BLACK;
        this.text = text;
    }

    @Override
    public void doSkins() {
        cssSkin = SkinManager.getShared().getCSSSkin();
        font = cssSkin.getDefaultFont();
    }

    @Override
    public void doLayout() {
        /*
        double tw = font.calculateWidth(text);
        double th = font.calculateHeight(text);
        baseline = font.getAscender();
        setWidth(tw);
        setHeight(th);*/
    }

    @Override
    public void doPrefLayout() {
        double tw = font.calculateWidth(text);
        double th = font.calculateHeight(text);
        baseline = font.getAscender();
        setWidth(tw);
        setHeight(th);
    }

    @Override
    public void draw(GFX g) {
        if(!isVisible()) return;
        if(fill != null && text != null) {
            g.setPaint(fill);
            g.drawText(text, font, 0, baseline);
        }
    }

    public void setText(String text) {
        this.text = text;
        setLayoutDirty();
    }

    @Override
    public Bounds getLayoutBounds() {
        return new Bounds(getTranslateX(), getTranslateY(), getWidth(), getHeight());
    }

    @Override
    public double getBaseline() {
        return baseline;
    }

    public Label setFill(FlatColor fill) {
        this.fill = fill;
        return this;
    }
}
