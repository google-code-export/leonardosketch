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
import org.joshy.gfx.util.URLUtils;

import java.net.URI;

/**
 * Implements drawing Controls using the CSS box model.
 * This class is usually a singleton and should hold no state. All state
 * comes from the BoxStage instance passed in along with the control.
 */
public class CSSSkin {
    private CSSRuleSet set;
    private Font defaultFont = Font.name("Arial").size(13).resolve();

    /* good functions */
    public CSSRuleSet getCSSSet() {
        return this.set;
    }

    public enum State {
        Pressed, Hover, Selected, None
    }

    public BoxPainter createBoxPainter(Control control, BoxState boxState, String text, CSSSkin.State state) {
        
        CSSMatcher matcher = createMatcher(control, state);
        double backWidth = boxState.width-boxState.margin.getLeft()-boxState.margin.getRight();
        double backHeight = boxState.height-boxState.margin.getTop()-boxState.margin.getBottom();

        Bounds bounds = new Bounds(boxState.margin.getLeft(),boxState.margin.getTop(),backWidth,backHeight);
        String prefix = "";

        BoxPainter boxPainter = new BoxPainter();
        //background stuff
        BaseValue background = set.findValue(matcher,prefix+"background");
        boxPainter.borderRadius = set.findIntegerValue(matcher,prefix+"border-radius");
        boxPainter.transparent = "transparent".equals(set.findStringValue(matcher,prefix+"background-color"));
        if(!boxPainter.transparent) {
            boxPainter.background_color = new FlatColor(set.findColorValue(matcher,prefix+"background-color"));
        } else {
            boxPainter.background_color = FlatColor.BLACK;
        }
        if(background instanceof LinearGradientValue) {
            boxPainter.gradient = true;
            boxPainter.gradientFill = toGradientFill((LinearGradientValue)background,bounds.getWidth(),bounds.getHeight());
        }

        //border stuff
        boxPainter.margin = getMargin(matcher);
        boxPainter.borderWidth = getBorderWidth(matcher,"");
        if(!boxPainter.borderWidth.allEquals(0)) {
            boxPainter.border_color = (new FlatColor(set.findColorValue(matcher,prefix+"border-color")));
        }

        //content stuff
        boxPainter.icon = getIcon(matcher);
        boxPainter.font = getFont(matcher);
        boxPainter.textAlign = set.findStringValue(matcher.element,"text-align");
        boxPainter.color = new FlatColor(set.findColorValue(matcher,"color"));
        boxPainter.text_shadow = set.findValue(matcher, "text-shadow");

        return boxPainter;
    }


    protected Font getFont(CSSMatcher matcher) {
        int fontSize = set.findIntegerValue(matcher, "font-size");
        Font font = Font.name("Arial").size(fontSize).resolve();
        return font;
    }

    public void draw(GFX gfx, BoxState box, Control control, String content, State state) {
        CSSMatcher matcher = createMatcher(control,state);
        //draw background
        drawBackground(gfx, matcher, "", box);
        //draw content
        drawContent(gfx, matcher, "", box, content);
        //draw border
        drawBorder(gfx,matcher,"",box);
        //debug overlay
        drawDebugOverlay(gfx,matcher,"",box);
    }

