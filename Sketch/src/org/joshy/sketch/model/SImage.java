package org.joshy.sketch.model;

import java.awt.geom.Area;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.event.BackgroundTask;
import org.joshy.gfx.node.Bounds;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.util.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * A sketch node representing an image. It is resizable.
 * It can be created from a file, a buffered image, or a url, or an input stream.
 * This lets us load images directly out of zip files, for example.
 */
public class SImage extends SNode implements SelfDrawable, SResizeableNode {
    private File file;
    private BufferedImage img;
    private Image image;
    private double width;
    private double height;
    private double y;
    private double x;
    private BackgroundTask<URI, BufferedImage> bgload;
    private Image thumb;
    private String relativeURL = null;
    private FlatColor strokePaint = FlatColor.BLACK;
    private double strokeWidth = 0.0;

    public SImage(File file) throws IOException {
        super();
        this.file = file;
        this.img = ImageIO.read(file);
        image = Image.create(this.img);
        init();
    }

    public SImage(URI remoteURI, String relativeURL, boolean backgroundLoading, Image thumb, final DocContext ctx) throws IOException, InterruptedException {
        this.file = null;
        this.thumb = thumb;
        this.relativeURL = relativeURL;
        if(backgroundLoading) {
            this.width = 100;
            this.height = 100;
            if(thumb != null) {
                this.width = thumb.getWidth();
                this.height = thumb.getHeight();
            }
            bgload = new BackgroundTask<URI,BufferedImage>() {
                @Override
                protected BufferedImage onWork(URI data) {
                    try {
                        return ImageIO.read(data.toURL());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onEnd(BufferedImage result) {
                    img = result;
                    image = Image.create(result);
                    width = img.getWidth();
                    height = img.getHeight();
                    SImage.this.thumb = null;
                    ctx.redraw();
                }
            };
            bgload.setData(remoteURI);
            bgload.start();
        } else {
            img = ImageIO.read(remoteURI.toURL());
            image = Image.create(this.img);
            init();
        }
    }

    public SImage(URI baseURI, String fileName) throws IOException {
        URI fileURI = baseURI.resolve(fileName);
        this.file = new File(fileURI);
        img = ImageIO.read(fileURI.toURL());
        image = Image.create(this.img);
        init();
    }

    public SImage(BufferedImage img, String relativeURL) {
        this.img = img;
        image = Image.create(this.img);
        this.relativeURL = relativeURL;
        init();
    }

    public SImage(URL url, String relativeURLString) throws IOException {
        this.file = null;
        this.relativeURL = relativeURLString;
        img = ImageIO.read(url);
        image = Image.create(this.img);
        init();
    }

    private void init() {
        this.width = img.getWidth();
        this.height = img.getHeight();
    }

    @Override
    public Bounds getBounds() {
        if(image == null) {
            return new Bounds(getTranslateX()+getX(),getTranslateY()+getY(),getWidth(),getHeight());
        }
        double sx = width/((double)image.getWidth());
        return new Bounds(getTranslateX()+getX(),getTranslateY()+getY(),img.getWidth()*sx,img.getHeight()*sx);
    }

    @Override
    public boolean contains(Point2D point) {
        return getBounds().contains(point);
    }

    public void draw(GFX g) {
        g.translate(getX(),getY());
        if(image == null) {
            if(thumb != null) {
                g.drawImage(thumb,0,0);
            }
            g.setPaint(FlatColor.BLACK);
            g.drawRect(0,0,getWidth(),getHeight());            
        } else {
            double sx = width/((double)image.getWidth());
            double sy = height/((double)image.getHeight());
            g.scale(sx,sy);
            g.setSmoothImage(true);
            g.drawImage(image,0,0);
            g.setSmoothImage(false);
            g.scale(1/sx,1/sy);
            if(getStrokeWidth() > 0) {
                g.setStrokeWidth(getStrokeWidth());
                g.setPaint(getStrokePaint());
                g.drawRect(0,0,getWidth(),getHeight());
                g.setStrokeWidth(1);
            }
        }
        g.translate(-getX(),-getY());
    }

    @Override
    public SNode duplicate(SNode dupe) {
        if(dupe == null) {
            try {
                dupe = new SImage(this.file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ((SImage)dupe).setStrokePaint(this.getStrokePaint());
            ((SImage)dupe).setStrokeWidth(this.getStrokeWidth());
        }
        return super.duplicate(dupe);
    }

    public String getRelativeURL() {
        if(relativeURL != null) return relativeURL;
        return file.getName();
    }

    public BufferedImage getBufferedImage() {
        return this.img;
    }

    public File getFile() {
        return file;
    }

    public double getX() {
        return this.x;
    }

    public double getWidth() {
        return this.width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return this.height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public FlatColor getStrokePaint() {
        return strokePaint;
    }

    public void setStrokePaint(FlatColor strokePaint) {
        this.strokePaint = strokePaint;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public double getPreferredAspectRatio() {
        if(image == null) return 1;
        double ratio = ((double)image.getHeight())/((double)image.getWidth());
        return ratio; 
    }

    public boolean constrainByDefault() {
        return true;
    }

    public Constrain getConstrain() {
        return Constrain.None;
    }

    public Bounds getTransformedBounds() {
        java.awt.geom.Rectangle2D r = new Rectangle2D.Double(getX(),getY(),getWidth(),getHeight());
        AffineTransform af = new AffineTransform();
        af.translate(getTranslateX(),getTranslateY());
        af.translate(getAnchorX(),getAnchorY());
        af.rotate(Math.toRadians(getRotate()));
        af.scale(getScaleX(), getScaleY());
        af.translate(-getAnchorX(),-getAnchorY());
        Shape sh = af.createTransformedShape(r);
        Rectangle2D bds = sh.getBounds2D();
        return Util.toBounds(bds);
    }
}
