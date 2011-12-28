package org.joshy.sketch.actions.io;

import org.joshy.gfx.draw.*;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Checkbox;
import org.joshy.gfx.node.layout.GridBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.stage.swing.SwingGFX;
import org.joshy.gfx.util.GraphicsUtil;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.tools.DrawPathTool;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SavePNGAction extends BaseExportAction {
    public boolean includeBackground = true;
    public boolean includeDocumentBounds = false;
    public boolean includeStamp = false;

    public SavePNGAction(DocContext context) {
        super(context);
    }

    @Override
    protected String getStandardFileExtension() {
        return "png";
    }


    public void execute() {
        final Stage stage = Stage.createStage();
        GridBox grid = new GridBox()
                .createColumn(20, GridBox.Align.Right)
                .createColumn(100, GridBox.Align.Left)
                ;
        final Checkbox backgroundCheckbox = new Checkbox("include background");
        grid.addControl(backgroundCheckbox);
        backgroundCheckbox.setSelected(includeBackground);
        grid.nextRow();
        final Checkbox documentBounds = new Checkbox("full document bounds");
        documentBounds.setSelected(includeDocumentBounds);
        grid.addControl(documentBounds);
        grid.nextRow();
        Button cancelButton = new Button("cancel");
        cancelButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                stage.hide();
            }
        });
        grid.addControl(cancelButton);
        Button continueButton = new Button("continue");
        continueButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                stage.hide();
                includeBackground = backgroundCheckbox.isSelected();
                includeDocumentBounds = documentBounds.isSelected();
                SavePNGAction.super.execute();
            }
        });
        grid.addControl(continueButton);
        stage.setContent(grid);
    }


    public static void exportStatic(File file, CanvasDocument doc) {
        if(doc instanceof SketchDocument) {
            SavePNGAction save = new SavePNGAction(null);
            save.includeBackground = false;
            save.includeDocumentBounds = false;
            save.export(file, (SketchDocument) doc);
        }
        if(doc instanceof PixelDocument) {
            SavePNGAction save = new SavePNGAction(null);
            save.export(file, (PixelDocument) doc);
        }
    }

    public static void exportFragment(File file, Iterable<SNode> nodes) {
        Bounds bounds = calculateBounds(nodes);
        BufferedImage img = new BufferedImage((int)bounds.getWidth()+1,(int)bounds.getHeight()+1,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.translate(-bounds.getX(),-bounds.getY());
        ExportProcessor.processFragment(new PNGExporter(), g2, nodes);
        g2.dispose();
        try {
            ImageIO.write(img,"png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void export(File file, PixelDocument doc) {
        BufferedImage img = GraphicsUtil.toAWT(doc.getBitmap());
        try {
            ImageIO.write(img,"png",file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void export(File file, SketchDocument doc) {
        Bounds bounds = null;
        if(includeDocumentBounds) {
            bounds = new Bounds(0,0,doc.getWidth(),doc.getHeight());
        } else {
            bounds = calculateBounds(doc.getCurrentPage().getModel());
        }
        BufferedImage img = new BufferedImage((int)bounds.getWidth()+1,(int)bounds.getHeight()+1,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.translate(-bounds.getX(),-bounds.getY());
        PNGExporter exporter = new PNGExporter();
        exporter.setIncludeDocumentBackground(includeBackground);
        ExportProcessor.process(exporter, g2, doc);
        if(includeStamp) {
            g2.translate(bounds.getX(),bounds.getY());
            g2.setPaint(Color.BLACK);
            Font font = org.joshy.gfx.draw.Font.DEFAULT.getAWTFont();
            g2.setFont(font);
            String stamp = "handcrafted with LeonardoSketch.org";
            Rectangle2D sb = font.getStringBounds(stamp, g2.getFontRenderContext());
            g2.drawString(stamp,
                    (int)(img.getWidth()-sb.getWidth()-10),
                    (int)(img.getHeight()-10)
            );
        }
        g2.dispose();
        try {
            ImageIO.write(img,"png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bounds calculateBounds(Iterable<SNode> model) {
        double x = Double.MAX_VALUE;
        double y = Double.MAX_VALUE;
        double w = Double.MIN_VALUE;
        double h = Double.MIN_VALUE;
        for(SNode n : model) {
            Bounds b = n.getTransformedBounds();
            /*
            if(n instanceof SShape) {
                b = ((SShape) n).getEffectBounds();
            }
            */
            x = Math.min(x, b.getX());
            y = Math.min(y, b.getY());
            w = Math.max(w, b.getX() + b.getWidth());
            h = Math.max(h, b.getY() + b.getHeight());
        }
        return new Bounds(x,y,w-x,h-y);
    }

    public static class PNGExporter implements ShapeExporter<Graphics2D> {
        private boolean useDocBg = true;

        public void docStart(Graphics2D g2, SketchDocument doc) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            SwingGFX gfx = new SwingGFX(g2);
            Paint fill = doc.getBackgroundFill();
            if(fill != null && useDocBg) {
                gfx.setPaint(fill);
                gfx.fillRect(0,0,(int)doc.getWidth(), (int) doc.getHeight());
            }
        }

        public void pageStart(Graphics2D out, SketchDocument.SketchPage page) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void exportPre(Graphics2D g2, SNode shape) {
            g2.translate(shape.getTranslateX(),shape.getTranslateY());
            g2.translate(shape.getAnchorX(),shape.getAnchorY());
            g2.rotate(Math.toRadians(shape.getRotate()));
            g2.scale(shape.getScaleX(), shape.getScaleY());
            g2.translate(-shape.getAnchorX(),-shape.getAnchorY());
            if(shape instanceof SelfDrawable) {
                SwingGFX gfx = new SwingGFX(g2);
                ((SelfDrawable)shape).draw(gfx);
                return;
            }
        }

        public void exportPost(Graphics2D g2, SNode shape) {
            g2.translate(shape.getAnchorX(),shape.getAnchorY());
            g2.scale(1.0/shape.getScaleX(), 1.0/shape.getScaleY());
            g2.rotate(Math.toRadians(-shape.getRotate()));
            g2.translate(-shape.getAnchorX(),-shape.getAnchorY());
            g2.translate(-shape.getTranslateX(),-shape.getTranslateY());
        }

        public void pageEnd(Graphics2D out, SketchDocument.SketchPage page) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void docEnd(Graphics2D out, SketchDocument document) {
        }

        public boolean isContainer(SNode n) {
            if(n instanceof SGroup) return true;
            return false;
        }

        public Iterable<? extends SNode> getChildNodes(SNode n) {
            if(n instanceof SGroup) return ((SGroup)n).getNodes();
            if(n instanceof ResizableGrid9Shape) return ((ResizableGrid9Shape)n).getNodes();
            return null;
        }

        public void setIncludeDocumentBackground(boolean useDocBg) {
            this.useDocBg = useDocBg;
        }
    }
}
