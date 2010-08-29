package org.joshy.gfx.node.layout;

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
    public HFlexBox() {
    }
    @Override
    public void doLayout() {
        u.p("-- doing hbox: " + getWidth() + " x " + getHeight());
        //set children to their preferred width first
        //and calc total width & flex
        double totalWidth = 0;
        double totalFlex = 0;
        for(Control c : controlChildren()) {
            c.doLayout();
            totalWidth += c.getWidth();
            totalFlex += spaceMap.get(c);
        }

        double totalExcess = getWidth()-totalWidth;
        u.p("total flex = " + totalFlex);

        double x = 0;
        for(Control c : controlChildren()) {
            //position child first
            c.setTranslateX(x);
            c.setTranslateY(0);
            //set the width
            double flex = spaceMap.get(c);
            if(totalFlex > 0) {
                c.setWidth(c.getWidth()+flex/totalFlex*totalExcess);
            }
            //update running total
            x = x + c.getWidth();
            //layout child
            c.doLayout();
        }
    }

}
