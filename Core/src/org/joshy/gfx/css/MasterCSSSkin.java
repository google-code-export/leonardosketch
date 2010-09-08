package org.joshy.gfx.css;

import org.joshy.gfx.Core;
import org.joshy.gfx.css.values.BaseValue;
import org.joshy.gfx.css.values.LinearGradientValue;
import org.joshy.gfx.draw.*;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Insets;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.util.URLUtils;

import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Aug 2, 2010
 * Time: 11:33:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class MasterCSSSkin {
    protected CSSRuleSet set;
    private Font defaultFont = Font.name("Arial").size(13).resolve();

    public BoxState getSize(Control control) {
        BoxState size = new BoxState();
        size.width = control.getWidth();
        size.height = control.getHeight();

        if(set == null) {
            size.width = 100;
            size.height = 100;
            return size;
        }

        String name = control.getClass().getSimpleName();
        Insets margin = getMargin(name);
        Insets padding = getPadding(name);
        size.contentWidth = control.getWidth()-margin.getLeft()-margin.getRight()-padding.getLeft()-padding.getRight();
        size.contentHeight = control.getHeight()-margin.getTop()-margin.getBottom()-padding.getTop()-padding.getBottom();
        return size;
    }

    protected Insets getPadding(String name) {
        return getPadding(name,"");
    }
    protected Insets getPadding(String name, String prefix) {
        int padding_left = set.findIntegerValue(name,prefix+"padding-left");
        int padding_right = set.findIntegerValue(name,prefix+"padding-right");
        int padding_top = set.findIntegerValue(name,prefix+"padding-top");
        int padding_bottom = set.findIntegerValue(name,prefix+"padding-bottom");
        return new Insets(padding_top,padding_right,padding_bottom,padding_left);
    }

    protected Insets getMargin(String name) {
        return getMargin(name,"");
    }
    protected Insets getMargin(String name, String prefix) {
        int margin_left = set.findIntegerValue(name,prefix+"margin-left");
        int margin_right = set.findIntegerValue(name,prefix+"margin-right");
        int margin_top = set.findIntegerValue(name,prefix+"margin-top");
        int margin_bottom = set.findIntegerValue(name,prefix+"margin-bottom");
        return new Insets(margin_top,margin_right,margin_bottom,margin_left);
    }

    protected Insets getMargin(CSSMatcher matcher) {
        return getMargin(matcher.element, "");
    }

    protected Insets getBorderWidth(String name) {
        return getBorderWidth(name,"");
    }
    protected Insets getBorderWidth(String name, String prefix) {
        int border_left = set.findIntegerValue(name,prefix+"border-left-width");
        int border_right = set.findIntegerValue(name,prefix+"border-right-width");
        int border_top = set.findIntegerValue(name,prefix+"border-top-width");
        int border_bottom = set.findIntegerValue(name,prefix+"border-bottom-width");
        return new Insets(border_top,border_right,border_bottom,border_left);
    }


    public void draw(GFX g, Control control, BoxState size) {
        if(set == null) {
            g.setPaint(FlatColor.BLUE);
            g.fillRect(0,0,20,20);
            return;
        }
        CSSMatcher matcher = createMatcher(control,null);
        if(Core.getShared().getFocusManager().getFocusedNode()==control) {
            matcher.pseudo = "active";
        }
        Insets margin = getMargin(matcher.element);

        //draw the background
        double backWidth = size.width-margin.getLeft()-margin.getRight();
        double backHeight = size.height-margin.getTop()-margin.getBottom();
        Bounds bounds = new Bounds(margin.getTop(),margin.getBottom(),backWidth,backHeight);
        drawBackground(g,matcher, "", bounds);
        //draw the border
        drawBorder(g,matcher, "", bounds);

    }

    public void drawBorder(GFX g, CSSMatcher matcher, String prefix, Bounds bounds) {
        Insets margin = getMargin(matcher.element);
        Insets borderWidth = getBorderWidth(matcher.element);
        if(prefix != null && !prefix.trim().equals("")) {
            margin = new Insets(set.findIntegerValue(matcher.element, prefix+"margin"));
            borderWidth = new Insets(set.findIntegerValue(matcher.element, prefix+"border-width"));
        }
        int borderRadius = set.findIntegerValue(matcher.element,prefix+"border-radius");
        if(!borderWidth.allEquals(0)) {
            g.setPaint(new FlatColor(set.findColorValue(matcher,prefix+"border-color")));
            g.setStrokeWidth(borderWidth.getLeft());
            if(borderRadius == 0) {
                g.drawRect(
                        bounds.getX()+margin.getLeft()+borderWidth.getLeft()/2,
                        bounds.getY()+margin.getTop()+borderWidth.getTop()/2,
                        bounds.getWidth()-margin.getLeft()-margin.getRight()-borderWidth.getLeft(),
                        bounds.getHeight()-margin.getTop()-margin.getBottom()-borderWidth.getTop());
            } else {
                g.drawRoundRect(
                        bounds.getX()+margin.getLeft()+borderWidth.getLeft()/2,
                        bounds.getY()+margin.getTop()+borderWidth.getTop()/2,
                        bounds.getWidth()-margin.getLeft()-margin.getRight()-borderWidth.getLeft(),
                        bounds.getHeight()-margin.getTop()-margin.getBottom()-borderWidth.getTop(),
                        borderRadius,borderRadius);
            }
            g.setStrokeWidth(1);
        }
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

    public CSSMatcher createMatcher(Control control, CSSSkin.State state) {
        CSSMatcher matcher = new CSSMatcher();
        matcher.element = control.getClass().getSimpleName();
        matcher.id = control.getId();
        if(state == CSSSkin.State.Hover) {
            matcher.pseudo = "hover";
        }
        if(state == CSSSkin.State.Pressed) {
            matcher.pseudo = "pressed";
        }
        if(state == CSSSkin.State.Selected) {
            matcher.pseudo = "selected";
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

    public void drawBackground(GFX g, CSSMatcher matcher, String prefix, Bounds b) {
        g.translate(b.getX(),b.getY());
        Insets margin = getMargin(matcher.element,prefix);
        BaseValue background = set.findValue(matcher,prefix+"background");
        int radius = set.findIntegerValue(matcher.element,prefix+"border-radius");

        if(!"transparent".equals(set.findStringValue(matcher,prefix+"background-color"))) {
            g.setPaint(new FlatColor(set.findColorValue(matcher,prefix+"background-color")));
            if(background instanceof LinearGradientValue) {
                g.setPaint(toGradientFill((LinearGradientValue)background,b.getWidth(),b.getHeight()));
            }
            if(radius == 0) {
                g.fillRect(0+margin.getLeft(),0+margin.getTop(),b.getWidth()-margin.getLeft()-margin.getRight(), b.getHeight()-margin.getTop()-margin.getBottom());
            } else {
                g.fillRoundRect(0+margin.getLeft(),0+margin.getTop(),b.getWidth()-margin.getLeft()-margin.getRight(), b.getHeight()-margin.getTop()-margin.getBottom(),radius,radius);
            }
        }
        g.translate(-b.getX(),-b.getY());
    }

    public void drawText(GFX g, CSSMatcher matcher, String prefix, Bounds b, String text) {
        g.translate(b.getX(),b.getY());
        Insets margin = getMargin(matcher.element,prefix);
        Insets borderWidth = getBorderWidth(matcher.element,prefix);
        Insets padding = getPadding(matcher.element,prefix);
        g.setPaint(new FlatColor(set.findColorValue(matcher,prefix+"color")));
        double x = margin.getLeft() + borderWidth.getLeft() + padding.getLeft();
        Font font = getDefaultFont();
        double tw = font.getWidth(text);
        double th = font.getAscender();
        double ty = 0 + (b.getHeight() -th)/2 + font.getAscender();
        g.drawText(text,font,x, ty);
        g.translate(-b.getX(),-b.getY());
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
