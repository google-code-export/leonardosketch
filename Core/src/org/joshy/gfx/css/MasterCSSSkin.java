package org.joshy.gfx.css;

import org.joshy.gfx.Core;
import org.joshy.gfx.css.values.BaseValue;
import org.joshy.gfx.css.values.LinearGradientValue;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.GradientFill;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Skin;
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
public class MasterCSSSkin extends Skin {
    protected CSSRuleSet set;

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
        int margin = set.findIntegerValue(name,"margin");
        int padding = set.findIntegerValue(name,"padding");
        size.contentWidth = control.getWidth()-margin*2-padding*2;
        size.contentHeight = control.getHeight()-margin*2-padding*2;
        return size;
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
        int margin = set.findIntegerValue(matcher.element,"margin");
//        int padding = set.findIntegerValue(matcher.element,"padding");
//        int borderWidth = set.findIntegerValue(matcher.element, "border-width");
//        int borderRadius = set.findIntegerValue(matcher.element,"border-radius");

        //draw the background
        double backWidth = size.width-margin*2;
        double backHeight = size.height-margin*2;
        Bounds bounds = new Bounds(margin,margin,backWidth,backHeight);
        drawBackground(g,matcher, "", bounds);
        //draw the border
        drawBorder(g,matcher, "", bounds);

    }

    protected void drawBorder(GFX g, CSSMatcher matcher, String prefix, Bounds bounds) {
        int margin = set.findIntegerValue(matcher.element,prefix+"margin");
        int borderWidth = set.findIntegerValue(matcher.element, prefix+"border-width");
        int borderRadius = set.findIntegerValue(matcher.element,prefix+"border-radius");
        if(borderWidth > 0) {
            g.setPaint(new FlatColor(set.findColorValue(matcher,prefix+"border-color")));
            g.setStrokeWidth(borderWidth);
            if(borderRadius == 0) {
                g.drawRect(
                        bounds.getX()+margin+borderWidth/2,
                        bounds.getY()+margin+borderWidth/2,
                        bounds.getWidth()-margin*2-borderWidth,
                        bounds.getHeight()-margin*2-borderWidth);
            } else {
                g.drawRoundRect(
                        bounds.getX()+margin+borderWidth/2,
                        bounds.getY()+margin+borderWidth/2,
                        bounds.getWidth()-margin*2-borderWidth,
                        bounds.getHeight()-margin*2-borderWidth,
                        borderRadius,borderRadius);
            }
            g.setStrokeWidth(1);
        }
    }

    public void setRuleSet(CSSRuleSet set) {
        this.set = set;
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

    protected void drawBackground(GFX g, CSSMatcher matcher, String prefix, Bounds b) {
        int margin = set.findIntegerValue(matcher.element,prefix+"margin");
        BaseValue background = set.findValue(matcher,prefix+"background");
        int radius = set.findIntegerValue(matcher.element,prefix+"border-radius");

        if(!"transparent".equals(set.findStringValue(matcher,prefix+"background-color"))) {
            g.setPaint(new FlatColor(set.findColorValue(matcher,prefix+"background-color")));
            if(background instanceof LinearGradientValue) {
                g.setPaint(toGradientFill((LinearGradientValue)background,b.getWidth(),b.getHeight()));
            }
            if(radius == 0) {
                g.fillRect(b.getX()+margin,b.getY()+margin,b.getWidth()-margin*2, b.getHeight()-margin*2);
            } else {
                g.fillRoundRect(b.getX()+margin,b.getY()+margin,b.getWidth()-margin*2, b.getHeight()-margin*2,radius,radius);
            }

        }
    }

    public static class BoxState {
        public double width;
        public double height;
        public double contentWidth;
        public double contentHeight;

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
