package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;

import java.awt.geom.Area;

public class SText extends AbstractResizeableNode implements SelfDrawable {
    public String text;
    private double fontSize;
    private Font.Weight weight = Font.Weight.Regular;
    private Font.Style style;
    private int alignment = 0;
    private String fontName = Font.DEFAULT.getName();


    public SText(double x, double y, double w, double h) {
        super(x, y, w, h);
        this.text = "";
        this.fontSize = 12.0;
    }
    public SText() {
        this(0,0,100,100);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        updateSize();
    }

    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
        updateSize();
    }

    public double getFontSize() {
        return fontSize;
    }

    public Font.Weight getWeight() {
        return weight;
    }

    public void setWeight(Font.Weight weight) {
        this.weight = weight;
        updateSize();
    }

    public void setStyle(Font.Style style) {
        this.style = style;
        updateSize();
    }
    
    public Font.Style getStyle() {
        return style;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public int getAlignment() {
        return alignment;
    }

    private void updateSize() {
        Font font = Font.name("Arial")
                .weight(getWeight())
                .style(getStyle())
                .size((float)fontSize)
                .resolve();
        setWidth(font.calculateWidth(text));
        setHeight(font.calculateHeight(text));
    }

    @Override
    public String toString() {
        return "SText{" +
                "text='" + text + '\'' +
                ", fontSize=" + fontSize +
                ", weight=" + weight +
                ", style=" + style +
                '}';
    }

    @Override
    public SNode duplicate(SNode dupe) {
        if(dupe == null) {
            dupe = new SText();
        }
        ((SText)dupe).setText(this.getText());
        ((SText)dupe).setWeight(this.getWeight());
        ((SText)dupe).setFontSize(this.getFontSize());
        return super.duplicate(dupe);
    }

    @Override
    public Area toArea() {
        return new Area();
    }

    public void draw(GFX g) {
        g.setPaint(this.getFillPaint());
        if(getFillPaint() instanceof FlatColor) {
            g.setPaint(((FlatColor)getFillPaint()).deriveWithAlpha(getFillOpacity()));
        }
        Font font = Font.name(getFontName())
                .size((float)this.getFontSize())
                .weight(this.getWeight())
                .style(this.getStyle())
                .resolve();
        double w = font.calculateWidth(getText());
        double h = font.calculateHeight(getText());
        if(alignment == 0) {
            g.drawText(getText(), font,
                    this.getX() + getWidth()/2-w/2,
                    this.getY() + font.getAscender() + getHeight()/2 - h/2);
//            g.drawRect(getX(),getY(),getWidth(),getHeight());
        } else {
            g.drawText(this.text, font, this.getX(), this.getY() + font.getAscender());
        }
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }
}
