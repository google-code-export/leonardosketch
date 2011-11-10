package org.joshy.sketch.actions.io;

import org.joshy.gfx.draw.*;
import org.joshy.gfx.util.OSUtil;
import org.joshy.gfx.util.u;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.util.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.*;
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
public class SaveAminoCanvasAction extends BaseExportAction {

    public SaveAminoCanvasAction(DocContext context) {
        super(context);
    }

    private static String HTML_CANVAS_PATH_KEY = "export.htmlcanvas.path";

    @Override
    public void execute() throws Exception {
        exportToDirectory(null);
    }

    private void exportToDirectory(File file) throws FileNotFoundException {
        if(file == null) {
            file = Util.requestDirectory("Export to Amino Canvas: Choose Output Directory",context);
            if(file == null) return;
        }

        File jsfile = new File(file,"generated.js");
        IndentWriter js_writer = new IndentWriter(new PrintWriter(new FileOutputStream(jsfile)));
        js_writer.basedir = file;
        ExportProcessor.process(new AminoExport(),js_writer,(SketchDocument) context.getDocument());

        File htmlfile = new File(file,"index.html");
        if(!htmlfile.exists()) {
            SaveAminoCanvasAction.outputIndexHTML(new IndentWriter(new PrintWriter(new FileOutputStream(htmlfile)))
                    , (SketchDocument) context.getDocument());
        }
        context.getDocument().setStringProperty(HTML_CANVAS_PATH_KEY,file.getAbsolutePath());
        OSUtil.openBrowser(htmlfile.toURI().toASCIIString());
        context.setLastExportAction(this);

    }

    public void exportHeadless() throws FileNotFoundException {
        File file = null;
        Map props = context.getDocument().getProperties();
        if(props.containsKey(HTML_CANVAS_PATH_KEY)) {
            File ffile = new File((String)props.get(HTML_CANVAS_PATH_KEY));
            if(ffile.exists()) {
                file = ffile;
            }
        }
        exportToDirectory(file);
    }

