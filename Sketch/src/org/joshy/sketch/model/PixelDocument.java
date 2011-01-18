package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.util.GraphicsUtil;
import org.joshy.gfx.util.u;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: May 23, 2010
 * Time: 4:13:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class PixelDocument extends CanvasDocument {
    private int width;
    private int height;
    private BufferedImage img;
    private BufferedImage brush;

    public PixelDocument(int width, int height) {
        this.width = width;
        this.height = height;
        img = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);

    }

    public PixelDocument(BufferedImage img) {
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.img = img;
        brush = createBrush(10f, FlatColor.BLACK, 1.0f, true);        
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public int getPixel(int x, int y) {
        return img.getRGB(x,y);
    }

    public Image getBitmap() {
        return Image.create(img);
    }

    public void setPixel(int x, int y, int rgba) {
        if(x < 0) return;
        if(y < 0) return;
        if(x >= width) return;
        if(y >= height) return;
        img.setRGB(x,y,rgba);
    }

    public double stampBrush(Point2D start, Point2D end, double left, double opacity) {
        Graphics2D g = img.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opacity));
        left = stampMask(brush, g, new Point2D.Double(start.getX(), start.getY()), new Point2D.Double(end.getX(), end.getY()), left);
        g.dispose();
        return left;
    }

    private double stampMask(BufferedImage mask, Graphics2D g2, Point2D.Double startPoint, Point2D.Double endPoint, double leftOverDistance) {
        //spacing is 1/10th the width of the image. This is the best size determined through trial and error:
        //http://www.losingfight.com/blog/2007/08/18/how-to-implement-a-basic-bitmap-brush/
        float spacing = ((float)mask.getWidth())/10;
        //anything less that 1/2 pixel is a waste of time and hurts performance
        if(spacing < 0.5f) spacing = 0.5f;

        // Determine the delta of the x and y. This will determine the slope
        //	of the line we want to draw.
        double deltaX = endPoint.x - startPoint.x;
        double deltaY = endPoint.y - startPoint.y;

        // Normalize the delta vector we just computed, and that becomes our step increment
        //	for drawing our line, since the distance of a normalized vector is always 1
        float distance = (float) Math.sqrt( deltaX * deltaX + deltaY * deltaY );
        double stepX = 0.0f;
        double stepY = 0.0f;
        if ( distance > 0.0f ) {
            float invertDistance = 1.0f / distance;
            stepX = deltaX * invertDistance;
            stepY = deltaY * invertDistance;
        }

        float offsetX = 0.0f;
        float offsetY = 0.0f;

        // We're careful to only stamp at the specified interval, so its possible
        //	that we have the last part of the previous line left to draw. Be sure
        //	to add that into the total distance we have to draw.
        double totalDistance = leftOverDistance + distance;

        // While we still have distance to cover, stamp
        while ( totalDistance >= spacing ) {
            // ... increment the offset and stamp...
            // Increment where we put the stamp
            if ( leftOverDistance > 0 ) {
                // If we're making up distance we didn't cover the last
                //	time we drew a line, take that into account when calculating
                //	the offset. leftOverDistance is always < spacing.
                offsetX += stepX * (spacing - leftOverDistance);
                offsetY += stepY * (spacing - leftOverDistance);

                leftOverDistance -= spacing;
            } else {
                // The normal case. The offset increment is the normalized vector
                //	times the spacing
                offsetX += stepX * spacing;
                offsetY += stepY * spacing;
            }
            Point2D.Double stampAt = new Point2D.Double(startPoint.x + offsetX, startPoint.y + offsetY);
            stampMask(mask, g2, stampAt);
            // Remove the distance we just covered
            totalDistance -= spacing;
        }

        // Return the distance that we didn't get to cover when drawing the line.
        //	It is going to be less than spacing.
        return totalDistance;
    }

    private void stampMask(BufferedImage mask, Graphics2D g2, Point2D.Double stampAt) {
        g2.translate(stampAt.x, stampAt.y);
        g2.translate(-mask.getWidth()/2, -mask.getHeight()/2);
        g2.drawImage(mask, 0, 0, null);
        g2.translate(mask.getWidth()/2, mask.getHeight()/2);
        g2.translate(-stampAt.x, -stampAt.y);
    }

    public BufferedImage createBrush(double radius, FlatColor color, double softness, boolean hard) {
        BufferedImage img = new BufferedImage((int)(radius*2+1), (int)(radius*2+1), BufferedImage.TYPE_INT_ARGB);
        u.p("created image of size: " + img.getWidth() + " " + img.getHeight());
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if(!hard) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        }
        g2.setColor(GraphicsUtil.toAWT(color));


        // The way we achieve "softness" on the edges of the brush is to draw
        //	the shape full size with some transparency, then keep drawing the shape
        //	at smaller sizes with the same transparency level. Thus, the center
        //	builds up and is darker, while edges remain partially transparent.

        // First, based on the softness setting, determine the radius of the fully
        //	opaque pixels.
        int innerRadius = (int)Math.ceil(softness * (0.5f - radius) + radius);
        int outerRadius = (int)Math.ceil(radius);

        // The alpha level is always proportial to the difference between the inner, opaque
        //	radius and the outer, transparent radius.
        float alphaStep = 1.0f / (outerRadius - innerRadius + 1);

        // Since we're drawing shape on top of shape, we only need to set the alpha once
        //CGContextSetAlpha(bitmapContext, alphaStep);
        //josh: i'm not sure about this. should we be multiplying?
        for (int i = outerRadius; i >= innerRadius; --i) {
            Graphics2D g3 = (Graphics2D) g2.create();
            g3.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaStep));
            // First, center the shape onto the context.
            //CGContextTranslateCTM(bitmapContext, outerRadius - i, outerRadius - i);
            g3.translate(outerRadius -i, outerRadius -i);

            // Second, scale the the brush shape, such that each successive iteration
            //	is two pixels smaller in width and height than the previous iteration.
            float scale = (2.0f * (float)i) / (2.0f * (float)outerRadius);
            //CGContextScaleCTM(bitmapContext, scale, scale);
            g3.scale(scale, scale);
            // Finally, actually add the path and fill it
            //CGContextAddPath(bitmapContext, mShape);
            //CGContextEOFillPath(bitmapContext);
            g3.fillOval(0, 0, (int)(radius*2), (int)(radius*2));
            g3.dispose();
        }
        //CGContextSaveGState(bitmapContext);
        g2.dispose();
        return img;
    }

    public void setBrush(BufferedImage brush) {
        this.brush = brush;
    }
}
