package org.joshy.gfx.node.control;

import org.joshy.gfx.SkinManager;
import org.joshy.gfx.css.CSSMatcher;
import org.joshy.gfx.css.CSSSkin;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Insets;

public class Textbox extends TextControl {
    private Insets insets;
    private double baseline;
    double xoff = 0;
    private CSSSkin.BoxState cssSize;

    public Textbox() {
        setWidth(100);
        setHeight(40);
        insets = new Insets(8,4,8,4);
    }

    public Textbox(String text) {
        this();
        setText(text);
    }

    @Override
    protected double filterMouseX(double x) {
        return x - xoff - insets.getLeft();
    }

    @Override
    public void doSkins() {
        super.doSkins();
        cssSkin = SkinManager.getShared().getCSSSkin();
        setLayoutDirty();
    }

    /* =========== Layout stuff ================= */
    @Override
    public void doPrefLayout() {
        if(cssSkin != null) {
            cssSize = cssSkin.getSize(this,text);
            if(prefWidth != CALCULATED) {
                setWidth(prefWidth);
                cssSize.width = prefWidth;
            } else {
                setWidth(cssSize.width);
            }
            if(prefHeight != CALCULATED) {
                setHeight(prefHeight);
                cssSize.height = prefHeight;
            } else {
                setHeight(cssSize.height);
            }

            insets = cssSize.padding;
        }
        double th = this.getFont().getAscender();
        baseline = insets.getTop() + this.getFont().getAscender();
        double height = insets.getTop() + th + insets.getBottom();
        setHeight(height);
    }

    @Override
    public void doLayout() {
        if(cssSize == null) doPrefLayout();
        cssSize.width = getWidth();
    }

    @Override
    public double getBaseline() {
        return cssSize.margin.getTop() + cssSize.borderWidth.getTop() + cssSize.padding.getTop() + cssSize.contentBaseline;
    }

    @Override
    public Bounds getLayoutBounds() {
        return new Bounds(getTranslateX(), getTranslateY(), getWidth(), baseline);
    }

    /* =============== Drawing stuff ================ */
    @Override
    public void draw(GFX g) {

        if(cssSkin != null) {
            if(cssSize == null) {
                this.doPrefLayout();
            }
            CSSMatcher matcher = new CSSMatcher(this);
            cssSkin.drawBackground(g, matcher,"", new Bounds(0,0,getWidth(),getHeight()));
            cssSkin.drawBorder(g,matcher,"",new Bounds(0,0,getWidth(),getHeight()));
        } else {
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
        }

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
            g.fillRect(insets.getLeft()+start+xoff,cp.cursorY+2+insets.getTop(), end-start,cp.cursorH);
            g.setPaint(FlatColor.BLACK);
        }

        //draw the text
        g.setPaint(FlatColor.BLACK);
        g.drawText(text,font,insets.getLeft()+xoff,baseline);

        //draw the cursor
        g.setPaint(FlatColor.BLUE);
        if(focused) {
            CursorPoint cp = getCurrentCursorPoint();
            // draw cursor
            g.fillRect(
                    insets.getLeft()+cp.cursorX + 1 + xoff,
                    insets.getTop()+cp.cursorY+2,
                    cp.cursorW, cp.cursorH);
        }

        //restore the old clip
        g.setClipRect(oldClip);
    }

    protected String filterText(String text) {
        return text;
    }

}
