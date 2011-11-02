package org.joshy.sketch.tools;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.util.u;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.model.SImage;
import org.joshy.sketch.model.SketchDocument;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;


public class AddImageTool extends CanvasTool {

    public AddImageTool(VectorDocContext context) {
        super(context);
    }

    @Override
    public void call(KeyEvent event) {
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {
    }

    public void drawOverlay(GFX g) {
    }

    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        FileDialog fd = new FileDialog((Frame)context.getCanvas().getParent().getStage().getNativeWindow());
        fd.setMode(FileDialog.LOAD);
        fd.setTitle("Import Image");
        fd.setVisible(true);
        if(fd.getFile() != null) {
            File file = new File(fd.getDirectory(),fd.getFile());
            u.p("opening a file" + file);
            try {
                load(file,cursor);
                context.releaseControl();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
    }

    private void load(File file, Point2D cursor) throws IOException {
        SketchDocument doc = (SketchDocument) context.getDocument();
        SImage image = new SImage(file);
        image.setTranslateX(cursor.getX());
        image.setTranslateY(cursor.getY());
        doc.getCurrentPage().add(image);
        context.redraw();
    }
}
