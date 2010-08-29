package org.joshy.gfx.node.layout;

import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.util.u;

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
        u.p("-- doing hbox: " + getWidth() + " x " + getHeight());
        
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
                u.p("for control: " + c + " using width = " + c.getWidth());
                c.setWidth(c.getWidth()+flex/totalFlex*totalExcess);
            }
            //update running total
            x = x + c.getWidth();

            //position y
            if(align == Align.Baseline) {

            } else {
                c.setTranslateY(0);
            }

            c.setHeight(bounds.getHeight());

            //layout child
            c.doLayout();
        }
    }

    @Override
    public void doPrefLayout() {
        u.p("hbox pref layout");
    }

}
