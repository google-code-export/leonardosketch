package org.joshy.gfx.node.layout;

import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Control;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Aug 28, 2010
* Time: 6:52:09 PM
* To change this template use File | Settings | File Templates.
*/
public class HFlexBox extends FlexBox {

    public HFlexBox() { }
    
    @Override
    public void doPrefLayout() {
        insets = cssSkin.getInsets(this);

        //do shrink to fit unless a preferred width has been set
        double totalWidth = 0;
        double maxHeight = 0;
        for(Control c : controlChildren()) {
            if(!c.isVisible()) continue;
            c.doPrefLayout();
            Bounds bounds = c.getLayoutBounds();
            totalWidth += bounds.getWidth();
            maxHeight = Math.max(maxHeight,bounds.getHeight());
        }
        if(getPrefWidth() == CALCULATED) {
            setWidth(totalWidth+insets.getLeft()+insets.getRight());
        } else {
            setWidth(getPrefWidth());
        }
        if(getPrefHeight() == CALCULATED) {
            setHeight(maxHeight+insets.getTop()+insets.getBottom());
        } else {
            setHeight(getPrefHeight());
        }
    }

    @Override
    public void doLayout() {
        if(insets == null) doPrefLayout();

        /*
          hbox.doLayout would do:
             call doPrefLayout on all children
             calc metrics based on getLayoutBounds() on all children
             set actual dimensions of all children
             call doLayout() on all children
        */

        //set children to their preferred width first
        //and calc total width & flex
        double totalWidth = 0;
        double totalFlex = 0;
        for(Control c : controlChildren()) {
            if(!c.isVisible()) continue;
            c.doPrefLayout();
            Bounds bounds = c.getLayoutBounds();
            totalWidth += bounds.getWidth();
            totalFlex += spaceMap.get(c);
            //double baseline = c.getBaseline();
        }

        double totalExcess = getWidth()-totalWidth;

        double x = 0;
        for(Control c : controlChildren()) {
            if(!c.isVisible()) continue;            
            Bounds bounds = c.getLayoutBounds();
            //position x
            c.setTranslateX(x+insets.getLeft());
            //set the width
            double flex = spaceMap.get(c);
            if(totalFlex > 0) {
                c.setWidth(c.getWidth()+flex/totalFlex*totalExcess);
            }
            //update running total
            x = x + c.getWidth();

            c.setHeight(bounds.getHeight());

            //position y
            if(align == Align.Top) {
                c.setTranslateY(0+insets.getTop());
            } else if(align == Align.Baseline) {
                double baseline = c.getBaseline();
                c.setTranslateY(getHeight()-baseline+insets.getTop());
            } else if(align == Align.Bottom) {
                c.setTranslateY(getHeight()-bounds.getHeight()+insets.getTop());
            } else if (align == Align.Stretch) {
                c.setTranslateY(0+insets.getTop());
                c.setHeight(getHeight());
            } else {
                c.setTranslateY(0+insets.getTop());
            }


            //layout child
            c.doLayout();
        }
    }

}