    @Override
    protected String getStandardFileExtension() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void export(File file, SketchDocument document) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private static void outputIndexHTML(IndentWriter out, SketchDocument doc) {
        out.println("<html><head><title>Amino Canvas Export</title>\n"
                +"<script src='"+ Main.AMINO_BINARY_URL+"'></script>\n"
                +"<script src='generated.js'></script>\n"
                +"<style type='text/css'>\n"
                +"  body {\n"
        );
        if(doc.getBackgroundFill() instanceof FlatColor) {
            out.indent();
            out.println("   background-color: " + AminoExport.serializeFlatColor((FlatColor) doc.getBackgroundFill()));
            out.outdent();
        }
        out.println(
                 "  }\n"
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
                +"runner.setBackground("+AminoExport.serializePaint(out,doc.getBackgroundFill())+");\n"
                +"runner.setRoot(sceneRoot);\n"
                +"runner.DEBUG=true;\n"

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
                if (renderShape(out, (SShape)node)) return;
            }
            if(node instanceof SGroup) {
                SGroup n = (SGroup) node;
                if(node.getBooleanProperty("com.joshondesign.amino.nodecacheimage")) {
                    renderToCachedImage(out, node);
                    out.mode = "imagecache";
                } else {
                    out.println("new Group()");
                    out.prop("x",n.getTranslateX());
                    out.prop("y",n.getTranslateY());
                    if(node.getBooleanProperty("com.joshondesign.amino.nodecache")) {
                        out.prop("cached",true);
                    }
                }
                out.indent();
            }
            if(node instanceof SImage) {
                SImage shape = (SImage) node;
                u.p("exporting image with relative url = " + shape.getRelativeURL());
                out.println("new Transform(");
                out.indent();
                out.println("new ImageView('images/" +shape.getRelativeURL() + "')");
                out.prop("x", shape.getX());
                out.prop("y", shape.getY());
                u.p("saving relative image to: " + shape.getRelativeURL());
                u.p("path = " + out.basedir.getName() + " " + out.basedir.getPath());
                File imagesDir = new File(out.basedir,"images");
                if(!imagesDir.exists()) {
                    imagesDir.mkdir();
                }
                File imageFile = new File(imagesDir,shape.getRelativeURL());
                try {
                    ImageIO.write(shape.getBufferedImage(),"PNG",imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                out.println(")");
                out.outdent();
                out.prop("translateX",shape.getTranslateX());
                out.prop("translateY",shape.getTranslateY());
                out.prop("anchorX",shape.getAnchorX());
                out.prop("anchorY",shape.getAnchorY());
                out.prop("scaleX",shape.getScaleX());
                out.prop("scaleY",shape.getScaleY());
                out.prop("rotate",shape.getRotate());
            }
            if(node.getBooleanProperty("com.joshondesign.amino.nodecache")) {
                out.println(".setCached(true)");
            }
        }

        private boolean renderShape(IndentWriter out, SShape shape) {
            out.println("new Transform(");
            out.indent();

            if(shape.getBooleanProperty("com.joshondesign.amino.nodecacheimage")) {
                u.p("we need to render into an image instead");
                renderToCachedImage(out, shape);
                if(shape instanceof SResizeableNode) {
                    SResizeableNode resize = (SResizeableNode) shape;
                    out.prop("x", resize.getX());
                    out.prop("y", resize.getY());
                }
            } else {

                if(shape instanceof SOval) {
                    SOval n = (SOval) shape;
                    out.println("new Ellipse().set("+n.getX()+","+n.getY()+","+n.getWidth()+","+n.getHeight()+")");
                }
                if(shape instanceof SRect) {
                    SRect n = (SRect) shape;
                    out.println("new Rect().set("+n.getX()+","+n.getY()+","+n.getWidth()+","+n.getHeight()+")");
                    if(n.getCorner() != 0) {
                        //out.println(".setCorner("+df.format(n.getCorner()/2.0)+")");
                        out.prop("corner",n.getCorner()/2.0);
                    }
                }
                if(shape instanceof NGon) {
                    toPathNode(out, ((NGon)shape).toUntransformedArea(), 0,0 );
                }
                if(shape instanceof SArea) {
                    toPathNode(out, ((SArea)shape).toUntransformedArea(),0,0);
                }
                if(shape instanceof SPoly) {
                    toPathNode(out, shape.toArea(),0,0);
                }
                if(shape instanceof SPath) {
                    serializePath(out,(SPath)shape);
                }
                if(shape instanceof SText) {
                    SText n = (SText) shape;
                    out.println("new Text()");
                    out.prop("text",escapeString(n.getText()));
                    out.prop("x",n.getX());
                    out.prop("y",n.getY()+n.getAscent());
                    out.prop("hAlign",n.getHalign().toString().toLowerCase());
                    out.prop("autoSize",n.isAutoSize());
                    out.prop("width",n.getWidth());
                    double fontSize = n.getFontSize();
                    if(Toolkit.getDefaultToolkit().getScreenResolution() == 72) {
                        fontSize = fontSize / 1.33;
                    }

                    out.prop("font",df.format(fontSize)+"pt "+n.getFontName());
                }
                out.indent();
                out.prop("strokeWidth", shape.getStrokeWidth());
                out.println(".setFill(" + serializePaint(out,shape.getFillPaint()) + ")");
                out.println(".setStroke(" + serializePaint(out,shape.getStrokePaint()) + ")");
                if(shape.getFillOpacity() != 1.0) {
                    out.prop("opacity", shape.getFillOpacity());
                }
                if(shape.getBooleanProperty("com.joshondesign.amino.nodecache")) {
                    out.prop("cached", true);
                }
            }

            out.println(")");
            out.outdent();
            out.outdent();

            out.prop("translateX",shape.getTranslateX());
            out.prop("translateY", shape.getTranslateY());
            out.prop("anchorX", shape.getAnchorX());
            out.prop("anchorY", shape.getAnchorY());
            out.prop("scaleX", shape.getScaleX());
            out.prop("scaleY",shape.getScaleY());
            out.prop("rotate",shape.getRotate());
            return false;
        }

        private String escapeString(String text) {
            text = text.replaceAll("'","\\\\'");
            text = text.replaceAll("\n","\\\\n");
            return text;
        }

        private void renderToCachedImage(IndentWriter out, SNode node) {
            String name = node.getStringProperty("random.cache.name");
            if(name == null) {
            String id = node.getId();
            if(id == null || "".equals(id.trim())) {
                id = "img_"+Math.round(Math.random()*10000);
            }
                name = "cache_"+id+".png";
                node.setStringProperty("random.cache.name",name);
            }
            File file = new File(out.basedir,name);
            List<SNode> nodes = new ArrayList<SNode>();
            nodes.add(node);
            SavePNGAction.exportFragment(file, nodes);
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

        private void toPathNode(IndentWriter out, Area area, double xoff, double yoff) {
            out.println("new PathNode()");
            out.indent();
            out.println(".setPath(");
            out.indent();
            out.println("new Path()");
            out.indent();
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
                    +","+df.format(color.getAlpha())
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

        public void prop(String propName, String value) {
            println(".set"+propName.substring(0,1).toUpperCase()+propName.substring(1));
            println("('"+value+"')");
        }
        public void prop(String propName, double value) {
            println(".set"+propName.substring(0,1).toUpperCase()+propName.substring(1));
            println("("+value+")");
        }
        public void prop(String propName, boolean value) {
            println(".set"+propName.substring(0,1).toUpperCase()+propName.substring(1));
            println("("+value+")");
        }
    }
}

