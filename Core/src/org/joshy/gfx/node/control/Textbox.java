package org.joshy.gfx.node.control;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.skin.InsetsSkin;

public class Textbox extends TextControl {
    private InsetsSkin insets;
    private double baseline;
    double xoff = 0;

    public Textbox() {
        setWidth(100);
        setHeight(20);
        insets = new InsetsSkin(5,3,5,3);
    }

    public Textbox(String text) {
        this();
        setText(text);
    }

    @Override
    protected double filterMouseX(double x) {
        return x - xoff;
    }

    /* =========== Layout stuff ================= */
    @Override
    public void doLayout() {
        double th = this.getFont().getAscender();
        baseline = insets.getTop() + this.getFont().getAscender();
        double height = insets.getTop() + th + insets.getBottom();
        setHeight(height);
    }

    @Override
    public Bounds getLayoutBounds() {
        return new Bounds(getTranslateX(), getTranslateY(), getWidth(), baseline);
    }

    /* =============== Drawing stuff ================ */
    @Override
    public void draw(GFX g) {
        
        //draw background
        if(focused) {
            g.setPaint(new FlatColor("#e0e0e0"));
        } else {
            g.setPaint(new FlatColor("#f0f0f0"));
        }
        g.fillRect(0,0,width,height);


        // draw border
        if(focused) {
            g.setPaint(FlatColor.BLUE);
        } else {
            g.setPaint(FlatColor.BLACK);
        }
        g.drawRect(0,0,width,height);

        //set a new clip
        Bounds oldClip = g.getClipRect();
        g.setClipRect(new Bounds(0,0,width,height));

        //adjust x to scroll if needed
        CursorPoint cursor = getCurrentCursorPoint();
        if(cursor.cursorX < -xoff) {
            xoff = 0-cursor.cursorX + 10;
        }
        if(cursor.cursorX + xoff > width-10) {
            xoff = width-cursor.cursorX - 10;
        }
        if(cursor.cursorX == 0) {
            xoff = 0;
        }

        //filter the text
        String text = filterText(getText());

        Font font = this.getFont();

        //draw the selection
        if(selection.isActive() && text.length() >= 1) {
            CursorPoint cp = getCurrentCursorPoint();
            double start = font.getWidth(text.substring(0,selection.getLeadingColumn()));
            double end = font.getWidth(text.substring(0,selection.getTrailingColumn()));
            g.setPaint(FlatColor.GRAY);
            g.fillRect(start+xoff,cp.cursorY+2, end-start,cp.cursorH);
            g.setPaint(FlatColor.BLACK);
        }

        //draw the text
        g.setPaint(FlatColor.BLACK);
        g.drawText(text,font,2+xoff,baseline);

        //draw the cursor
        g.setPaint(FlatColor.BLUE);
        if(focused) {
            CursorPoint cp = getCurrentCursorPoint();
            // draw cursor
            g.fillRect(cp.cursorX + 1 + xoff, cp.cursorY+2, cp.cursorW, cp.cursorH);
        }

        //restore the old clip
        g.setClipRect(oldClip);
    }

    protected String filterText(String text) {
        return text;
    }

}