    private void drawDebugOverlay(GFX gfx, CSSMatcher matcher, String prefix, BoxState box) {
        Insets borderWidth = getBorderWidth(matcher,"");

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

        Insets borderWidth = getBorderWidth(matcher,"");
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
                if(buf != null) {
                GFX g2 = buf.getGFX();
                g2.setPaint(new FlatColor(shadow.getColor(),0.3));
                g2.translate(-textX,-contentY);
                Font.drawCentered(g2,content,font,textX,contentY,textWidth,box.contentHeight,true);
                buf.apply(new BlurEffect(3,3));
                gfx.draw(buf,textX+shadow.getXoffset(),contentY+shadow.getYoffset());
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

    private void drawBackground(GFX g, CSSMatcher matcher, String prefix, BoxState box) {
        double backWidth = box.width-box.margin.getLeft()-box.margin.getRight();
        double backHeight = box.height-box.margin.getTop()-box.margin.getBottom();
        Bounds bounds = new Bounds(box.margin.getLeft(),box.margin.getTop(),backWidth,backHeight);
        drawBackground(g, matcher, prefix, bounds);
    }
    
    public void drawBackground(GFX g, CSSMatcher matcher, String prefix, Bounds bounds) {
        g.translate(bounds.getX(),bounds.getY());
        Insets margin = getMargin(matcher,prefix);
        BaseValue background = set.findValue(matcher,prefix+"background");
        int radius = set.findIntegerValue(matcher,prefix+"border-radius");

        if(!"transparent".equals(set.findStringValue(matcher,prefix+"background-color"))) {
            g.setPaint(new FlatColor(set.findColorValue(matcher,prefix+"background-color")));
            if(background instanceof LinearGradientValue) {
                g.setPaint(toGradientFill((LinearGradientValue)background,bounds.getWidth(),bounds.getHeight()));
            }
            if(radius == 0) {
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
                        radius,
                        radius);
            }
        }
        g.translate(-bounds.getX(),-bounds.getY());
    }


    public void drawBorder(GFX gfx, CSSMatcher matcher, String prefix, Bounds bounds) {
        Insets margin = getMargin(matcher,prefix);
        Insets borderWidth = getBorderWidth(matcher,prefix);
        int borderRadius = set.findIntegerValue(matcher,prefix+"border-radius");
        if(!borderWidth.allEquals(0)) {
            gfx.setPaint(new FlatColor(set.findColorValue(matcher,prefix+"border-color")));
            if(borderRadius <= 0) {
                if(borderWidth.allEqual()) {
                    if(borderWidth.getLeft() >0) {
                        gfx.setStrokeWidth(borderWidth.getLeft());
                        gfx.drawRect(
                                bounds.getX()+margin.getLeft(),
                                bounds.getY()+margin.getTop(),
                                bounds.getWidth()-margin.getLeft()-margin.getRight(),
                                bounds.getHeight()-margin.getTop()-margin.getBottom()
                        );
                    }
                    gfx.setStrokeWidth(1);
                } else {
                    double x = bounds.getX()+margin.getLeft();
                    double y = bounds.getY()+margin.getTop();
                    double w = bounds.getWidth()-margin.getLeft()-margin.getRight()-1;
                    double h = bounds.getHeight()-margin.getTop()-margin.getBottom()-1;
                    if(borderWidth.getLeft()>0) {
                        gfx.setStrokeWidth(borderWidth.getLeft());
                        gfx.drawLine(x,y,x,y+h);
                    }
                    if(borderWidth.getTop()>0) {
                        gfx.setStrokeWidth(borderWidth.getTop());
                        gfx.drawLine(x,y,x+w,y);
                    }
                    if(borderWidth.getRight()>0) {
                        gfx.setStrokeWidth(borderWidth.getRight());
                        gfx.drawLine(x+w,y,x+w,y+h);
                    }
                    if(borderWidth.getBottom()>0) {
                        gfx.setStrokeWidth(borderWidth.getBottom());
                        gfx.drawLine(x,y+h,  x+w,y+h);
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
            gfx.setStrokeWidth(1);
        }
    }

    private void drawBorder(GFX gfx, CSSMatcher matcher, String prefix, BoxState box) {
        double backWidth = box.width-box.margin.getLeft()-box.margin.getRight();
        double backHeight = box.height-box.margin.getTop()-box.margin.getBottom();
        Bounds bounds = new Bounds(box.margin.getLeft(),box.margin.getTop(),backWidth,backHeight);

        Insets margin = getMargin(matcher);
        Insets borderWidth = getBorderWidth(matcher,"");
        /*if(prefix != null && !prefix.trim().equals("")) {
            margin = new Insets(set.findIntegerValue(matcher.element, prefix+"margin"));
            borderWidth = new Insets(set.findIntegerValue(matcher.element, prefix+"border-width"));
        }*/
        int borderRadius = set.findIntegerValue(matcher,prefix+"border-radius");
        if(!borderWidth.allEquals(0)) {
            gfx.setPaint(new FlatColor(set.findColorValue(matcher,prefix+"border-color")));
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
        Insets margin = getMargin(matcher);

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
        drawBackground(g, matcher, "thumb-", thumbBounds);
        drawBorder(    g,matcher,"thumb-",thumbBounds);
    }

    public BoxState getSize(Control control) {
        CSSMatcher matcher = createMatcher(control, State.None);
        BoxState size = new BoxState();

        if(set == null) {
            size.width = 100;
            size.height = 100;
            return size;
        }

        size.width = set.findIntegerValue(matcher,"width");
        size.height = set.findIntegerValue(matcher,"height");

        Insets margin = getMargin(matcher);
        Insets padding = getPadding(matcher);
        size.contentWidth = control.getWidth()-margin.getLeft()-margin.getRight()-padding.getLeft()-padding.getRight();
        size.contentHeight = control.getHeight()-margin.getTop()-margin.getBottom()-padding.getTop()-padding.getBottom();
        return size;
    }

    public BoxState getSize(Control control, String content) {
        BoxState size = getSize(control);
        CSSMatcher matcher = createMatcher(control,null);
        Image icon = getIcon(matcher);
        size.margin = getMargin(matcher);
        size.padding = getPadding(matcher);
        size.borderWidth = getBorderWidth(matcher,"");
        size.contentWidth =  control.getWidth()-size.margin.getLeft()-size.margin.getRight()-size.padding.getLeft()-size.padding.getRight();
        size.contentHeight = control.getHeight()-size.margin.getTop()-size.margin.getBottom()-size.padding.getTop()-size.padding.getBottom();

        //calc the sizes
        if("true".equals(set.findStringValue(matcher,"shrink-to-fit"))) {
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
    protected Insets getPadding(CSSMatcher matcher) {
        return getPadding(matcher,"");
    }
    protected Insets getPadding(CSSMatcher matcher, String prefix) {
        int padding_left = set.findIntegerValue(matcher,prefix+"padding-left");
        int padding_right = set.findIntegerValue(matcher,prefix+"padding-right");
        int padding_top = set.findIntegerValue(matcher,prefix+"padding-top");
        int padding_bottom = set.findIntegerValue(matcher,prefix+"padding-bottom");
        return new Insets(padding_top,padding_right,padding_bottom,padding_left);
    }

    protected Insets getMargin(CSSMatcher matcher) {
        return getMargin(matcher, "");
    }

    protected Insets getMargin(CSSMatcher matcher, String prefix) {
        int margin_left = set.findIntegerValue(matcher,prefix+"margin-left");
        int margin_right = set.findIntegerValue(matcher,prefix+"margin-right");
        int margin_top = set.findIntegerValue(matcher,prefix+"margin-top");
        int margin_bottom = set.findIntegerValue(matcher,prefix+"margin-bottom");
        return new Insets(margin_top,margin_right,margin_bottom,margin_left);
    }


    protected Insets getBorderWidth(CSSMatcher matcher, String prefix) {
        int border_left = set.findIntegerValue(matcher,prefix+"border-left-width");
        int border_right = set.findIntegerValue(matcher,prefix+"border-right-width");
        int border_top = set.findIntegerValue(matcher,prefix+"border-top-width");
        int border_bottom = set.findIntegerValue(matcher,prefix+"border-bottom-width");
        return new Insets(border_top,border_right,border_bottom,border_left);
    }

    public void drawText(GFX g, CSSMatcher matcher, String prefix, Bounds b, String text) {
        g.translate(b.getX(),b.getY());
        Insets margin = getMargin(matcher,prefix);
        Insets borderWidth = getBorderWidth(matcher,prefix);
        Insets padding = getPadding(matcher,prefix);
        g.setPaint(new FlatColor(set.findColorValue(matcher,prefix+"color")));
        double x = margin.getLeft() + borderWidth.getLeft() + padding.getLeft();
        Font font = getDefaultFont();
        double tw = font.getWidth(text);
        double th = font.getAscender();
        double ty = 0 + (b.getHeight() -th)/2 + font.getAscender();
        g.drawText(text,font,x, ty);
        g.translate(-b.getX(),-b.getY());
    }

    public Insets getInsets(Control control) {
        CSSMatcher matcher = createMatcher(control,null);
        Insets margin = getMargin(matcher, "");
        Insets border = getBorderWidth(matcher, "");
        Insets padding = getPadding(matcher,"");
        return new Insets(margin,border,padding);
    }
    public void setRuleSet(CSSRuleSet set) {
        this.set = set;
    }

    public CSSRuleSet getRuleSet() {
        return this.set;
    }

    protected Image getIcon(CSSMatcher matcher) {
        Image icon = null;
        URI uri = set.findURIValue(matcher, "icon");
        if(uri != null) {
//            u.p("doing the icon: " + uri);
//            u.p("base URI = " + set.getBaseURI());
            if(set.getBaseURI() != null) {
                try {
                    URI imageURI = URLUtils.safeURIResolve(set.getBaseURI(),uri);
                    //URI imageURI = set.getBaseURI().resolve(uri);
//                    u.p("image uri = " + imageURI);
                    icon = Image.getImageFromCache(imageURI.toURL());
//                    u.p("Image = " + icon);
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        return icon;
    }

    public CSSMatcher createMatcher(Control control, State state) {
        CSSMatcher matcher = new CSSMatcher(control);
        if(state == State.Hover) {
            matcher.pseudo = "hover";
        }
        if(state == State.Pressed) {
            matcher.pseudo = "pressed";
        }
        if(state == State.Selected) {
            matcher.pseudo = "selected";
        }
        if(control instanceof Scrollbar) {
            if(((Scrollbar)control).isVertical()) {
                matcher.pseudo = "vertical";
            }
        }
        return matcher;
    }

    protected GradientFill toGradientFill(LinearGradientValue grad, double backWidth, double backHeight) {
        GradientFill gf = new GradientFill(
                new FlatColor(grad.getStop(0).getColor()),
                new FlatColor(grad.getStop(1).getColor()),
                90, false
        );
        gf.startX = 0;
        gf.endX = 0;
        gf.startY = 0;
        gf.endY = backHeight;
        if("left".equals(grad.getPosition1())) {
            gf.endX = backWidth;
            gf.endY = 0;
        }
        return gf;
    }
    public Font getDefaultFont() {
        return defaultFont;
    }


    public static class BoxState {
        public double width;
        public double height;
        public double contentWidth;
        public double contentHeight;
        public Insets margin;
        public Insets borderWidth;
        public Insets padding;
        public double contentBaseline;

        public BoxState() {
        }

        public BoxState(double width, double height) {
            this.width = width;
            this.height = height;
            this.contentWidth = width;
            this.contentHeight = height;
        }
    }

}
