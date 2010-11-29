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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
        throw new Exception("unrecognized SVG element: " + root.name());
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
