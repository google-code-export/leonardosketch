package org.joshy.sketch.model;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.util.u;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 5/17/12
 * Time: 7:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class MaskedImage extends SNode implements SelfDrawable {
    private SShape shape;
    private SImage image;

    public void setImage(SImage image) {
        this.image = image;
    }

    public void setShape(SShape shape) {
        this.shape = shape;
    }

    @Override
    public Bounds getBounds() {
        return shape.getBounds();
    }

    @Override
    public Bounds getTransformedBounds() {
        Bounds b =  shape.getTransformedBounds();
        return new Bounds(b.getX()+getTranslateX(),b.getY()+getTranslateY(),b.getWidth(),b.getHeight());
    }

    @Override
    public boolean contains(Point2D point) {
        return shape.contains(point);
    }

    public void draw(GFX g) {
        double dx = image.getTranslateX();
        double dy = image.getTranslateY();
        Area a = shape.toArea();
        a.transform(AffineTransform.getTranslateInstance(-dx, -dy));
        g.setMask(a);
        g.translate(dx,dy);
        image.draw(g);
        g.translate(-dx,-dy);
        g.setMask(null);
    }

    public SShape getShape() {
        return shape;
    }

    public SImage getImage() {
        return image;
    }
}
