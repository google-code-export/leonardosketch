package org.joshy.gfx.node.layout;

import org.joshy.gfx.SkinManager;
import org.joshy.gfx.css.CSSSkin;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.control.skin.FillSkin;

public class Panel extends Container {
    protected FlatColor fill = null;
    protected FillSkin skin;
    private FlatColor borderColor = FlatColor.BLACK;
    private CSSSkin cssSkin;
    private Callback<Panel> callback;

    public Panel() {
        setSkinDirty();
    }

    public Panel onDoLayout(Callback<Panel> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void doLayout() {
        if(callback != null) {
            callback.call(this);
        } else {
            super.doLayout();
        }
    }

    @Override
    public void doSkins() {
        skin = SkinManager.getShared().getFillSkin(this, "main", "background", null, FillSkin.SKELETON);
        cssSkin = (CSSSkin) SkinManager.getShared().getSkin(this,PART_CSS,PROP_CSS);
        super.doSkins();
    }

    @Override
    public void draw(GFX g) {
        if(!visible) return;
        g.setOpacity(getOpacity());
        drawSelf(g);
        for(Node child : children) {
            g.translate(child.getTranslateX(),child.getTranslateY());
            child.draw(g);
            g.translate(-child.getTranslateX(),-child.getTranslateY());
        }
        this.drawingDirty = false;
        g.setOpacity(1.0);
    }

    protected void drawSelf(GFX g) {
        if(fill != null) {
            g.setPaint(fill);
            Bounds bounds = getVisualBounds();
            g.fillRect(0,0,getWidth(),getHeight());
            g.setPaint(borderColor);
            g.drawRect(0,0,getWidth(),getHeight());
            return;
        }
        
        if(cssSkin != null) {
            cssSkin.draw(g, this, null, new CSSSkin.BoxState(getWidth(),getHeight()),CSSSkin.State.None);
            return;
        }

        if(skin != null) {
            skin.paint(g,0,0,getWidth(),getHeight());
            return;
        }

    }

    public Panel setFill(FlatColor fill) {
        this.fill = fill;
        return this;
    }

    public Panel add(Node ... nodes) {
        for(Node node : nodes) {
            this.add(node);
        }
        return this;
    }

    public double getOpacity() {
        return this.opacity;
    }
}
