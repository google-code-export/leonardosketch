package org.joshy.sketch.pixel.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.util.u;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/17/11
 * Time: 5:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class PixelGraphics {
    private FlatColor fill;
    private PixelLayer target;

    public PixelGraphics(PixelLayer pixelLayer) {
        target = pixelLayer;
    }

    public void setFill(FlatColor purple) {
        this.fill = purple;
    }

    public void fillRect(final int x, final int y, final int w, final int h) {
        u.p("filling a rect at " + x + " " + y);
        int tx = x/256;
        int ty = y/256;
        int tx2 = (x+w)/256;
        int ty2 = (y+h)/256;
        u.p("tx2 = " + tx2);

        fillShape(tx,ty,tx2,ty2, new ShapeFillCallback(){
            public void fill(Graphics2D g2) {
                g2.setPaint(new java.awt.Color(fill.getRGBA(),true));
                g2.fillRect(x,y,w,h);
            }
        });
    }

    public void fillOval(final int x, final int y, final int w, final int h) {
        u.p("filling an oval at " + x + " " + y);
        fillShape(x/256,y/256,(x+w)/256, (y+h)/256,
                new ShapeFillCallback() { public void fill(Graphics2D g2) {
                    g2.setPaint(new java.awt.Color(fill.getRGBA(),true));
                    g2.fillOval(x,y,w,h);
                }}
        );

    }

    private void fillShape(int tx, int ty, int tx2, int ty2, ShapeFillCallback callback) {
        u.p("filling a shape in tile: " + tx + " " + ty + " -> " + tx2 + " " + ty2);
        for(int i=tx; i<=tx2; i++) {
            for(int j=ty; j<=ty2; j++) {
                PixelTile tile = target.getTile(i,j);
                if(tile == null) {
                    u.p("creating a tile at: " + (i) + " " + (j));
                    tile = target.createTile(i,j);
                }

                BufferedImage b = tile.getBuffer();
                Graphics2D g2 = b.createGraphics();
                g2.translate(-i*256,-j*256);
                callback.fill(g2);
                g2.translate(i*256,j*256);
                g2.dispose();
            }
        }
    }

    public void fillSelection(PixelSelection selection) {
        //draw over the selection
        PixelTile tile = target.getTile(0,0);
        if(tile == null) {
            tile = target.createTile(0,0);
        }

        BufferedImage scratch = new BufferedImage(selection.buffer.getWidth(),selection.buffer.getHeight(),BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scratch.createGraphics();
        //draw the selection mask first
        g2.drawImage(selection.buffer,0,0,null);

        //now fill  using SrcAtop
        //g2.setPaint(java.awt.Color.RED);
        g2.setPaint(new java.awt.Color(fill.getRGBA(),true));
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.fillRect(0,0,selection.buffer.getWidth(),selection.buffer.getHeight());
        g2.dispose();

        //now composite onto the existing layer
        BufferedImage buff = tile.getBuffer();
        Graphics2D gx = buff.createGraphics();
        gx.drawImage(scratch,0,0,null);
        gx.dispose();
    }

    public void fillDisplacementClouds(int x, int y, int w, int h) {
        BufferedImage scratch = new BufferedImage(257,257, BufferedImage.TYPE_INT_RGB);


        int c1, c2, c3, c4;
        c1 = randColor();
        c2 = randColor();
        c3 = randColor();
        c4 = randColor();
        scratch.setRGB(0,0,c1);
        scratch.setRGB(256,0,c2);
        scratch.setRGB(0,256,c3);
        scratch.setRGB(256,256,c4);
        int val = (c1+c2+c3+c4)/4;
        scratch.setRGB(128,128,val);

        divide(scratch,0,0,257,257,c1,c2,c3,c4);



        PixelTile tile = target.getTile(0,0);
        if(tile == null) {
            tile = target.createTile(0,0);
        }
        Graphics2D g2 = tile.buffer.createGraphics();
        g2.drawImage(scratch,
                x,y,x+w,y+h,
                0,0,w,h
                ,null);
        g2.dispose();

    }

    private void divide(BufferedImage scratch, int x, int y, int w, int h, int c1, int c2, int c3, int c4) {
        int nw = w/2;
        int nh = h/2;
        if(w > 1 || h > 1) {
            int middle = (c1+c2+c3+c4)/4;
            double dis =  displace(nw+nh);
            //u.p("dis = " + dis);
            middle += (int)(dis*256);
            middle = clamp(middle,0,255);
            int edge1 = (c1+c2)/2;
            int edge2 = (c2+c3)/2;
            int edge3 = (c3+c4)/2;
            int edge4 = (c4+c1)/2;
            divide(scratch,x,y,nw,nh, c1,edge1,middle,edge4);
            divide(scratch,x+nw,y,w-nw,nh, edge1,c2,edge2,middle);
            divide(scratch,x+nw,y+nh,w-nw,h-nh, middle, edge2, c3, edge3);
            divide(scratch,x,y+nh,nw,h-nh, edge4, middle, edge3, c4);
        } else {
            int middle = (c1+c2+c3+c4)/4;
            boolean monochromatic = true;
            if(monochromatic) {
                middle = middle << 16 | middle << 8 | middle;
            }
            scratch.setRGB(x,y,middle);
        }
    }

    private int clamp(int middle, int min, int max) {
        if(middle < min) return min;
        if(middle > max) return max;
        return middle;
    }

    private double displace(int i) {
        double max = ((double)i) / (257+257)*3;
        return ((Math.random()-0.5)*max);
    }

    private int randColor() {
        return (int) (Math.random()*255);
    }

    private abstract class ShapeFillCallback {
        public abstract void fill(Graphics2D g2);
    }
}
