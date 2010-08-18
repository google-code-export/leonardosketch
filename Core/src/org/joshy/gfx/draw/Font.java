package org.joshy.gfx.draw;

import org.joshy.gfx.util.u;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Font {
    private Weight weight = Weight.Regular;
    private Style style;
    public static final Font DEFAULT = Font.name("Arial").size(12).resolve();

    public static FontBuilder fromURL(URL url) {
        return new FontBuilder(url);
    }

    public enum Weight {
        Regular, Bold
    }

    public enum Style {
        Regular, Italic, Oblique
    }
    

    protected String name;
    protected float size;
    private BufferedImage img = new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB);
    private Graphics2D graphics;
    protected java.awt.Font fnt;
    protected boolean vector = false;
    private File file;

    protected Font(String name, float size, boolean vector, Weight weight, Style style, File file, URL url) {
        this.name = name;
        this.size = size;
        this.file = file;
        this.vector = vector;
        this.weight = weight;
        this.style = style;
        graphics = img.createGraphics();
        int w = java.awt.Font.PLAIN;
        if(weight == Weight.Bold) {
            w = java.awt.Font.BOLD;
        }
        int s = java.awt.Font.PLAIN;
        if(style == Style.Italic) {
            s = java.awt.Font.ITALIC;
        }
        if(file != null) {
            try {
                fnt = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, file);
                fnt = fnt.deriveFont((float)size);
                fnt = fnt.deriveFont(w|s);
                u.p("final font = " + fnt);
            } catch (FontFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (url != null) {
            try {
                fnt = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, url.openStream());
                fnt = fnt.deriveFont((float)size);
                fnt = fnt.deriveFont(w|s);
                u.p("final font = " + fnt);
            } catch (FontFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(FontBuilder.customRootFontCache.containsKey(name)) {
            fnt = FontBuilder.customRootFontCache.get(name);
            fnt = fnt.deriveFont((float)size);
            fnt = fnt.deriveFont(w|s);
        } else {
            fnt = new java.awt.Font(getName(),w|s, (int)size);
        }
//        String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
//        for(String na : names) {
//            u.p("jogltext.font name = " + na);
//        }
    }
    protected Font(String name, float size, File file) {
        this(name,size,false,Weight.Regular, Style.Regular, file, null);
    }

    public static FontBuilder name(String s) {
        return new FontBuilder(s);
    }
    public static FontBuilder fromFile(File file) {
        return new FontBuilder(file);        
    }

    public String getName() {
        return name;
    }

    public float getSize() {
        return size;
    }

    public java.awt.Font getAWTFont() {
        return this.fnt;
    }

    public double calculateWidth(String text) {
        FontRenderContext frc = graphics.getFontRenderContext();
        return fnt.getStringBounds(text,frc).getWidth();
    }

    public double calculateHeight(String text) {
        FontRenderContext frc = graphics.getFontRenderContext();
        return fnt.getStringBounds(text,frc).getHeight();
    }

    public double getWidth(String string) {
        FontMetrics metrics = graphics.getFontMetrics(this.fnt);
        return metrics.stringWidth(string);
    }

    public Weight getWeight() {
        return weight;
    }


    public double getAscender() {
        FontMetrics metrics = graphics.getFontMetrics(this.fnt);
        return metrics.getAscent();
    }

    public double getDescender() {
        FontMetrics metrics = graphics.getFontMetrics(this.fnt);
        return metrics.getDescent();
    }

    public static void drawCentered(GFX g, String string, Font font, double xoff, double yoff, double width, double height, boolean includeDescender) {
        double tw = font.getWidth(string);
        double th = font.getAscender();
        if(includeDescender) {
            th += font.getDescender();
        }
        g.drawText(string,font,(int)(xoff + (width -tw)/2), yoff + (height -th)/2 + font.getAscender());
    }

    public static void drawCenteredVertically(GFX g, String string, Font font, double xoff, double yoff, double width, double height, boolean includeDescender) {
        if(string == null) return;
        if(string.length() == 0) return;
        double tw = font.getWidth(string);
        double th = font.getAscender();
        if(includeDescender) {
            th += font.getDescender();
        }
        g.drawText(string,font,xoff, yoff + (height -th)/2 + font.getAscender());
    }

    public static void drawLines(GFX g, Font font, String ... strings) {
        //g.drawLine(0,0,300,0);
        double y = font.getAscender();
        for(String s : strings) {
            g.drawText(s,font,0,y);
            //g.drawLine(0,y,300,y);
            y+= font.getAscender() + font.getDescender() + font.getLeading();
        }
    }


    public double getLeading() {
        FontMetrics metrics = graphics.getFontMetrics(this.fnt);
        return metrics.getLeading();
    }

}
