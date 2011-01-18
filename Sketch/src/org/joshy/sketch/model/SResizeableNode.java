package org.joshy.sketch.model;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jun 10, 2010
 * Time: 12:29:38 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SResizeableNode extends SelfDrawable {
    double getX();

    double getWidth();

    void setWidth(double width);

    double getHeight();

    void setHeight(double height);

    double getY();

    void setY(double y);

    void setX(double x);

    //node stuff
    public double getTranslateX();
    public double getTranslateY();
    public void setTranslateX(double x);
    public void setTranslateY(double y);

    public double getPreferredAspectRatio();

    public boolean constrainByDefault();
}
