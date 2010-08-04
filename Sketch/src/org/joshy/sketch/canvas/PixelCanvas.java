package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.sketch.model.PixelDocument;
import org.joshy.sketch.modes.pixel.PixelDocContext;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 8, 2010
 * Time: 5:42:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class PixelCanvas extends DocumentCanvas {
    private PixelDocument document;
    private PixelDocContext context;

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
    public void doSkins() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    //draw the background
    @Override
    public void draw(GFX g) {
        g.setPaint(FlatColor.WHITE);
        g.fillRect(0,0,getWidth(),getHeight());
        g.translate(panX,panY);
        g.scale(scale,scale);
        draw(g, document);
        g.scale(1/scale,1/scale);
        g.translate(-panX,-panY);
    }
    //draw the document
    private void draw(GFX g, PixelDocument pixelDocument) {
        //draw the actual image
        g.drawImage(pixelDocument.getBitmap(),0,0);
        //draw the doc borders
        g.setPaint(FlatColor.BLACK);
        g.drawRect(0,0,pixelDocument.getWidth(),pixelDocument.getHeight());
        if(context.selectedTool != null) {
            context.selectedTool.drawOverlay(g);
        }
    }

    public void setDocument(PixelDocument doc) {
        this.document = doc;
    }

    public PixelDocument getDocument() {
        return document;
    }

}
