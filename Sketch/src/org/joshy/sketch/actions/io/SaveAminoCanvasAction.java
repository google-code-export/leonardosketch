package org.joshy.sketch.actions.io;

import org.joshy.gfx.draw.*;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.util.OSUtil;
import org.joshy.gfx.util.u;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.util.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Oct 30, 2010
 * Time: 5:32:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class SaveAminoCanvasAction extends SAction {
    private DocContext context;

    public SaveAminoCanvasAction(DocContext context) {
        this.context = context;
    }

    private static String HTML_CANVAS_PATH_KEY = "export.htmlcanvas.path";

    @Override
    public void execute() throws Exception {
        File file = null;
        Map props = context.getDocument().getProperties();
        if(props.containsKey(HTML_CANVAS_PATH_KEY)) {
            File ffile = new File((String)props.get(HTML_CANVAS_PATH_KEY));
            if(ffile.exists()) {
                file = ffile;
            }
        }

        if(file == null) {
            file = Util.requestDirectory("Export to Amino Canvas: Choose Output Directory",context);
            if(file == null) return;
        }

        File jsfile = new File(file,"generated.js");
        IndentWriter js_writer = new IndentWriter(new PrintWriter(new FileOutputStream(jsfile)));
        js_writer.basedir = file;
        ExportProcessor.process(new AminoExport(),js_writer,(SketchDocument) context.getDocument());

        File htmlfile = new File(file,"index.html");
        SaveAminoCanvasAction.outputIndexHTML(new IndentWriter(new PrintWriter(new FileOutputStream(htmlfile)))
                , (SketchDocument) context.getDocument());
        context.getDocument().setStringProperty(HTML_CANVAS_PATH_KEY,file.getAbsolutePath());
        OSUtil.openBrowser(htmlfile.toURI().toASCIIString());
    }

    private static void outputIndexHTML(IndentWriter out, SketchDocument doc) {
        out.println("<html><head><title>Amino Canvas Export</title>\n"
                +"<script src='"+ Main.AMINO_BINARY_URL+"'></script>\n"
                +"<script src='generated.js'></script>\n"
                +"<style type='text/css'>\n"
                //+"  body { background-color: " + AminoExport.serializeFlatColor(doc.getBackgroundFill()) + "; }\n"
                +"</style>\n"
                +"</head>");
        out.println("<body onload=\"setupDrawing();\">");
        out.println("<canvas"
                +" width=\""+Math.round(doc.getWidth())+"\""
                +" height=\""+Math.round(doc.getHeight())+"\""
                +" id=\"foo\"></canvas>");
        out.println("<script>");
        //function definition
        out.println("function setupDrawing(){");
        out.println("var runner =  new Runner();\n"
                +"runner.setCanvas(document.getElementById('foo'));\n"
                +"runner.setFPS(30);\n"
                //+"runner.setBackground("+AminoExport.serializePaint(out,doc.getBackgroundFill())+");\n"
                +"runner.setRoot(sceneRoot);\n"
        );
        out.println("runner.start();");
        out.println("}");
        out.println("</script>");
        out.println("</body>");
        out.println("</html>");
        out.close();
        out.close();
    }

    private static class AminoExport implements ShapeExporter<IndentWriter> {
        private static DecimalFormat df = new DecimalFormat();

        private AminoExport() {
            df.setMaximumFractionDigits(2);
        }

        public void docStart(IndentWriter out, SketchDocument doc) {

            //export named elements
            exportNamedElements(out, doc);
            out.println("var sceneRoot = new Group()");

            //export unnamed elements
        }

        private void exportNamedElements(IndentWriter out, SketchDocument document) {
            for(SketchDocument.SketchPage page : document.getPages()) {
                for(SNode node : page.getNodes()) {
                    exportNamedElements(out,node);
                }
            }
        }
        private void exportNamedElements(IndentWriter out, SNode node) {
            String id = node.getId();
            if(id != null && !id.equals("")) {
                out.println("var " + id + " = ");
                //do pre
                out.mode = "named";
                exportPre(out,node);
                //do post
                exportPost(out,node);
                //finish the statement
                out.mode = "";
                out.println(";");
            }
            if(node instanceof SGroup) {
                SGroup group = (SGroup) node;
                for(SNode nd : group.getNodes()) {
                    exportNamedElements(out,nd);
                }
            }
        }

        public void docEnd(IndentWriter out, SketchDocument document) {
            out.close();
        }

        public void pageStart(IndentWriter out, SketchDocument.SketchPage page) {
        }

        public void exportPre(IndentWriter out, SNode node) {
            u.p("exporting pre: " + node);
            //don't recurse if this branch is already cached in an image
            if("imagecache".equals(out.mode)) return;
            if("".equals(out.mode)) {
                String id = node.getId();
                if(id != null && !id.equals("")) {
                    out.println(".add("+id);
                    return;
                } else {
                    out.println(".add(");
                }
            }


            out.indent();
            if(node instanceof SShape) {
                SShape shape = (SShape) node;
                out.println("new Transform(");
                out.indent();

                if(node.getBooleanProperty("com.joshondesign.amino.nodecacheimage")) {
                    u.p("we need to render into an image instead");
                    renderToCachedImage(out, node);
                } else {

                    if(node instanceof SOval) {
                        SOval n = (SOval) node;
                        out.println("new Ellipse().set("
                                +n.getX()
                                +","+n.getY()
                                +","+n.getWidth()
                                +","+n.getHeight()
                                +")");
                    }
                    if(node instanceof SRect) {
                        SRect n = (SRect) node;
                        out.println("new Rect().set("
                                +n.getX()
                                +","+n.getY()
                                +","+n.getWidth()
                                +","+n.getHeight()
                                +")");
                        if(n.getCorner() != 0) {
                            out.println(".setCorner("+df.format(n.getCorner()/2.0)+")");
                        }
                    }
                    if(node instanceof NGon) {
                        NGon n = (NGon) node;
                        toPathNode(out, n, n.toArea(),
                                n.getTranslateX() - n.getRadius(),
                                n.getTranslateY() - n.getRadius()
                        );
                    }
                    if(node instanceof SArea) {
                        SArea n = (SArea) node;
                        toPathNode(out, n, n.toArea(),0,0);
                    }
                    if(node instanceof SPoly) {
                        SPoly n = (SPoly) node;
                        Bounds bounds = n.getBounds();
                        toPathNode(out, n, n.toArea()
                                ,bounds.getX()
                                ,bounds.getY());
                    }
                    if(node instanceof SPath) {
                        SPath n = (SPath) node;
                        //toPathNode(out, n.toArea());
                        serializePath(out,n);
                    }
                    if(node instanceof SText) {
                        SText n = (SText) node;
                        out.println("new Text()");
                        out.println(".setText('"+escapeString(n.getText())+"')");
                        out.println(".setX("+n.getX()+")");
                        out.println(".setY("+(n.getY()+n.getAscent())+")");
                        out.println(".setHAlign('"+n.getHalign().toString().toLowerCase()+"')");
                        out.println(".setAutoSize("+n.isAutoSize()+")");
                        out.println(".setWidth("+n.getWidth()+")");
                        double fontSize = n.getFontSize();
                        if(Toolkit.getDefaultToolkit().getScreenResolution() == 72) {
                            fontSize = fontSize / 1.33;
                        }

                        out.println(".setFont('"
                                +df.format(fontSize)+"pt "
                                +n.getFontName()+"')");
                    }
                    out.indent();
                    out.println(".setStrokeWidth(" + shape.getStrokeWidth() + ")");
                    out.println(".setStroke(" + serializePaint(out, shape.getStrokePaint()) + ")");
                    out.println(".setFill("   + serializePaint(out, shape.getFillPaint())   + ")");
                    if(shape.getFillOpacity() != 1.0) {
                        out.println(".setOpacity("+df.format(shape.getFillOpacity())+")");
                    }
                    if(node.getBooleanProperty("com.joshondesign.amino.nodecache")) {
                        out.println(".setCached(true)");
                    }
                }

                out.println(")");
                out.outdent();
                out.outdent();

                if(node instanceof NGon) {
                    NGon n = (NGon) node;
                    out.println(
                         ".setTranslateX(" + (shape.getTranslateX()-n.getRadius()) + ")"
                        +".setTranslateY(" + (shape.getTranslateY()-n.getRadius()) + ")"
                    );
                    return;
                }
                if(node instanceof SPoly) {
                    SPoly n = (SPoly) node;
                    Bounds bounds = n.getBounds();
                    out.println(
                         ".setTranslateX(" + bounds.getX() + ")"
                        +".setTranslateY(" + bounds.getY() + ")"
                    );
                    return;
                }
                if(node.getBooleanProperty("com.joshondesign.amino.nodecacheimage")) {
                    Bounds bounds = shape.getEffectBounds();
                    out.println(".setTranslateX(" + bounds.getX() + ").setTranslateY(" + bounds.getY() + ")");
                }
                if(node instanceof SArea) return;
                if(!node.getBooleanProperty("com.joshondesign.amino.nodecacheimage")) {
                    out.println(".setTranslateX(" + shape.getTranslateX() + ").setTranslateY(" + shape.getTranslateY() + ")");
                }
            }
            if(node instanceof SGroup) {
                SGroup n = (SGroup) node;
                if(node.getBooleanProperty("com.joshondesign.amino.nodecacheimage")) {
                    renderToCachedImage(out, node);
                    out.mode = "imagecache";
                } else {
                    out.println("new Group().setX("+n.getTranslateX()+").setY("+n.getTranslateY()+")");
                    if(node.getBooleanProperty("com.joshondesign.amino.nodecache")) {
                        out.println(".setCached(true)");
                    }
                }
                out.indent();
            }
            if(node instanceof SImage) {
                SImage n = (SImage) node;
                u.p("exporting image with relative url = " + n.getRelativeURL());
                out.println("new Transform(");
                out.indent();
                out.println("new ImageView('images/"+n.getRelativeURL()+"')");
                out.println(".setX("+n.getX()+")");
                out.println(".setY("+n.getY()+")");
                u.p("saving relative image to: " + n.getRelativeURL());
                u.p("path = " + out.basedir.getName() + " " + out.basedir.getPath());
                File imagesDir = new File(out.basedir,"images");
                if(!imagesDir.exists()) {
                    imagesDir.mkdir();
                }
                File imageFile = new File(imagesDir,n.getRelativeURL());
                try {
                    ImageIO.write(n.getBufferedImage(),"PNG",imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                out.println(")");
                out.outdent();
                out.println(".setTranslateX(" + n.getTranslateX() + ").setTranslateY(" + n.getTranslateY() + ")");
            }
            if(node.getBooleanProperty("com.joshondesign.amino.nodecache")) {
                out.println(".setCached(true)");
            }
        }

        private String escapeString(String text) {
            text = text.replaceAll("'","\\\\'");
            text = text.replaceAll("\n","\\\\n");
            return text;
        }

        private void renderToCachedImage(IndentWriter out, SNode node) {
            String id = node.getId();
            if(id == null || "".equals(id.trim())) {
                id = "img_"+Math.round(Math.random()*10000);
            }
            File file = new File(out.basedir,"cache_"+id+".png");
            List<SNode> nodes = new ArrayList<SNode>();
            nodes.add(node);
            SavePNGAction.export(file,nodes);
            out.println("new ImageView('"+file.getName()+"')");
        }

        private void serializePath(IndentWriter out, SPath path) {
            out.println("new PathNode()");
            out.indent();
            out.println(".setPath(");
            out.indent();
            out.println("new Path()");
            out.indent();
            Path2D.Double j2dpath = SPath.toPath(path);
            PathIterator it = j2dpath.getPathIterator(null);
            double dx = 0;
            double dy = 0;
            while(!it.isDone()) {
                double[] coords = new double[6];
                int n = it.currentSegment(coords);
                if(n == PathIterator.SEG_MOVETO) {
                    out.println(".moveTo("+(coords[0]-dx)+","+(coords[1]-dy)+")");
                }
                if(n == PathIterator.SEG_LINETO) {
                    out.println(".lineTo("+(coords[0]-dx)+","+(coords[1]-dy)+")");
                }
                if(n == PathIterator.SEG_CUBICTO) {
                    out.println(".curveTo("+
                            (coords[0]-dx)+","+(coords[1]-dy)+","+(coords[2]-dx)+","+(coords[3]-dy)+
                            ","+(coords[4]-dx)+","+(coords[5]-dy)+")"
                    );
                }
                if(n == PathIterator.SEG_CLOSE) {
                    out.println(".closeTo()");
                    break;
                }
                it.next();
            }
            out.println(".build()");
            out.outdent();
            out.outdent();
            out.println(")");
        }

        private void toPathNode(IndentWriter out, SShape shape, Area area, double xoff, double yoff) {
            out.println("new PathNode()");
            out.indent();
            out.println(".setPath(");
            out.indent();
            out.println("new Path()");
            out.indent();
            Rectangle2D bounds = area.getBounds2D();
            double dx = xoff;//bounds.getX();
            double dy = yoff;//bounds.getY();
            //don't subtract off the translation just yet
            //dx = xoff;
            //dy = yoff;
            PathIterator it = area.getPathIterator(null);
            while(!it.isDone()) {
                double[] coords = new double[6];
                int n = it.currentSegment(coords);
                if(n == PathIterator.SEG_MOVETO) {
                    out.println(".moveTo("+(coords[0]-dx)+","+(coords[1]-dy)+")");
                }
                if(n == PathIterator.SEG_LINETO) {
                    out.println(".lineTo("+(coords[0]-dx)+","+(coords[1]-dy)+")");
                }
                if(n == PathIterator.SEG_CUBICTO) {
                    out.println(".curveTo("+
                            (coords[0]-dx)+","+(coords[1]-dy)+","+(coords[2]-dx)+","+(coords[3]-dy)+
                            ","+(coords[4]-dx)+","+(coords[5]-dy)+")"
                    );
                }
                if(n == PathIterator.SEG_CLOSE) {
                    out.println(".closeTo()");
                    break;
                }
                it.next();
            }
            out.println(".build()");
            out.outdent();
            out.outdent();
            out.println(")");

        }

        private String toHexString(FlatColor color) {
            return "#"+String.format("%06x",color.getRGBA()&0x00FFFFFF);
        }

        private static String serializeFlatColor(FlatColor color) {
            return "rgba("
                    +(int)(255*color.getRed())
                    +","+(int)(255*color.getGreen())
                    +","+(int)(255*color.getBlue())
                    +","+(int)(255*color.getAlpha())
                    +")"
                    ;
        }
        private static String serializePaint(IndentWriter out, org.joshy.gfx.draw.Paint fillPaint) {
            if(fillPaint instanceof FlatColor) {
                FlatColor color = (FlatColor) fillPaint;
                return "'"+serializeFlatColor(color) + "'";
            }
            if(fillPaint instanceof MultiGradientFill) {
                StringBuffer sb = new StringBuffer();
                MultiGradientFill grad = (MultiGradientFill) fillPaint;

                if(grad instanceof LinearGradientFill) {
                    LinearGradientFill lgrad = (LinearGradientFill) grad;
                    sb.append("new LinearGradientFill("
                            +lgrad.getStartX()
                            +","+lgrad.getStartY()
                            +","+lgrad.getEndX()
                            +","+lgrad.getEndY()
                            +")"
                    );
                }

                if(grad instanceof RadialGradientFill) {
                    RadialGradientFill rgrad = (RadialGradientFill)grad;
                    sb.append("new RadialGradientFill("
                            + rgrad.getCenterX()
                            + "," + rgrad.getCenterY()
                            + "," + rgrad.getRadius()
                            +")"
                    );
                }

                for(MultiGradientFill.Stop stop : grad.getStops()) {
                    sb.append("\n.addStop("+df.format(stop.getPosition())
                            +",'"+serializeFlatColor(stop.getColor())+"'"
                            +")");
                }
                return sb.toString();
            }
            if(fillPaint instanceof PatternPaint) {
                PatternPaint pp = (PatternPaint) fillPaint;
                if(!out.images.containsKey(pp.getRelativeURL())) {
                    File imagesDir = new File(out.basedir,"images");
                    if(!imagesDir.exists()) {
                        imagesDir.mkdir();
                    }
                    File imageFile = new File(imagesDir,pp.getRelativeURL());
                    try {
                        ImageIO.write(pp.getImage(),"PNG",imageFile);
                        out.images.put(pp.getRelativeURL(),pp.getImage());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                StringBuffer sb = new StringBuffer();
                sb.append("new PatternFill(");
                sb.append("'images/"+pp.getRelativeURL()+"'");
                sb.append(",'repeat'");
                sb.append(")");
                return sb.toString();
            }
            return "";
        }

        public void exportPost(IndentWriter out, SNode node) {
            if(node instanceof SGroup) {
                if(node.getBooleanProperty("com.joshondesign.amino.nodecacheimage")) {
                    out.mode = "";
                }
                out.outdent();
            }
            if("".equals(out.mode)) {
                out.println(")");
            }
            out.outdent();
        }

        public void pageEnd(IndentWriter out, SketchDocument.SketchPage page) {
        }


        public boolean isContainer(SNode n) {
            if(n instanceof SGroup) return true;
            return false;
        }

        public Iterable<? extends SNode> getChildNodes(SNode n) {
            if(n instanceof SGroup) {
                SGroup sg = (SGroup) n;
                return sg.getNodes();
            }
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private class IndentWriter {
        private PrintWriter writer;
        private int tab = 0;
        private String tabString = "";
        public String mode = "";
        public File basedir;
        public Map<String,BufferedImage> images = new HashMap<String,BufferedImage>();

        public IndentWriter(PrintWriter printWriter) {
            this.writer = printWriter;
        }

        public void println(String s) {
            this.writer.print(tabString);
            this.writer.println(s);
        }

        public void close() {
            this.writer.close();
        }

        public void indent() {
            tab++;
            calcTabString();
        }

        public void outdent() {
            tab--;
            calcTabString();
        }

        private void calcTabString() {
            tabString = "";
            for(int i=0; i<tab; i++) {
                tabString += "  ";
            }
        }
    }
}

