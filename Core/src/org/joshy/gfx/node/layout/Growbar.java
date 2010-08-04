package org.joshy.gfx.node.layout;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.util.u;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Feb 1, 2010
 * Time: 7:24:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class Growbar extends Panel {
    private List<Control> growableControls;

    public Growbar() {
        growableControls = new ArrayList<Control>();
    }

    @Override
    public void doLayout() {
        super.doLayout();

        double x = 0;
        double totalWidth = 0;
        double maxHeight = 0;
        double spacerCount = 0;
        for(Control c : controlChildren()) {
            if(c instanceof Spacer) {
                spacerCount++;
            }
            if(growableControls.contains(c)) {
                spacerCount++;
            } else {
                totalWidth += c.getLayoutBounds().getWidth();
            }
            if(c.getLayoutBounds().getHeight() > maxHeight) {
                maxHeight = c.getLayoutBounds().getHeight();
            }
        }

        double excessWidth = getWidth()-totalWidth;

        for(Control c : controlChildren()) {
            c.setTranslateX(x);
            c.setTranslateY((getHeight()-c.getLayoutBounds().getHeight())/2);
            if(c instanceof Spacer) {
                x+= excessWidth/spacerCount;
            }
            if(growableControls.contains(c)) {
                c.setWidth(excessWidth/spacerCount);
            }
            x += c.getLayoutBounds().getWidth();
        }
    }

    public void makeGrowable(Control... controls) {
        for(Control c : controls) {
            growableControls.add(c);
        }
    }

    public static class Spacer extends Control {
        private Control child;

        public Spacer() {
        }

        public Spacer(Control child) {
            this.child = child;
        }

        @Override
        public void draw(GFX g) {
            if(child != null) {
                child.draw(g);
            }
        }

        @Override
        public void doLayout() {
        }

        @Override
        public void doSkins() {
        }
    }

    public static class Strut extends Control {
        public Strut(double width, double height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void draw(GFX g) {

        }

        @Override
        public void doLayout() {
        }

        @Override
        public void doSkins() {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
