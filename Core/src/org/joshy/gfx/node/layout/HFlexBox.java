package org.joshy.gfx.node.layout;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
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
    public void doLayout() {

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
            c.doPrefLayout();
            Bounds bounds = c.getLayoutBounds();
            totalWidth += bounds.getWidth();
            totalFlex += spaceMap.get(c);
            double baseline = c.getBaseline();
        }

        double totalExcess = getWidth()-totalWidth;
        //u.p("total flex = " + totalFlex);

        double x = 0;
        for(Control c : controlChildren()) {
            Bounds bounds = c.getLayoutBounds();
            //position x
            c.setTranslateX(x);
            //set the width
            double flex = spaceMap.get(c);
            if(totalFlex > 0) {
                c.setWidth(c.getWidth()+flex/totalFlex*totalExcess);
            }
            //update running total
            x = x + c.getWidth();

            //position y
            if(align == Align.Top) {
                c.setTranslateY(0);
            } else if(align == Align.Baseline) {
                double baseline = c.getBaseline();
                c.setTranslateY(getHeight()-baseline);
            } else if(align == Align.Bottom) {
                c.setTranslateY(getHeight()-bounds.getHeight());
            } else {
                c.setTranslateY(0);
            }

            c.setHeight(bounds.getHeight());

            //layout child
            c.doLayout();
        }
    }

    @Override
    public void drawSelf(GFX g) {
        g.setPaint(FlatColor.GREEN);
        g.drawRect(0,0,getWidth(),getHeight());
    }

    @Override
    public void doPrefLayout() {
    }

}
