package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.event.BackgroundTask;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.util.u;
import org.joshy.sketch.modes.DocContext;

import javax.imageio.ImageIO;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jun 9, 2010
 * Time: 3:55:46 PM
 * To change this template use File | Settings | File Templates.
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

    public SImage(File file) throws IOException {
        super();
        this.file = file;
        this.img = ImageIO.read(file);
        image = Image.create(this.img);
        init();
    }

    public SImage(URI remoteURI, boolean backgroundLoading, Image thumb, final DocContext ctx) throws IOException, InterruptedException {
        this.file = null;
        this.thumb = thumb;
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
        u.p("using uri: " + baseURI);
        u.p("filename = " + fileName);
        URI fileURI = baseURI.resolve(fileName);
        u.p("loading image from local file: " + fileURI);
        this.file = new File(fileURI);
        img = ImageIO.read(fileURI.toURL());
        image = Image.create(this.img);
        init();
    }

    public SImage(URL url) throws IOException {
        u.p("using URL : " + url);
        this.file = null;
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
        }
        return super.duplicate(dupe);
    }

    public String getRelativeURL() {
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

    public double getPreferredAspectRatio() {
        if(image == null) return 1;
        double ratio = ((double)image.getHeight())/((double)image.getWidth());
        return ratio; 
    }

    public boolean constrainByDefault() {
        return true;
    }
}
