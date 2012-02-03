package org.joshy.sketch.model;

import org.joshy.gfx.draw.*;

import java.awt.geom.Area;

public class SText extends AbstractResizeableNode implements SelfDrawable {

    public enum HAlign { Left, Center, Right };

    private String text;
    private double fontSize;
    private Font.Weight weight = Font.Weight.Regular;
    private Font.Style style;
    private String fontName = Font.DEFAULT.getName();
    private boolean autoSize = true;
    private HAlign halign = HAlign.Left;
    private boolean bulleted = false;
    private boolean wrapText = false;

    public SText(double x, double y, double w, double h) {
        super(x, y, w, h);
        this.text = "";
        this.fontSize = 12.0;
    }
    public SText() {
        this(0,0,100,100);
    }

    public HAlign getHalign() {
        return halign;
    }

    public void setHalign(HAlign halign) {
        this.halign = halign;
        markContentChanged();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        markContentChanged();
        updateSize();
    }

    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
        updateSize();
        markContentChanged();
    }

    public double getFontSize() {
        return fontSize;
    }

    public Font.Weight getWeight() {
        return weight;
    }

    public void setWeight(Font.Weight weight) {
        this.weight = weight;
        markContentChanged();
        updateSize();
    }

    public void setStyle(Font.Style style) {
        this.style = style;
        markContentChanged();
        updateSize();
    }
    
    public Font.Style getStyle() {
        return style;
    }

    public void refresh() {
        if(isAutoSize()){
            updateSize();
        }
    }

    private void updateSize() {
        if(!isAutoSize()) return;
        Font font = Font.name(getFontName())
                .weight(getWeight())
                .style(getStyle())
                .size((float)fontSize)
                .resolve();
        String[] strings = getText().split("\n");
        double maxWidth = 0;
        double h = 0;
        for(String s : strings) {
            maxWidth = Math.max(maxWidth, font.calculateWidth(s));
            h += font.getAscender();
            h += font.getDescender();
        }
        setWidth(maxWidth);
        setHeight(h);
    }

    public double getAscent() {
        Font font = Font.name(getFontName())
                .weight(getWeight())
                .style(getStyle())
                .size((float)fontSize)
                .resolve();
        return font.getAscender();
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
        ((SText)dupe).setStyle(this.getStyle());
        ((SText)dupe).setFontSize(this.getFontSize());
        ((SText)dupe).setFontName(this.getFontName());
        ((SText)dupe).setAutoSize(this.isAutoSize());
        ((SText)dupe).setHalign(this.getHalign());
        return super.duplicate(dupe);
    }

    @Override
    public Area toArea() {
        return new Area();
    }

    @Override
    public SPath toPath() {
        SPath path = new SPath();
        path.moveTo(this.getX(),this.getY());
        path.lineTo(this.getX()+this.getWidth(),this.getY());
        path.lineTo(this.getX()+this.getWidth(),this.getY()+this.getHeight());
        path.lineTo(this.getX(),this.getY()+this.getHeight());
        path.setTranslateX(this.getTranslateX());
        path.setTranslateY(this.getTranslateY());
        path.setFillPaint(this.getFillPaint());
        path.setFillOpacity(this.getFillOpacity());
        path.setStrokeWidth(this.getStrokeWidth());
        path.setStrokePaint(this.getStrokePaint());
        return path;
    }

    public void draw(GFX g) {

        double opacity = -1;
        Paint paint = this.getFillPaint();
        if(paint != null) {
            if(paint instanceof FlatColor) {
                g.setPaint(((FlatColor)paint).deriveWithAlpha(getFillOpacity()));
            }
            if(paint instanceof MultiGradientFill) {
                MultiGradientFill gf = (MultiGradientFill) paint;
                gf = gf.translate(getX(),getY());
                g.setPaint(gf);
            }
            if(paint instanceof PatternPaint) {
                opacity = g.getOpacity();
                g.setOpacity(getFillOpacity());
                g.setPaint(paint);
            }
        }
        drawShadow(g);
        fillShape(g);
        if(opacity >=0) g.setOpacity(opacity);
    }

    protected void fillShape(GFX g) {
        Font font = Font.name(getFontName())
                .size((float)this.getFontSize())
                .weight(this.getWeight())
                .style(this.getStyle())
                .resolve();
        double x = 0;
        double y = 0;

        if(isWrapText()) {
            double maxWidth = getWidth();
            String[] words = getText().split(" ");
            double wordSpacing = 10;
            y += font.getAscender();
            for(String word : words) {
                double w = font.calculateWidth(word);
                if(x + w > maxWidth) {
                    x = 0;
                    y += (font.getAscender() + font.getDescender());
                }
                drawText(g,word,font, this.getX()+x,this.getY()+y);
                x += (w + wordSpacing);
            }
            
        } else {
            //draw unwrapped text
            String[] strings = getText().split("\n");
            y += font.getAscender();
            double fw = getWidth();
            for(String s : strings) {
                double w = font.calculateWidth(s);
                switch(this.getHalign()) {
                    case Left: x = 0; break;
                    case Center: x = (fw-w)/2; break;
                    case Right: x = fw-w; break;
                }

                drawText(g, s, font, this.getX() + x, this.getY() + y);
                y += (font.getAscender() + font.getDescender());
            }
        }
    }

    protected void drawText(GFX g, String s, Font font, double x, double y) {
        g.drawText(s,font, x,y);
        if(bulleted) {
            g.fillOval(x-20,y-15,10,10);
        }
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
        markContentChanged();
        updateSize();
    }

    public boolean isAutoSize() {
        return autoSize;
    }

    public void setAutoSize(boolean autoSize) {
        this.autoSize = autoSize;
        markContentChanged();
    }

    public void setBulleted(boolean bulleted) {
        this.bulleted = bulleted;
        markContentChanged();
    }

    public boolean isBulleted() {
        return bulleted;
    }

    public boolean isWrapText() {
        return wrapText;
    }

    public void setWrapText(boolean wrapText) {
        this.wrapText = wrapText;
        markContentChanged();
    }

}
