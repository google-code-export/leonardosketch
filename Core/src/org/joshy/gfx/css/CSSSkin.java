package org.joshy.gfx.css;

import org.joshy.gfx.css.values.BaseValue;
import org.joshy.gfx.css.values.LinearGradientValue;
import org.joshy.gfx.css.values.ShadowValue;
import org.joshy.gfx.draw.*;
import org.joshy.gfx.draw.effects.BlurEffect;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Insets;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.Scrollbar;
import org.joshy.gfx.util.GraphicsUtil;

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
        size.margin = getMargin(name);
        size.padding = getPadding(name);
        size.borderWidth = getBorderWidth(name);
        size.contentWidth =  control.getWidth()-size.margin.getLeft()-size.margin.getRight()-size.padding.getLeft()-size.padding.getRight();
        size.contentHeight = control.getHeight()-size.margin.getTop()-size.margin.getBottom()-size.padding.getTop()-size.padding.getBottom();

        //calc the sizes
        if("true".equals(set.findStringValue(name,"shrink-to-fit"))) {
            Font font = getFont(matcher);
            size.contentWidth = font.calculateWidth(content);
            size.contentHeight = font.calculateHeight(content);
            if(icon != null) {
                size.contentWidth += icon.getWidth();
                size.contentHeight = Math.max(size.contentHeight,icon.getHeight());
            }
            size.width = size.margin.getLeft()+size.margin.getRight()+size.borderWidth.getLeft()+size.borderWidth.getRight()+size.padding.getLeft()+size.padding.getRight()+size.contentWidth;
            size.height = size.margin.getTop()+size.margin.getBottom()+size.borderWidth.getTop()+size.borderWidth.getBottom()+size.padding.getTop()+size.padding.getBottom()+size.contentHeight;
            double fh = font.calculateHeight(content);
            size.contentBaseline = (size.contentHeight-fh)/2 + fh;
        } else {
            size.contentBaseline = size.contentHeight;
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

        Insets margin = getMargin(matcher.element);
        Insets padding = getPadding(matcher.element);
        Insets borderWidth = getBorderWidth(matcher.element);
        int borderRadius = set.findIntegerValue(matcher.element,"border-radius");
        BaseValue background = set.findValue(matcher,"background");

        //BaseValue grad = set.findValue(matcher.element, "foo");
        //u.p("got the grad value: " + background);
        Font font = getFont(matcher);

        //draw the background
        double backWidth = size.width-margin.getLeft()-margin.getRight();
        double backHeight = size.height-margin.getTop()-margin.getBottom();

        //background-color
//        u.p("bg color as string = " + set.findStringValue(matcher,"background-color"));
        if(!"transparent".equals(set.findStringValue(matcher,"background-color"))) {
            g.setPaint(new FlatColor(set.findColorValue(matcher,"background-color")));
            //u.p("bg color = " + Integer.toHexString(set.findColorValue(matcher,"background-color")));
            if(background instanceof LinearGradientValue) {
                g.setPaint(toGradientFill((LinearGradientValue)background,backWidth,backHeight));
            }
            if(borderRadius == 0) {
                g.fillRect(margin.getLeft(),margin.getTop(),backWidth,backHeight);
            } else {
                g.fillRoundRect(margin.getLeft(),margin.getTop(),backWidth,backHeight,borderRadius,borderRadius);
            }
            
        }



        //draw the border
        if(!borderWidth.allEquals(0)) {
            g.setPaint(new FlatColor(set.findColorValue(matcher,"border-color")));
            g.setStrokeWidth(borderWidth.getLeft());
            if(borderRadius == 0) {
                g.drawRect(
                        margin.getLeft()+borderWidth.getLeft()/2,
                        margin.getTop()+borderWidth.getTop()/2,
                        size.width-margin.getLeft()-margin.getRight()-borderWidth.getLeft(),
                        size.height-margin.getTop()-margin.getBottom()-borderWidth.getTop());
            } else {
                g.drawRoundRect(
                        margin.getLeft()+borderWidth.getLeft()/2,
                        margin.getRight()+borderWidth.getTop()/2,
                        size.width-margin.getLeft()-margin.getRight()-borderWidth.getLeft(),
                        size.height-margin.getTop()-margin.getBottom()-borderWidth.getTop(),
                        borderRadius,borderRadius);
            }
            g.setStrokeWidth(1);
        }




        //draw the internal content
        double contentX = margin.getLeft()+borderWidth.getLeft()+padding.getLeft();
        double contentY = margin.getTop()+borderWidth.getTop()+padding.getTop();
        
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
        //do drop shadow on text content
        if(content != null && content.length() > 0) {
            BaseValue value = set.findValue(matcher, "text-shadow");
            if(value instanceof ShadowValue) {
                ShadowValue shadow = (ShadowValue) value;
                ImageBuffer buf = g.createBuffer((int)textWidth,(int)size.contentHeight);
                GFX g2 = buf.getGFX();
                g2.setPaint(new FlatColor(shadow.getColor(),0.3));
                g2.translate(-textX,-contentY);
                Font.drawCentered(g2,content,font,textX,contentY,textWidth,size.contentHeight,true);
                buf.apply(new BlurEffect(3,3));
                g.draw(buf,textX+shadow.getXoffset(),contentY+shadow.getYoffset());
            }
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
        if("true".equals(set.findStringValue(matcher,"debug-margin"))) {
            g.setPaint(FlatColor.RED);
            g.drawRect(0,0,size.width,size.height);
        }
        if("true".equals(set.findStringValue(matcher,"debug-border"))) {
            g.setPaint(FlatColor.GREEN);
            g.drawRect(margin.getLeft(),margin.getTop(),size.width-margin.getLeft()-margin.getRight(),size.height-margin.getTop()-margin.getBottom());
        }
        if("true".equals(set.findStringValue(matcher,"debug-padding"))) {
            g.setPaint(FlatColor.BLUE);
            g.drawRect(
                    margin.getLeft()+borderWidth.getLeft(),
                    margin.getTop()+borderWidth.getTop(),
                    size.width-margin.getLeft()-margin.getRight()-borderWidth.getLeft()-borderWidth.getRight(),
                    size.height-margin.getTop()-margin.getBottom()-borderWidth.getTop()-borderWidth.getBottom());
        }
    }

    private Font getFont(CSSMatcher matcher) {
        int fontSize = set.findIntegerValue(matcher.element, "font-size");
        Font font = Font.name("Arial").size(fontSize).resolve();
        return font;
    }

    public CSSRuleSet getCSSSet() {
        return this.set;
    }


    public enum State {
        Pressed, Hover, Selected, None
    }

    public Insets getInsets(Control control) {
        CSSMatcher matcher = createMatcher(control,null);
        return getMargin(matcher.element);
        //int margin = set.findIntegerValue(matcher.element,"margin");
        //return new Insets(margin);
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
        Insets margin = getMargin(matcher.element);

        double backWidth = size.width-margin.getLeft()-margin.getRight();
        double backHeight = size.height-margin.getTop()-margin.getBottom();
        Bounds backBounds = new Bounds(margin.getLeft(),margin.getTop(),backWidth,backHeight);
        //draw the background
        drawBackground(g,matcher,"",backBounds);
        drawBorder(    g,matcher,"",backBounds);
        //draw the track
        //draw the arrows
        drawBackground(g,matcher,"left-arrow-",leftArrowBounds);
        drawBackground(g,matcher,"right-arrow-",rightArrowBounds);
        drawBorder(g,matcher,"left-arrow-",leftArrowBounds);
        drawBorder(g,matcher,"right-arrow-",rightArrowBounds);
        g.setPaint(FlatColor.BLACK);
        if(scrollbar.isVertical()) {
            GraphicsUtil.fillUpArrow(g,3,3,14);
            GraphicsUtil.fillDownArrow(g,3,scrollbar.getHeight()-3-14,14);
        } else {
            GraphicsUtil.fillLeftArrow(g,2,3,14);
            GraphicsUtil.fillRightArrow(g,scrollbar.getWidth()-2-14,3,14);
        }

        //draw the thumb
        drawBackground(g,matcher,"thumb-",thumbBounds);
        drawBorder(    g,matcher,"thumb-",thumbBounds);
    }

 }
