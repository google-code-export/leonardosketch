package org.joshy.gfx.css;

import org.joshy.gfx.css.values.BaseValue;
import org.joshy.gfx.css.values.ShadowValue;
import org.joshy.gfx.draw.*;
import org.joshy.gfx.draw.effects.BlurEffect;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Insets;
import org.joshy.gfx.node.control.Control;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Sep 19, 2010
 * Time: 12:02:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class BoxPainter {
    int borderRadius;
    Insets margin;
    public boolean transparent;
    public FlatColor background_color;
    public boolean gradient;
    public GradientFill gradientFill;
    public Insets borderWidth;
    public FlatColor border_color;
    public Image icon;
    public Font font;
    public String textAlign;
    public FlatColor color;
    public BaseValue text_shadow;
    private String oldText;
    private ImageBuffer oldBuf;


    public void draw(GFX g, CSSSkin.BoxState box, Control control, String text) {
        drawBackground(g,box,control,text);
        drawContent(g, box, text);
        drawBorder(g, box);
    }
    
    public void drawBackground(GFX g, CSSSkin.BoxState box, Control control, String text) {
        double backWidth = box.width-box.margin.getLeft()-box.margin.getRight();
        double backHeight = box.height-box.margin.getTop()-box.margin.getBottom();
        Bounds bounds = new Bounds(box.margin.getLeft(),box.margin.getTop(),backWidth,backHeight);
        g.translate(bounds.getX(),bounds.getY());
        //Insets margin = box.margin;
        //BaseValue background = set.findValue(matcher,prefix+"background");
        //int borderRadius = set.findIntegerValue(matcher,prefix+"border-radius");

        if(!transparent) {
            g.setPaint(background_color);
            if(gradient) {
                g.setPaint(gradientFill);
            }
            if(borderRadius == 0) {
                g.fillRect(
                        0+margin.getLeft(),
                        0+margin.getTop(),
                        bounds.getWidth()-margin.getLeft()-margin.getRight(),
                        bounds.getHeight()-margin.getTop()-margin.getBottom()
                        );
            } else {
                g.fillRoundRect(
                        0+margin.getLeft(),
                        0+margin.getTop(),
                        bounds.getWidth()-margin.getLeft()-margin.getRight(),
                        bounds.getHeight()-margin.getTop()-margin.getBottom(),
                        borderRadius,
                        borderRadius);
            }
        }
        g.translate(-bounds.getX(),-bounds.getY());
    }

    private void drawBorder(GFX gfx, CSSSkin.BoxState box) {
        double backWidth = box.width-box.margin.getLeft()-box.margin.getRight();
        double backHeight = box.height-box.margin.getTop()-box.margin.getBottom();
        Bounds bounds = new Bounds(box.margin.getLeft(),box.margin.getTop(),backWidth,backHeight);

        if(!borderWidth.allEquals(0)) {
            gfx.setPaint(border_color);
            if(borderRadius == 0) {
                if(borderWidth.allEqual()) {
                    gfx.setStrokeWidth(borderWidth.getLeft());
                    gfx.drawRect(
                            bounds.getX()+margin.getLeft(),
                            bounds.getY()+margin.getTop(),
                            bounds.getWidth()-margin.getLeft()-margin.getRight(),
                            bounds.getHeight()-margin.getTop()-margin.getBottom()
                    );
                    gfx.setStrokeWidth(1);
                } else {
                    if(borderWidth.getLeft()>0) {
                        gfx.setStrokeWidth(borderWidth.getLeft());
                        gfx.drawLine(bounds.getX(),bounds.getY(),bounds.getX(),bounds.getY()+bounds.getHeight());
                    }
                    if(borderWidth.getTop()>0) {
                        gfx.setStrokeWidth(borderWidth.getTop());
                        gfx.drawLine(bounds.getX(),bounds.getY(),bounds.getX()+bounds.getWidth(),bounds.getY());
                    }
                }
            } else {
                gfx.drawRoundRect(
                        bounds.getX()+margin.getLeft(),
                        bounds.getY()+margin.getTop(),
                        bounds.getWidth()-margin.getLeft()-margin.getRight(),
                        bounds.getHeight()-margin.getTop()-margin.getBottom(),
                        borderRadius,borderRadius
                );
            }
        }
    }

    private void drawContent(GFX gfx, CSSSkin.BoxState box, String content) {
        //draw the internal content
        double contentX = box.margin.getLeft()+borderWidth.getLeft()+box.padding.getLeft();
        double contentY = box.margin.getTop()+borderWidth.getTop()+box.padding.getTop();
        gfx.setPaint(color);

        double textX = contentX;
        double textWidth = box.contentWidth;
        if(icon != null) {
            textX += icon.getWidth();
            textWidth -= icon.getWidth();
        }
        //do drop shadow on text content
        if(content != null && content.length() > 0) {
            //BaseValue value = null;//set.findValue(matcher, "text-shadow");
            if(text_shadow instanceof ShadowValue) {
                ShadowValue shadow = (ShadowValue) text_shadow;
                if(!content.equals(oldText)) {
                    ImageBuffer buf = gfx.createBuffer((int)textWidth,(int)box.contentHeight);
                    if(buf != null) {
                        GFX g2 = buf.getGFX();
                        g2.setPaint(new FlatColor(shadow.getColor(),0.3));
                        g2.translate(-textX,-contentY);
                        Font.drawCentered(g2,content,font,textX,contentY,textWidth,box.contentHeight,true);
                        buf.apply(new BlurEffect(3,3));
                        oldBuf = buf;
                    }
                    oldText = content;
                }
                if(oldBuf != null) {
                    gfx.draw(oldBuf,textX+shadow.getXoffset(),contentY+shadow.getYoffset());
                }

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

}
