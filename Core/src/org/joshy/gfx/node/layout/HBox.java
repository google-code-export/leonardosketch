package org.joshy.gfx.node.layout;

import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Insets;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.control.Control;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 23, 2010
 * Time: 8:23:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class HBox extends Panel {
    private Bounds bounds = new Bounds(0,0,500,500);
    private double spacing = 5.0;
    private HAlign halign = HAlign.BASELINE;
    private Insets padding = new Insets(0,0,0,0);
    private double borderWidth;
    private double preferredHeight;
    private boolean preferredHeightIsSet = false;
    private double preferredWidth;
    private boolean preferredWidthIsSet = false;

    public HBox() {
    }

    @Override
    public void doLayout() {
        double x = 0;
        Bounds maxBounds = new Bounds(0,0,0,0);

        Insets insets = padding;
        x+= insets.getLeft();

        //layout all children first
        for(Node child : visibleChildren()) {
            if(child instanceof Control) {
                Control control = (Control) child;
                control.doLayout();
            }
        }

        //calculate the max child height
        double maxHeight = -1;
        for(Node child : visibleChildren()) {
            if(child instanceof Control) {
                maxHeight = Math.max(maxHeight, ((Control)child).getVisualBounds().getHeight());
            } else {
                maxHeight = Math.max(maxHeight, child.getVisualBounds().getHeight());
            }
        }
        maxHeight += (insets.getTop()+insets.getBottom());
        double height = 0;
        if(preferredHeightIsSet) {
            height = preferredHeight;
        } else {
            height = maxHeight;
        }

        for(Node child : visibleChildren()) {
            Bounds bounds = child.getVisualBounds();
            //align bottom then position centered vertically
            if(halign == HAlign.BASELINE){
                if(child instanceof Control) {
                    bounds = ((Control)child).getLayoutBounds();
                }
                child.setTranslateX(x);
                child.setTranslateY(maxHeight-bounds.getHeight() + (height-maxHeight)/2 + insets.getTop());
                x+=bounds.getWidth()+spacing;
            }
            if(halign == HAlign.TOP) {
                child.setTranslateX(x);
                x+=bounds.getWidth()+spacing;
                child.setTranslateY(0+insets.getTop());
            }
            if(halign == HAlign.BOTTOM) {
                child.setTranslateX(x);
                x+=bounds.getWidth()+spacing;
                child.setTranslateY(height-bounds.getHeight());
            }
            if(halign == HAlign.CENTER) {
                child.setTranslateX(x);
                x+=bounds.getWidth()+spacing;
                child.setTranslateY((height-bounds.getHeight())/2);
            }
            //update the max bounds
            maxBounds = maxBounds.union(bounds);
        }
        //subtract off that extra spacing, then add the right insets
        x-=spacing;
        x+=insets.getRight();
        if(preferredWidthIsSet) {
            x = preferredWidth;
        }
        bounds = new Bounds(0,0,x,height);
        setWidth(x);
        setHeight(height);
    }

    private Iterable<Node> visibleChildren() {
        List<Node> nodes = new ArrayList<Node>();
        for(Node n : children) {
            if(n.isVisible()) {
                nodes.add(n);
            }
        }
        return nodes;
    }

    @Override
    public Bounds getVisualBounds() {
        return bounds;
    }

    public HBox setSpacing(double spacing) {
        this.spacing = spacing;
        return this;
    }

    public HBox setHAlign(HAlign align) {
        this.halign = align;
        return this;
    }

    public HBox setPadding(Insets insets) {
        this.padding = insets;
        return this;
    }

    public HBox setBorderWidth(double borderWidth) {
        this.borderWidth = borderWidth;
        return this;
    }

    public HBox setPreferredWidth(double width) {
        this.preferredWidth = width;
        this.preferredWidthIsSet = true;
        return this;
    }

    public HBox setPreferredHeight(double height) {
        this.preferredHeight = height;
        this.preferredHeightIsSet = true;
        return this;
    }
}
