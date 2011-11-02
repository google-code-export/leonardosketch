package org.joshy.sketch.actions.io;

import org.joshy.gfx.draw.FlatColor;
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
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class SavePNGAction extends BaseExportAction {
    private boolean includeBackground = true;
    private boolean includeDocumentBounds = false;

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


    /*
    private void showFileDialog() {
        FileDialog fd = new FileDialog((Frame)context.getStage().getNativeWindow());
        fd.setMode(FileDialog.SAVE);
        fd.setTitle("Export PNG Image");
        File currentFile = context.getDocument().getFile();
        if(currentFile != null) {
            fd.setFile(currentFile.getName().substring(0,currentFile.getName().lastIndexOf('.'))+".png");
        }
        fd.setVisible(true);
        if(fd.getFile() != null) {
            String fileName = fd.getFile();
            if(!fileName.toLowerCase().endsWith(".png")) {
                fileName = fileName + ".png";
            }
            File file = new File(fd.getDirectory(),fileName);
            if(context.getDocument() instanceof SketchDocument) {
                exportTo(file, (SketchDocument) context.getDocument(), includeDocumentBounds, includeBackground);
            }
            if(context.getDocument() instanceof PixelDocument) {
                exportTo(file, (PixelDocument) context.getDocument());
            }
            context.setLastExportAction(this);
            lastfile = file;
        }
    }
    */

    /*
    public void exportHeadless() {
        if(lastfile != null) {
            if(context.getDocument() instanceof SketchDocument) {
                exportTo(lastfile, (SketchDocument) context.getDocument(), includeDocumentBounds, includeBackground);
            }
            if(context.getDocument() instanceof PixelDocument) {
                exportTo(lastfile, (PixelDocument) context.getDocument());
            }
        }
    }
    */

    public static void export(File file, CanvasDocument doc) {
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

    public static void export(File file, List<SNode> nodes) {
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
    protected void export(File file, SketchDocument doc) {
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
        g2.dispose();
        try {
            ImageIO.write(img,"png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bounds calculateBounds(List<SNode> model) {
        double x = Double.MAX_VALUE;
        double y = Double.MAX_VALUE;
        double w = Double.MIN_VALUE;
        double h = Double.MIN_VALUE;
        for(SNode n : model) {
            Bounds b = n.getBounds();
            if(n instanceof SShape) {
                b = ((SShape) n).getEffectBounds();
            }
            x = Math.min(x, b.getX());
            y = Math.min(y, b.getY());
            w = Math.max(w, b.getX() + b.getWidth());
            h = Math.max(h, b.getY() + b.getHeight());
        }
        return new Bounds(x,y,w-x,h-y);
    }

    public static void export(Graphics2D g, SketchDocument doc) {
        ExportProcessor.process(new PNGExporter(), g, doc);
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

        private void draw(Graphics2D g, SText sText) {
            g.setPaint(GraphicsUtil.toAWT((FlatColor) sText.getFillPaint()));
            org.joshy.gfx.draw.Font font = org.joshy.gfx.draw.Font.DEFAULT;
            font = org.joshy.gfx.draw.Font.name(font.getName())
                    .size((float)sText.getFontSize())
                    .weight(sText.getWeight())
                    .style(sText.getStyle())
                    .resolve();
            g.setFont(font.getAWTFont());
            g.drawString(sText.getText(),(int)sText.getX(),(int)sText.getY()+(int)font.getAscender());
        }

        private void draw(Graphics2D g, SRect rect) {
            g.setPaint(GraphicsUtil.toAWT((FlatColor) rect.getFillPaint()));
            g.fillRect((int) rect.getX(),(int) rect.getY(),(int) rect.getWidth(),(int) rect.getHeight());

            g.setStroke(new BasicStroke((float) rect.getStrokeWidth()));
            g.setPaint(GraphicsUtil.toAWT((FlatColor) rect.getStrokePaint()));
            g.drawRect((int) rect.getX(),(int) rect.getY(),(int) rect.getWidth(),(int) rect.getHeight());
            g.setStroke(new BasicStroke(1));

        }

        private void draw(Graphics2D g, SOval oval) {
            g.setPaint(GraphicsUtil.toAWT((FlatColor) oval.getFillPaint()));
            g.fillOval((int) oval.getX(),(int) oval.getY(),(int) oval.getWidth(),(int) oval.getHeight());

            g.setStroke(new BasicStroke((float) oval.getStrokeWidth()));
            g.setPaint(GraphicsUtil.toAWT((FlatColor) oval.getStrokePaint()));
            g.drawOval((int) oval.getX(),(int) oval.getY(),(int) oval.getWidth(),(int) oval.getHeight());
            g.setStroke(new BasicStroke(1));
        }

        private void draw(Graphics2D g, SPoly poly) {
            Polygon p = new Polygon();
            for(int i=0; i<poly.pointCount(); i++) {
                p.addPoint((int)poly.getPoint(i).getX(),
                        (int)poly.getPoint(i).getY());
            }

            g.setPaint(GraphicsUtil.toAWT((FlatColor) poly.getFillPaint()));
            if(poly.isClosed()) {
                g.fillPolygon(p.xpoints,p.ypoints,p.npoints);
            } else {
                g.drawPolyline(p.xpoints,p.ypoints,p.npoints);
            }

            g.setPaint(GraphicsUtil.toAWT((FlatColor) poly.getStrokePaint()));
            g.setStroke(new BasicStroke((float) poly.getStrokeWidth()));
            if(poly.isClosed()) {
                g.drawPolygon(p.xpoints,p.ypoints,p.npoints);
            } else {
                g.drawPolyline(p.xpoints,p.ypoints,p.npoints);
            }
            g.setStroke(new BasicStroke(1));
        }

        private void draw(Graphics2D g, SPath path) {
            g.setPaint(GraphicsUtil.toAWT((FlatColor) path.getFillPaint()));
            if(path.isClosed()) {
                g.fill(DrawPathTool.toPath2D(path));
            } else {
                g.draw(DrawPathTool.toPath2D(path));
            }
            

            g.setPaint(GraphicsUtil.toAWT((FlatColor) path.getStrokePaint()));
            g.setStroke(new BasicStroke((float) path.getStrokeWidth()));
            if(path.isClosed()) {
                g.draw(DrawPathTool.toPath2D(path));
            } else {
                g.draw(DrawPathTool.toPath2D(path));
            }
            g.setStroke(new BasicStroke(1));
        }

        private void draw(Graphics2D g, NGon shape) {
            AffineTransform at = g.getTransform();
            g.setPaint(GraphicsUtil.toAWT((FlatColor) shape.getFillPaint()));

            double ang = 360.0/((double)shape.getSides());
            double rad = shape.getRadius();
            double x2 = Math.cos(Math.toRadians(ang))*rad;
            double y2 = Math.sin(Math.toRadians(ang))*rad;
            g.setStroke(new BasicStroke((float) shape.getStrokeWidth()));
            g.setPaint(GraphicsUtil.toAWT((FlatColor) shape.getStrokePaint()));
            for(int i=0; i<shape.getSides(); i++) {
                g.draw(new Line2D.Double(rad,0,x2,y2));
                g.rotate(Math.toRadians(ang));
            }
            g.setStroke(new BasicStroke(1));
            g.setTransform(at);


        }

        public void setIncludeDocumentBackground(boolean useDocBg) {
            this.useDocBg = useDocBg;
        }
    }
}
