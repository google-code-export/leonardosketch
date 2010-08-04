package org.joshy.gfx.css;

import org.joshy.gfx.css.values.BaseValue;
import org.joshy.gfx.css.values.LinearGradientValue;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.Scrollbar;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Jul 31, 2010
* Time: 3:30:35 PM
* To change this template use File | Settings | File Templates.
*/
public class CSSSkin extends MasterCSSSkin {

    public BoxState getSize(Control control, String content) {
        BoxState size = super.getSize(control);
        CSSMatcher matcher = createMatcher(control,null);
        Image icon = getIcon(matcher);
        String name = control.getClass().getSimpleName();
        int margin = set.findIntegerValue(name,"margin");
        int padding = set.findIntegerValue(name,"padding");
        int borderWidth = set.findIntegerValue(name, "border-width");
        size.contentWidth = control.getWidth()-margin*2-padding*2;
        size.contentHeight = control.getHeight()-margin*2-padding*2;
        //calc the sizes
        if("true".equals(set.findStringValue(name,"shrink-to-fit"))) {
            //Font font = FontSkin.DEFAULT.getFont();
            Font font = getFont(matcher);
            size.contentWidth = font.calculateWidth(content);
            size.contentHeight = font.calculateHeight(content);
            if(icon != null) {
                size.contentWidth += icon.getWidth();
                size.contentHeight = Math.max(size.contentHeight,icon.getHeight());
            }
            size.width = margin*2+borderWidth*2+padding*2+size.contentWidth;
            size.height = margin*2+borderWidth*2+padding*2+size.contentHeight;
        }
        return size;
    }

    public void draw(GFX g, Control control, String content, BoxState size, State state) {
        if(set == null) {
            g.setPaint(FlatColor.BLUE);
            g.fillRect(0,0,20,20);
            return;
        }
        CSSMatcher matcher = createMatcher(control,state);
        Image icon = getIcon(matcher);
//        u.p("In drawing, icon = " + icon);

        int margin = set.findIntegerValue(matcher.element,"margin");
        int padding = set.findIntegerValue(matcher.element,"padding");
        int borderWidth = set.findIntegerValue(matcher.element, "border-width");
        int borderRadius = set.findIntegerValue(matcher.element,"border-radius");
        BaseValue background = set.findValue(matcher,"background");

        //BaseValue grad = set.findValue(matcher.element, "foo");
        //u.p("got the grad value: " + background);
        Font font = getFont(matcher);

        //draw the background
        double backWidth = size.width-margin*2;
        double backHeight = size.height-margin*2;

        //background-color
//        u.p("bg color as string = " + set.findStringValue(matcher,"background-color"));
        if(!"transparent".equals(set.findStringValue(matcher,"background-color"))) {
            g.setPaint(new FlatColor(set.findColorValue(matcher,"background-color")));
            //u.p("bg color = " + Integer.toHexString(set.findColorValue(matcher,"background-color")));
            if(background instanceof LinearGradientValue) {
                g.setPaint(toGradientFill((LinearGradientValue)background,backWidth,backHeight));
            }
            if(borderRadius == 0) {
                g.fillRect(margin,margin,backWidth,backHeight);
            } else {
                g.fillRoundRect(margin,margin,backWidth,backHeight,borderRadius,borderRadius);
            }
            
        }



        //draw the border
        if(borderWidth > 0) {
            g.setPaint(new FlatColor(set.findColorValue(matcher,"border-color")));
            g.setStrokeWidth(borderWidth);
            if(borderRadius == 0) {
                g.drawRect(margin+borderWidth/2,margin+borderWidth/2,
                        size.width-margin*2-borderWidth,size.height-margin*2-borderWidth);
            } else {
                g.drawRoundRect(margin+borderWidth/2,margin+borderWidth/2,
                        size.width-margin*2-borderWidth,size.height-margin*2-borderWidth,
                        borderRadius,borderRadius);
            }
            g.setStrokeWidth(1);
        }




        //draw the internal content
        double contentX = margin+borderWidth+padding;
        double contentY = margin+borderWidth+padding;
        
        //debugging
//        g.setPaint(FlatColor.GREEN);
//        g.fillRect(contentX,contentY,size.contentWidth,size.contentHeight);
//        g.setPaint(FlatColor.BLACK);

        String textAlign = set.findStringValue(matcher.element,"text-align");
        g.setPaint(new FlatColor(set.findColorValue(matcher,"color")));

        double textX = contentX;
        double textWidth = size.contentWidth;
        if(icon != null) {
            textX += icon.getWidth();
            textWidth -= icon.getWidth();
        }
        if("center".equals(textAlign)) {
            Font.drawCentered(g,content,font,textX,contentY,textWidth,size.contentHeight,true);
        } else {
            Font.drawCenteredVertically(g,content,font,textX,contentY,textWidth,size.contentHeight,true);
        }

        if(icon != null) {
            g.drawImage(icon,contentX,contentY);
        }

        //debugging
//        g.setPaint(FlatColor.RED);
//        g.drawRect(0,0,size.width,size.height);
    }

    private Font getFont(CSSMatcher matcher) {
        int fontSize = set.findIntegerValue(matcher.element, "font-size");
        Font font = Font.name("Arial").size(fontSize).resolve();
        //Font font = FontSkin.DEFAULT.getFont();
        return font;
    }

    public CSSRuleSet getCSSSet() {
        return this.set;
    }


    public enum State {
        Pressed, Hover, Selected, None
    }

    public void draw(GFX g, Scrollbar scrollbar, BoxState size, Bounds thumbBounds, Bounds leftArrowBounds, Bounds rightArrowBounds) {
        if(set == null) {
            g.setPaint(FlatColor.BLUE);
            g.fillRect(0,0,20,20);
            return;
        }
        CSSMatcher matcher = createMatcher(scrollbar,null);
        if(scrollbar.isVertical()) {
            matcher.pseudo = "vertical";
        }
        int margin = set.findIntegerValue(matcher.element,"margin");
//        int padding = set.findIntegerValue(matcher.element,"padding");
//        int borderWidth = set.findIntegerValue(matcher.element, "border-width");
//        int borderRadius = set.findIntegerValue(matcher.element,"border-radius");

        double backWidth = size.width-margin*2;
        double backHeight = size.height-margin*2;
        Bounds backBounds = new Bounds(margin,margin,backWidth,backHeight);
        //draw the background
        drawBackground(g,matcher,"",backBounds);
        drawBorder(    g,matcher,"",backBounds);
        //draw the track
        //draw the arrows

        drawBackground(g,matcher,"left-arrow-",leftArrowBounds);
        drawBackground(g,matcher,"right-arrow-",rightArrowBounds);

        //draw the thumb
        drawBackground(g,matcher,"thumb-",thumbBounds);
        drawBorder(    g,matcher,"thumb-",thumbBounds);
    }

 }
