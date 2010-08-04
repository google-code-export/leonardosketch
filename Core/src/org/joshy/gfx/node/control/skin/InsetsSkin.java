package org.joshy.gfx.node.control.skin;

import org.joshy.gfx.node.Skin;
import org.w3c.dom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 21, 2010
 * Time: 9:38:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class InsetsSkin extends Skin {

    private double left;
    private double top;
    private double bottom;
    private double right;
    public static final InsetsSkin DEFAULT = new InsetsSkin();

    public InsetsSkin(Element val) {
        super();
        left = Integer.parseInt(val.getAttribute("left"));
        right = Double.parseDouble(val.getAttribute("right"));
        top = Integer.parseInt(val.getAttribute("top"));
        bottom = Double.parseDouble(val.getAttribute("bottom"));
    }

    public InsetsSkin() {
        super();
    }

    public InsetsSkin(double top, double right, double bottom, double left) {
        super();
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    @Override
    public String toString() {
        return "InsetsSkin{" +
                "left=" + left +
                ", top=" + top +
                ", bottom=" + bottom +
                ", right=" + right +
                '}';
    }

    public double getLeft() {
        return left;
    }

    public double getRight() {
        return right;
    }

    public double getBottom() {
        return bottom;
    }

    public double getTop() {
        return top;
    }
}
