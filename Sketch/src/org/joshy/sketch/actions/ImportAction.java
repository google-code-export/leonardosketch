package org.joshy.sketch.actions;

import com.joshondesign.xml.Doc;
import com.joshondesign.xml.Elem;
import com.joshondesign.xml.XMLParser;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.util.u;
import org.joshy.sketch.controls.StandardDialog;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.modes.preso.PresoModeHelper;
import org.joshy.sketch.modes.vector.VectorModeHelper;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Nov 16, 2010
 * Time: 5:38:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImportAction extends SAction {
    private DocContext context;

    public ImportAction(DocContext context) {
        super();
        this.context = context;
    }

    @Override
    public void execute() throws Exception {
        FileDialog fd = new FileDialog((Frame)context.getStage().getNativeWindow());
        fd.setMode(FileDialog.LOAD);
        fd.setTitle("Import Other Graphics File");
        fd.setVisible(true);
        if(fd.getFile() != null) {
            File file = new File(fd.getDirectory(),fd.getFile());
            u.p("opening a file" + file);
            try {
                load(file);
                context.main.recentFiles.add(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void load(File file) throws Exception {
        if(file.getName().toLowerCase().endsWith(".svg")) {
            SketchDocument doc = importSVG(file);
            if(doc.isPresentation()) {
                context.getMain().setupNewDoc(new PresoModeHelper(context.getMain()),doc);
            } else {
                context.getMain().setupNewDoc(new VectorModeHelper(context.getMain()),doc);
            }
            return;
        }


        StandardDialog.showError("Could not open file " + file.getName() + ".\nUnknown format. Sorry. :(");
    }

    private static SketchDocument importSVG(InputStream stream) throws Exception {
        SketchDocument sdoc = new SketchDocument();
        sdoc.removePage(sdoc.getCurrentPage());
        SketchDocument.SketchPage page = sdoc.addPage();
        u.p("parsing");
        Doc doc = XMLParser.parse(stream);
        u.p("parsed");
        Elem svg = doc.xpathElement("/svg");
        for(Elem n : svg.xpath("./*")) {
            u.p("node = " + n + " " + n.name());
            SNode node = loadNode(n);
            if(node != null) page.add(node);
        }
        return sdoc;
    }
    public static SketchDocument importSVG(File file) throws Exception {
        SketchDocument sdoc = importSVG(new FileInputStream(file));
        //sdoc.setFile(file);
        sdoc.setTitle(file.getName()+"");
        sdoc.setCurrentPage(0);
        sdoc.setDirty(false);
        return sdoc;
    }


    public static SketchDocument importSVG(URL url) throws Exception {
        SketchDocument sdoc = importSVG(url.openStream());
        //sdoc.setFile(file);
        sdoc.setTitle(url.getPath()+"");
        sdoc.setCurrentPage(0);
        sdoc.setDirty(false);
        return sdoc;
    }

    private static SNode loadNode(Elem root) throws Exception {
        u.p("loading SVG element: " + root.name());
        if("g".equals(root.name())) {
            SGroup g = new SGroup();
            for(Elem n : root.xpath("./*")) {
                SNode nn = loadNode(n);
                if(nn != null) g.addAll(false,nn);
            }
            g.normalize();
            return g;
        }
        if("rect".equals(root.name())) {
            SRect rect = new SRect();
            rect.setX(Double.parseDouble(root.attr("x")));
            rect.setY(Double.parseDouble(root.attr("y")));
            parseFill(rect,root);
            parseStroke(rect,root);
            rect.setWidth(Double.parseDouble(root.attr("width")));
            rect.setHeight(Double.parseDouble(root.attr("height")));
            return rect;
        }
        if("line".equals(root.name())) {
            SPoly poly = new SPoly();
            double x1 = Double.parseDouble(root.attr("x1"));
            double y1 = Double.parseDouble(root.attr("y1"));
            double x2 = Double.parseDouble(root.attr("x2"));
            double y2 = Double.parseDouble(root.attr("y2"));
            poly.addPoint(new Point2D.Double(x1,y1));
            poly.addPoint(new Point2D.Double(x2,y2));
            poly.setClosed(false);
            parseFill(poly,root);
            parseStroke(poly,root);
            return poly;
        }
        if("polygon".equals(root.name()) || "polyline".equals(root.name())) {
            String pointsString = root.attr("points");
            String[] points = pointsString.split("\\s");
            SPoly poly = new SPoly();
            for(String pt : points) {
                if(pt != null && pt.trim().equals("")) continue;
                String[] xy = pt.split(",");
                poly.addPoint(new Point2D.Double(
                        Double.parseDouble(xy[0]),
                        Double.parseDouble(xy[1])
                ));
            }
            //the polygon is closed, poly lines aren't
            poly.setClosed("polygon".equals(root.name()));
            parseFill(poly,root);
            parseStroke(poly,root);
            return poly;
        }
        if("path".equals(root.name())) {
            return parsePathNode(root);
        }

        Exception ex = new Exception("unrecognized SVG element: " + root.name());
        u.p(ex);
        return null;
    }

    private static SNode parsePathNode(Elem root) throws IOException {
        SPath path = new SPath();
        String d = root.attr("d");

        PushbackReader read = new PushbackReader(new StringReader(d));
        int count = 0;
        double x = 0;
        double y = 0;
        boolean closed = false;
        SPath.PathPoint prev = null;
        boolean go = true;
        while(go) {
            count++;
            char ch = (char) read.read();
            if(ch == -1) break;
            double x1,x2,y1,y2;
            u.p("ch = " + ch);
            switch(ch) {
                //absolute move
                case 'M':
                    x = readDouble(read);
                    y = readDouble(read);
                    prev = path.moveTo(x,y);
                    continue;
                //relative vertical lineto
                case 'v':
                    y+= readDouble(read);
                    prev = path.lineTo(x,y);
                    continue;
                //absolute vertical lineto
                case 'V':
                    y = readDouble(read);
                    prev = path.lineTo(x,y);
                    continue;
                //relative horiz lineto
                case 'h':
                    x+= readDouble(read);
                    prev = path.lineTo(x,y);
                    continue;
                case 'H':
                    x = readDouble(read);
                    prev = path.lineTo(x,y);
                    continue;
                //relative lineto
                case 'l':
                    x+= readDouble(read);
                    y+= readDouble(read);
                    prev = path.lineTo(x, y);
                    continue;
                case 'L':
                    x = readDouble(read);
                    y = readDouble(read);
                    prev = path.lineTo(x, y);
                    continue;
                //relative cubic curve
                case 'c':
                    x1 = x+readDouble(read);
                    y1 = y+readDouble(read);
                    x2 = x+readDouble(read);
                    y2 = y+readDouble(read);
                    x += readDouble(read);
                    y += readDouble(read);
                    prev = path.curveTo(prev,x1,y1,x2,y2,x,y);
                    continue;
                //relative shorthand curve
                case 's':
                    x2 = x+readDouble(read);
                    y2 = y+readDouble(read);
                    x += readDouble(read);
                    y += readDouble(read);
                    double dx = prev.x-prev.cx1;
                    double dy = prev.y-prev.cy1;
                    double rx = prev.x+dx;
                    double ry = prev.y+dy;
                    prev = path.curveTo(prev,rx,ry,x2,y2,x,y);
                    continue;
                //absolute cubic curve
                case 'C':
                    x1 = readDouble(read);
                    y1 = readDouble(read);
                    x2 = readDouble(read);
                    y2 = readDouble(read);
                    x = readDouble(read);
                    y = readDouble(read);
                    prev = path.curveTo(prev,x1,y1,x2,y2,x,y);
                    continue;
               case 'z':
                    prev = path.closeTo(prev);
                   closed = true;
                    break;
               case ' ': continue;
               case '\n': continue;
               //end of string
               case (char)-1:
                   go = false;
                   break;
               default:
                   u.p("unrecognized character! " + ch + " " + ((int)ch));
                   go = false;
                   break;
            }
        }

        path.close(closed);
        parseFill(path,root);
        parseStroke(path,root);
        return path;
    }

    private static double readDouble(PushbackReader read) throws IOException {
        StringBuffer s = new StringBuffer();
        int count = -1;
        while(true) {
            char ch = (char) read.read();
            //skip spaces
            if(ch == ' ') continue;
            count++;
            //allow a - only if at the beginning
            if(ch == '-' && count == 0) {
                //u.p("negative number");
                s.append(ch);
                continue;
            }
            if((ch >= '0' && ch <= '9') || ch == '.') {
                //u.p("got double part " + ch);
                s.append(ch);
            } else {
                if(ch != ',') {
                    read.unread(ch);
                }
                break;
            }
        }
        return Double.parseDouble(s.toString());
    }

    private static void parseFill(SShape shape, Elem root) {
        if(!root.hasAttr("fill")) {
            shape.setFillPaint(FlatColor.BLACK);
            return;
        }
        String sfill = root.attr("fill");
        if(sfill.equals("none")) {
            shape.setFillPaint(null);
            return;
        }
        if(sfill.startsWith("#")) {
            shape.setFillPaint(new FlatColor(sfill));
            return;
        }
        u.p("trouble parsing fill: " + sfill);
    }
    private static void parseStroke(SShape shape, Elem root) {
        if(!root.hasAttr("stroke")) {
            shape.setStrokeWidth(1);
            shape.setStrokePaint(null);
        }
        String sstroke = root.attr("stroke");
        if(sstroke.equals("none")) {
            shape.setStrokePaint(null);
        }
        if(sstroke.startsWith("#")) {
            shape.setStrokePaint(new FlatColor(sstroke));
        }

        if(!root.hasAttr("stroke-width")) {
            shape.setStrokeWidth(1);
        } else {
            String sstrokeWidth = root.attr("stroke-width");
            double strokeWidth = Double.parseDouble(sstrokeWidth);
            shape.setStrokeWidth(strokeWidth);
        }
    }

}
