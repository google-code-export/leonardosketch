package org.joshy.sketch.actions.io;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Paint;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Dec 4, 2010
 * Time: 12:25:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class SaveJava2DAction extends SAction {
    private DocContext context;

    public SaveJava2DAction(DocContext context) {
        super();
        this.context = context;
    }

    @Override
    public void execute() throws Exception {
        FileDialog fd = new FileDialog((Frame)context.getStage().getNativeWindow());
        fd.setMode(FileDialog.SAVE);
        fd.setTitle("Export PDF Image");
        File currentFile = context.getDocument().getFile();
        if(currentFile != null) {
            fd.setFile(currentFile.getName().substring(0,currentFile.getName().lastIndexOf('.'))+".pdf");
        }
        fd.setVisible(true);
        if(fd.getFile() != null) {
            String fileName = fd.getFile();
            if(!fileName.toLowerCase().endsWith(".java")) {
                fileName = fileName + ".java";
            }
            File file = new File(fd.getDirectory(),fileName);
            if(context.getDocument() instanceof SketchDocument) {
                export(file, (SketchDocument) context.getDocument());
            }
        }

    }

    private void export(File file, SketchDocument sketchDocument) throws FileNotFoundException {
        ExportProcessor.process(new Java2DExport(file),
                new PrintWriter(new FileOutputStream(file)),
                (SketchDocument) context.getDocument());
    }

    private class Java2DExport implements ShapeExporter<PrintWriter> {
        private File file;

        public Java2DExport(File file) {
            this.file = file;
        }

        public void docStart(PrintWriter out, SketchDocument doc) {
            out.println("import java.awt.*;");
            out.println("import javax.swing.JComponent;");
            out.println("import javax.swing.JFrame;");
            out.println("import javax.swing.SwingUtilities;");
            String fname = file.getName();
            if(fname.endsWith(".java")) {
                fname = fname.substring(0,fname.length()-5);
            }
            out.println("public class "+fname.replaceAll(" |\\.","_") + " {");
            out.println("public static void main(String ... args) {");
            out.println("    SwingUtilities.invokeLater(new Runnable(){public void run() {");
            out.println("        JFrame frame = new JFrame(\""+file.getName()+"\");");
            out.println("        frame.add(new JComponent(){ protected void paintComponent(Graphics graphics) {");
            out.println("            super.paintComponent(graphics);");
            out.println("            drawPage((Graphics2D)graphics);");
            out.println("        }});");
            out.println("        frame.pack();");
            out.println("        frame.setSize(640,480);");            
            out.println("        frame.show();");
            out.println("    }});");
            out.println("}");
            out.println();
            out.println();
        }

        public void pageStart(PrintWriter out, SketchDocument.SketchPage page) {
            out.println("public static void drawPage(Graphics2D g) {");
            out.println("  g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);");
        }

        public void exportPre(PrintWriter out, SNode node) {
            out.println();
            out.println("  //drawing " + node.getClass().getName());
            out.println("  g.translate("+node.getTranslateX()+","+node.getTranslateY()+");");
            if(node instanceof SShape) {
                SShape shape = (SShape) node;
                out.println("  g.setPaint("+serialize(shape.getFillPaint())+");");
                out.println("  g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,"+shape.getFillOpacity()+"f));");
                if(shape instanceof SRect) {
                    SRect rect = (SRect) shape;
                    out.println("  g.fillRect("+(int)rect.getX()+","+(int)rect.getY()+","+(int)rect.getWidth()+","+(int)rect.getHeight()+");");
                }
                if(shape instanceof SOval) {
                    SOval oval = (SOval) shape;
                    out.println("  g.fillOval("+(int)oval.getX()+","+(int)oval.getY()+","+(int)oval.getWidth()+","+(int)oval.getHeight()+");");
                }
                if(shape.getStrokeWidth() > 0) {
                    Graphics2D g2 = null;
                    out.println("  g.setStroke(new BasicStroke((float) "+shape.getStrokeWidth()+"));");
                    out.println("  g.setPaint("+serialize(shape.getStrokePaint())+");");
                    out.println("  g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,"+1.0+"f));");
                    if(shape instanceof SRect) {
                        SRect rect = (SRect) shape;
                        out.println("  g.drawRect("+(int)rect.getX()+","+(int)rect.getY()+","+(int)rect.getWidth()+","+(int)rect.getHeight()+");");
                    }
                    if(shape instanceof SOval) {
                        SOval oval = (SOval) shape;
                        out.println("  g.drawOval("+(int)oval.getX()+","+(int)oval.getY()+","+(int)oval.getWidth()+","+(int)oval.getHeight()+");");
                    }
                }
            }
            out.println("  g.translate("+(-node.getTranslateX())+","+(-node.getTranslateY())+");");
        }

        private String serialize(Paint fillPaint) {
            if(fillPaint instanceof FlatColor) {
                FlatColor color = (FlatColor) fillPaint;
                return "new Color("+(int)(255*color.getRed())+","+(int)(255*color.getGreen())+","+(int)(255*color.getBlue())+")";
            }
            return "";
        }

        public void exportPost(PrintWriter out, SNode shape) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void pageEnd(PrintWriter out, SketchDocument.SketchPage page) {
            out.println("}");
        }

        public void docEnd(PrintWriter out, SketchDocument document) {
            out.println();
            out.println();
            out.println("}");
            out.close();
        }

        public boolean isContainer(SNode n) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Iterable<? extends SNode> getChildNodes(SNode n) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
