package org.joshy.gfx.node.layout;

import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.util.u;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Aug 28, 2010
* Time: 6:51:48 PM
* To change this template use File | Settings | File Templates.
*/
public class VFlexBox extends FlexBox {
    @Override
    public void doLayout() {
        u.p("======= doing v box " + getWidth() + " x " + getHeight());
        //set children to their preferred width first
        //and calc total width & flex
        double totalHeight = 0;
        double totalFlex = 0;
        for(Control c : controlChildren()) {
            c.doLayout();
            if(c instanceof SplitPane) {
                //reset to 0
                c.setHeight(0);
                //c.setWidth(0);
            }
            totalHeight += c.getHeight();
            totalFlex += spaceMap.get(c);
        }

        double totalExcess = getHeight()-totalHeight;

        double y = 0;
        for(Control c : controlChildren()) {
            //position child first
            c.setTranslateX(0);
            c.setTranslateY(y);
            //set the height
            double flex = spaceMap.get(c);
            if(totalFlex > 0) {
                //u.p("before height = " + c.getHeight());
                c.setHeight(c.getHeight()+flex/totalFlex*totalExcess);
                //u.p("set height to " + c.getHeight());
            }
            //update running total
            y = y + c.getHeight();
            //set the width
            if(align == Align.Stretch) {
                c.setWidth(getWidth());
            }
            c.doLayout();
        }
    }
}
