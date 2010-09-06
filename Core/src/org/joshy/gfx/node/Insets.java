package org.joshy.gfx.node;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Feb 2, 2010
 * Time: 7:02:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class Insets {
    private double top;
    private double bottom;
    private double left;
    private double right;

    public Insets(double top, double right, double bottom, double left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public Insets(double w) {
        this(w,w,w,w);
    }

    public double getTop() {
        return top;
    }

    public double getLeft() {
        return left;
    }

    public double getBottom() {
        return bottom;
    }

    public double getRight() {
        return right;
    }

    public boolean allEquals(double i) {
        return (left == i && right == i && top == i && bottom == i);
    }
}
