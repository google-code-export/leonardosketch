package org.joshy.gfx.css;

import org.joshy.gfx.css.values.BaseValue;
import org.joshy.gfx.css.values.LinearGradientValue;
import org.joshy.gfx.css.values.ShadowValue;
import org.joshy.gfx.draw.*;
import org.joshy.gfx.draw.effects.BlurEffect;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Insets;
import org.joshy.gfx.node.control.Control;

/**
 * Implements drawing Controls using the CSS box model.
 * This class is usually a singleton and should hold no state. All state
 * comes from the BoxStage instance passed in along with the control. 
 */
public class SuperSkin extends CSSSkin {
    
    public void draw2(GFX gfx, BoxState box, Control control, String content, State state) {
        CSSMatcher matcher = createMatcher(control,state);
        //draw background
        drawBackground2(gfx, matcher, "", box);
        //draw content
        drawContent(gfx, matcher, "", box, content);
        //draw border
        drawBorder2(gfx,matcher,"",box);
        //debug overlay
        drawDebugOverlay(gfx,matcher,"",box);
    }

    private void drawDebugOverlay(GFX gfx, CSSMatcher matcher, String prefix, BoxState box) {
        Insets borderWidth = getBorderWidth(matcher);

        //debugging
        if("true".equals(set.findStringValue(matcher,"debug-margin"))) {
            gfx.setPaint(FlatColor.RED);
            gfx.drawRect(0,0,box.width,box.height);
        }
        if("true".equals(set.findStringValue(matcher,"debug-border"))) {
            gfx.setPaint(FlatColor.GREEN);
            gfx.drawRect(box.margin.getLeft(),
                    box.margin.getTop(),
                    box.width-box.margin.getLeft()-box.margin.getRight(),
                    box.height-box.margin.getTop()-box.margin.getBottom());
        }
        if("true".equals(set.findStringValue(matcher,"debug-padding"))) {
            gfx.setPaint(FlatColor.BLUE);
            gfx.drawRect(
                    box.margin.getLeft()+borderWidth.getLeft(),
                    box.margin.getTop()+borderWidth.getTop(),
                    box.width-box.margin.getLeft()-box.margin.getRight()-borderWidth.getLeft()-borderWidth.getRight(),
                    box.height-box.margin.getTop()-box.margin.getBottom()-borderWidth.getTop()-borderWidth.getBottom());
        }
    }

    private void drawContent(GFX gfx, CSSMatcher matcher, String prefix, BoxState box, String content) {
        Image icon = getIcon(matcher);
        Font font = getFont(matcher);

        Insets borderWidth = getBorderWidth(matcher);
        //draw the internal content
        double contentX = box.margin.getLeft()+borderWidth.getLeft()+box.padding.getLeft();
        double contentY = box.margin.getTop()+borderWidth.getTop()+box.padding.getTop();
        String textAlign = set.findStringValue(matcher.element,"text-align");
        gfx.setPaint(new FlatColor(set.findColorValue(matcher,"color")));

        double textX = contentX;
        double textWidth = box.contentWidth;
        if(icon != null) {
            textX += icon.getWidth();
            textWidth -= icon.getWidth();
        }
        //do drop shadow on text content
        if(content != null && content.length() > 0) {
            BaseValue value = set.findValue(matcher, "text-shadow");
            if(value instanceof ShadowValue) {
                ShadowValue shadow = (ShadowValue) value;
                ImageBuffer buf = gfx.createBuffer((int)textWidth,(int)box.contentHeight);
                GFX g2 = buf.getGFX();
                g2.setPaint(new FlatColor(shadow.getColor(),0.3));
                g2.translate(-textX,-contentY);
                Font.drawCentered(g2,content,font,textX,contentY,textWidth,box.contentHeight,true);
                buf.apply(new BlurEffect(3,3));
                gfx.draw(buf,textX+shadow.getXoffset(),contentY+shadow.getYoffset());
            }
        }

        if("center".equals(textAlign)) {
            Font.drawCentered(gfx,content,font,textX,contentY,textWidth,box.contentHeight,true);
        } else {
            Font.drawCenteredVertically(gfx,content,font,textX,contentY,textWidth,box.contentHeight,true);
        }

        if(icon != null) {
            gfx.drawImage(icon,contentX,contentY);
        }

    }


