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
            page.add(node);
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
                g.addAll(loadNode(n));
            }
            return g;
        }
        if("rect".equals(root.name())) {
            SRect rect = new SRect();
            rect.setX(Double.parseDouble(root.attr("x")));
            rect.setY(Double.parseDouble(root.attr("y")));
            //rect.setFillPaint(Double.parseDouble(root.attr("x")));
            parseFill(rect,root);
            rect.setWidth(Double.parseDouble(root.attr("width")));
            rect.setHeight(Double.parseDouble(root.attr("height")));
            return rect;
        }
        if("polygon".equals(root.name())) {
            String pointsString = root.attr("points");
            String[] points = pointsString.split("\\s");
            //u.p(points);
            SPoly poly = new SPoly();
            for(String pt : points) {
                if(pt != null && pt.trim().equals("")) continue;
                //u.p("pt = " + pt);
                String[] xy = pt.split(",");
                poly.addPoint(new Point2D.Double(
                        Double.parseDouble(xy[0]),
                        Double.parseDouble(xy[1])
                ));
            }
            poly.setClosed(true);
            parseFill(poly,root);
            return poly;
        }
        if("path".equals(root.name())) {
            return parsePathNode(root);
        }
        throw new Exception("unrecognized SVG element: " + root.name());
    }

    private static SNode parsePathNode(Elem root) throws IOException {
        SPath path = new SPath();
        String d = root.attr("d");
        u.p("data = " + d);

        PushbackReader read = new PushbackReader(new StringReader(d));
        int count = 0;
        double x = 0;
        double y = 0;
        double prevx1 = 0;
        double prevy1 = 0;
        SPath.PathPoint prev = null;
        while(true) {
            count++;
            if(count > 20)break;
            char ch = (char) read.read();
            if(ch == -1) break;

            //absolute move
            if(ch == 'M') {
                x = readDouble(read);
                y = readDouble(read);
                //u.p("move " + x + " , " + y);
                prev = path.moveTo(x,y);
                continue;
            }
            
            //relative vertical lineto
            if(ch == 'v') {
                y+= readDouble(read);
                //u.p("vertical line " + y);
                prev = path.lineTo(x,y);
                continue;
            }
            
            //absolute vertical lineto
            if(ch == 'V') {
                y = readDouble(read);
                //u.p("vertical line " + y);
                prev = path.lineTo(x,y);
                continue;
            }
            //relative horiz lineto
            if(ch == 'h') {
                x+= readDouble(read);
                //u.p("horizontal line " + x);
                prev = path.lineTo(x,y);
                continue;
            }
            //relative lineto
            if(ch == 'l') {
                x+= readDouble(read);
                y+= readDouble(read);
                //u.p("line to: " + x + " " + y);
                prev = path.lineTo(x, y);
                continue;
            }
            //relative cubic curve
            if(ch == 'c') {
                double x1 = x+readDouble(read);
                double y1 = y+readDouble(read);
                double x2 = x+readDouble(read);
                double y2 = y+readDouble(read);
                x += readDouble(read);
                y += readDouble(read);

                //u.p("cubic c1" + x1 + "," + y1 + " c2 " + x2 + ", " + y2 + " -> " + x + "," + y);
                prev = path.curveTo(prev,x1,y1,x2,y2,x,y);
                continue;
            }
            if(ch == 'C') {
                double x1 = readDouble(read);
                double y1 = readDouble(read);
                double x2 = readDouble(read);
                double y2 = readDouble(read);
                x = readDouble(read);
                y = readDouble(read);

                //u.p("cubic " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + x + " " + y);
                prev = path.curveTo(prev,x1,y1,x2,y2,x,y);
                continue;
            }
            if(ch == 'z') {
                //u.p("close path");
                path.setClosed(true);
                break;
            }
            if(ch == ' ') continue;
            if(ch == '\n') continue;
            //u.p("read char: " + ch);
        }

        path.close(false);
        parseFill(path,root);
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

    private static void parseFill(SShape node, Elem root) {
        String sfill = root.attr("fill");
        if(sfill.equals("none")) {
            node.setFillPaint(null);
            return;
        }
        if(sfill.startsWith("#")) {
            node.setFillPaint(new FlatColor(sfill));
            return;
        }
        u.p("trouble parsing fill: " + sfill);
    }
}
