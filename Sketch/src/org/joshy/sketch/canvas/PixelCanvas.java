package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.draw.PatternPaint;
import org.joshy.gfx.node.Bounds;
import org.joshy.sketch.modes.pixel.PixelDocContext;
import org.joshy.sketch.pixel.model.PixelDoc;
import org.joshy.sketch.pixel.model.PixelLayer;
import org.joshy.sketch.pixel.model.PixelSelection;
import org.joshy.sketch.pixel.model.PixelTile;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 8, 2010
 * Time: 5:42:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class PixelCanvas extends DocumentCanvas {
    private PixelDoc document;
    private PixelDocContext context;
    private PixelSelection selection;

    public PixelCanvas(PixelDocContext context) {
        this.context = context;
    }
    
    public boolean isFocused() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doLayout() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doPrefLayout() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doSkins() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Bounds getVisualBounds() {
        return new Bounds(
                getTranslateX()
                ,getTranslateY()
                ,1000*getScale()
                ,1000*getScale()
        );
    }

    @Override
    public Bounds getInputBounds() {
        return new Bounds(
                getTranslateX()
                ,getTranslateY()
                ,1000*getScale()
                ,1000*getScale()
        );
    }

    /*
    //draw the background
    @Override
    public void draw(GFX g) {
        g.setPaint(FlatColor.WHITE);
        g.fillRect(0,0,getWidth(),getHeight());
        g.scale(scale,scale);
        draw(g, document);
        g.scale(1/scale,1/scale);
    }
    //draw the document
    private void draw(GFX g, PixelDoc pixelDocument) {
        //draw the actual image
        g.drawImage(pixelDocument.getBitmap(),0,0);
        //draw the doc borders
        g.setPaint(FlatColor.BLACK);
        g.drawRect(0,0,pixelDocument.getWidth(),pixelDocument.getHeight());
        if(context.selectedTool != null) {
            context.selectedTool.drawOverlay(g);
        }
    }
    */
    public void draw(GFX gfx) {
        gfx.push();
        gfx.scale(getScale(),getScale());
        drawLayers(gfx);
        drawSelection(gfx);
        drawOverlays(gfx);
        gfx.pop();
        if(document.isRepeat()) {
            double size = document.getRepeatSize() * getScale();
            gfx.setPaint(FlatColor.RED);
            for(int i = 0; i<3; i++) {
                for(int j=0; j<3; j++) {
                    gfx.drawRect(i*size,j*size,size,size);
                }
            }
        }
    }

    private void drawSelection(GFX gfx) {
        if(!selection.isEmpty()) {
            drawLayer(gfx, selection.layer);
        }
    }

    private void drawOverlays(GFX gfx) {
        if(context.getSelectedTool() != null) {
            context.getSelectedTool().drawOverlay(gfx);
        }
    }

    public void drawLayers(GFX gfx) {
        for(PixelLayer layer : document.getLayers()) {
            drawLayer(gfx, layer);
        }
    }

    private void drawLayer(GFX gfx, PixelLayer layer) {
        if(document.isRepeat()) {
            gfx.setPaint(FlatColor.RED);
            int size = document.getRepeatSize();
            PixelTile tile = layer.getTile(0, 0);
            if(tile != null) {
                try {
                    PatternPaint pat = PatternPaint.create(tile.getBuffer().getSubimage(0,0,size,size),null);
                    gfx.setPaint(pat);
                    gfx.fillRect(0, 0, 600, 600);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for(int y=0; y<600; y+=256) {
            for(int x=0; x<600; x+=256) {
                PixelTile tile = layer.getTile(x/256,y/256);
                if(tile != null) {
                    Image image = tile.getImage();
                    if(image != null) {
                        gfx.drawImage(image,x,y);
                    }
                }
                //tile grid lines
                //gfx.setPaint(FlatColor.GRAY);
                //gfx.drawRect(x,y,256,256);
            }
        }
    }

    public void setDocument(PixelDoc doc) {
        this.document = doc;
        selection = new PixelSelection(this.document);
    }

    public PixelDoc getDocument() {
        return document;
    }

    public PixelSelection getSelection() {
        return selection;
    }
}