    private void drawBackground2(GFX g, CSSMatcher matcher, String prefix, BoxState box) {
        double backWidth = box.width-box.margin.getLeft()-box.margin.getRight();
        double backHeight = box.height-box.margin.getTop()-box.margin.getBottom();
        Bounds bounds = new Bounds(box.margin.getLeft(),box.margin.getTop(),backWidth,backHeight);

        g.translate(bounds.getX(),bounds.getY());
        //Insets margin = getMargin(matcher.element,prefix);
        BaseValue background = set.findValue(matcher,prefix+"background");
        int radius = set.findIntegerValue(matcher,prefix+"border-radius");

        if(!"transparent".equals(set.findStringValue(matcher,prefix+"background-color"))) {
            g.setPaint(new FlatColor(set.findColorValue(matcher,prefix+"background-color")));
            if(background instanceof LinearGradientValue) {
                g.setPaint(toGradientFill((LinearGradientValue)background,bounds.getWidth(),bounds.getHeight()));
            }
            if(radius == 0) {
                g.fillRect(
                        box.margin.getLeft(),
                        box.margin.getTop(),
                        bounds.getWidth()-box.margin.getLeft()-box.margin.getRight(),
                        bounds.getHeight()-box.margin.getTop()-box.margin.getBottom());
            } else {
                g.fillRoundRect(
                        box.margin.getLeft(),
                        box.margin.getTop(),
                        bounds.getWidth()-box.margin.getLeft()-box.margin.getRight(),
                        bounds.getHeight()-box.margin.getTop()-box.margin.getBottom(),
                        radius,
                        radius);
            }
        }
        g.translate(-bounds.getX(),-bounds.getY());
    }


    private void drawBorder2(GFX gfx, CSSMatcher matcher, String prefix, BoxState box) {
        double backWidth = box.width-box.margin.getLeft()-box.margin.getRight();
        double backHeight = box.height-box.margin.getTop()-box.margin.getBottom();
        Bounds bounds = new Bounds(box.margin.getLeft(),box.margin.getTop(),backWidth,backHeight);

        Insets margin = getMargin(matcher);
        Insets borderWidth = getBorderWidth(matcher);
        /*if(prefix != null && !prefix.trim().equals("")) {
            margin = new Insets(set.findIntegerValue(matcher.element, prefix+"margin"));
            borderWidth = new Insets(set.findIntegerValue(matcher.element, prefix+"border-width"));
        }*/
        int borderRadius = set.findIntegerValue(matcher,prefix+"border-radius");
        if(!borderWidth.allEquals(0)) {
            gfx.setPaint(new FlatColor(set.findColorValue(matcher,prefix+"border-color")));
            double bw = 1;
            gfx.setStrokeWidth(bw);
            if(borderRadius == 0) {
                gfx.drawRect(
                        bounds.getX()+margin.getLeft(),
                        bounds.getY()+margin.getTop(),
                        bounds.getWidth()-margin.getLeft()-margin.getRight(),
                        bounds.getHeight()-margin.getTop()-margin.getBottom()
                );
            } else {
                gfx.drawRoundRect(
                        bounds.getX()+margin.getLeft(),
                        bounds.getY()+margin.getTop(),
                        bounds.getWidth()-margin.getLeft()-margin.getRight(),
                        bounds.getHeight()-margin.getTop()-margin.getBottom(),
                        borderRadius,borderRadius
                );
            }
            gfx.setStrokeWidth(1);
        }
    }

}
