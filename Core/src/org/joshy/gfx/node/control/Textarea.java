package org.joshy.gfx.node.control;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;

public class Textarea extends TextControl {
    
    public Textarea() {
        this.width = 100;
        this.height = 100;
        this.allowMultiLine = true;
    }

    public Textarea(String text) {
        this();
        this.text = text;
    }

    @Override
    public void draw(GFX g) {
        
        //draw background
        if(focused) {
            g.setPaint(new FlatColor("#e0e0e0"));
        } else {
            g.setPaint(new FlatColor("#f0f0f0"));
        }
        g.fillRect(0,0,width,height);

        //draw border
        if(focused) {
            g.setPaint(FlatColor.BLUE);
        } else {
            g.setPaint(FlatColor.BLACK);
        }
        g.drawRect(0,0,width,height);

        //draw text
        g.setPaint(FlatColor.BLACK);
        String[] lines = text.split("\n");
        g.translate(1,3);
        
        CursorPoint cp = getCurrentCursorPoint();

        Font font = getFont();
        double y = font.getAscender();
        int row = 0;
        for(String line : lines) {
            //draw selection, if needed

            if(selection.isActive()) {
                double start = 0;
                double end = 0;
                //u.p("leading createColumn = " + selection.getLeadingColumn() + " trailing createColumn = " + selection.getTrailingColumn());
                //we are doing the leading row
                if(row == selection.getLeadingRow()) {
                    if(selection.getTrailingRow() != selection.getLeadingRow()) {
                        //if wrapping off the end to the next line
                        start = font.getWidth(line.substring(0,selection.getLeadingColumn()));
                        end = font.getWidth(line);
                    } else {
                        //else we are on the same line
                        start = font.getWidth(line.substring(0,selection.getLeadingColumn()));
                        end = font.getWidth(line.substring(0,selection.getTrailingColumn()));
                    }
                }
                //we are doing the trailing row, and it's not the leading row
                if(row == selection.getTrailingRow() && row != selection.getLeadingRow()) {
                    start = 0;
                    end = font.getWidth(line.substring(0,selection.getTrailingColumn()));
                }
                //if we are doing between the leading and trailing rows
                if(row > selection.getLeadingRow() && row < selection.getTrailingRow()) {
                    start = 0;
                    end = font.getWidth(line);
                }
                g.setPaint(FlatColor.GRAY);
                g.fillRect(start,y-font.getAscender(), end-start,cp.cursorH);
                g.setPaint(FlatColor.BLACK);
            }

            //draw text
            g.drawText(line,font,0,y);
            //g.drawLine(0,y,300,y);

            y+= font.getAscender() + font.getDescender() + font.getLeading();
            row++;
        }

        if(focused) {
            // draw cursor
            if(cp != null) {
                g.fillRect(cp.cursorX , cp.cursorY, cp.cursorW, cp.cursorH);
            }
        }

        g.translate(-1,3);
    }

}
