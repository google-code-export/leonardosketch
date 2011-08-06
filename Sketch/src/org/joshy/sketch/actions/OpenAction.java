package org.joshy.sketch.actions;

import com.joshondesign.xml.Doc;
import com.joshondesign.xml.Elem;
import com.joshondesign.xml.XMLParser;
import org.joshy.gfx.draw.*;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.util.u;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.io.NativeExport;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.pixel.PixelModeHelper;
import org.joshy.sketch.modes.preso.PresoModeHelper;
import org.joshy.sketch.modes.vector.VectorModeHelper;
import org.joshy.sketch.util.Log;
import org.joshy.sketch.util.LogDialog;

import javax.imageio.ImageIO;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OpenAction extends SAction {
    private File specificFile;
    private Main main;

    public OpenAction(Main main) {
        this.main = main;
    }

    public OpenAction(Main main, File specificFile) {
        this.main = main;
        this.specificFile = specificFile;
    }

    public void execute(List<File> files) {
        try {
            load(files.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void execute() {
        if(specificFile != null) {
            try {
                load(specificFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            FileDialog fd = new FileDialog((Frame)null);
            fd.setMode(FileDialog.LOAD);
            fd.setTitle("Open Leonardo File");
            fd.setVisible(true);
            if(fd.getFile() != null) {
                File file = new File(fd.getDirectory(),fd.getFile());
                u.p("opening a file" + file);
                try {
                    load(file);
                    main.addRecentFile(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void load(File file) {

        Log.LogCollector col = new ClassFilterLogCollector(OpenAction.class);
        Log.addCollector(col);
        try {
            if(file.getName().toLowerCase().endsWith(".png")) {
                loadPng(file);
            } if (file.getName().toLowerCase().endsWith(".leoz")) {
                SketchDocument doc = loadZip(file);
                if(doc.isPresentation()) {
                    main.setupNewDoc(new PresoModeHelper(main),doc);
                } else {
                    main.setupNewDoc(new VectorModeHelper(main),doc);
                }
            } else {
                SketchDocument doc = load(new FileInputStream(file), file, file.getName(),null);
                if(doc.isPresentation()) {
                    main.setupNewDoc(new PresoModeHelper(main),doc);
                } else {
                    main.setupNewDoc(new VectorModeHelper(main),doc);
                }
            }
        } catch (Exception ex) {
            Log.error(ex);
        }

        Log.removeCollector(col);
        if(col.hasErrors()) {
            LogDialog.show("There were errors while loading the file",col);
        }
        if(col.hasWarnings()) {
            LogDialog.show("There were warnings while loading the file",col);
        }
        if(col.hasInfo()) {
            //LogDialog.show("there were infos while loading the application");
        }
    }

    private void loadPng(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            PixelDocument doc = new PixelDocument(img);
            main.setupNewDoc(new PixelModeHelper(main),doc);
        } catch (Exception e) {
            Log.error(e);
        }
    }

    public static SketchDocument load(InputStream in, File file, String fileName, ZipFile zipFile) throws Exception {
        Doc doc = XMLParser.parse(in);
        if(file != null) {
            doc.setBaseURI(file.toURI());
        }
        SketchDocument sdoc = new SketchDocument();
        sdoc.removePage(sdoc.getCurrentPage());
        int version = Integer.parseInt(doc.xpathString("/sketchy/@version"));

        Log.info("version = ",version);
        if(version < NativeExport.CURRENT_VERSION) {
            doc = upgradeDocument(doc);
        }
        for(Elem e : doc.xpath("/sketchy/page")) {
            loadPage(sdoc,e,zipFile);
        }
        String type = doc.xpathString("/sketchy/info/@type");
        if("presentation".equals(type)) {
            sdoc.setPresentation(true);
        }

        Elem info = doc.xpathElement("/sketchy/info");
        if(info != null) {
            if(info.hasAttr("backgroundFill")) {
                loadFlatColorAttribute(info,sdoc,"backgroundFill", FlatColor.class);
            }
            for(Elem element : info.xpath("property")) {
                sdoc.setStringProperty(
                        element.attr("name"),
                        element.attr("value"));
            }

        }

        sdoc.setFile(file);
        sdoc.setTitle(fileName+"");
        sdoc.setCurrentPage(0);
        sdoc.setDirty(false);
        return sdoc;
    }

    public static SketchDocument loadZip(File file) throws Exception {
        ZipFile zf = new ZipFile(file);
        Enumeration<? extends ZipEntry> en = zf.entries();
        while(true) {
            if(!en.hasMoreElements()) break;
            ZipEntry entry = en.nextElement();
            Log.info("loading entry",entry.getName());
            if(entry.getName().endsWith("/leo.xml")) {
                String name = entry.getName();
                String dir = name.substring(0, name.indexOf("/leo.xml"));
                return load(zf.getInputStream(entry),file,dir,zf);
            }
        }
        return null;
    }


    private static Doc upgradeDocument(Doc doc) {
        URL upgradeXSL = NativeExport.class.getResource("upgrade_-1_0.xsl");
        try {
            Log.info("upgrading document to version 0");
            doc = XMLParser.translate(doc,upgradeXSL.toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    private static void loadPage(SketchDocument sdoc, Elem epage, ZipFile zipFile) throws XPathExpressionException {
        SketchDocument.SketchPage page = sdoc.addPage();
        for(Elem e : epage.xpath("./guidelines/guideline")) {
            page.createGuideline(
                    Double.parseDouble(e.attr("position")),
                    Boolean.parseBoolean(e.attr("vertical"))
                    );
        }
        for(Elem e : epage.xpath("./*")) {
            //skip guidelines
            if("guidelines".equals(e.name())) continue;
            //do all shapes
            SNode sh = loadAnyShape(e,zipFile);
            if(sh == null) {
                Log.warning("Unknown shape parsed: " + e.name());
            }
            if(sh != null) {
                page.add(sh);
            }
        }
    }

    private static SNode loadAnyShape(Elem e, ZipFile zipFile) throws XPathExpressionException {
        if("group".equals(e.name())) {
            return loadGroup(e,zipFile);
        }
        if("node".equals(e.name())) {
            return loadNode(e,zipFile);
        }
        if("shape".equals(e.name())) {
            return loadShape(e,zipFile);
        }
        if("resizeableNode".equals(e.name())) {
            return (SNode) loadResizableNode(e,zipFile);
        }
        return null;
    }

    public static List<SNode> loadShapes(File file, ZipFile zipFile) throws Exception {
        Doc doc = XMLParser.parse(new FileInputStream(file));
        doc.setBaseURI(file.toURI());
        List<SNode> shapes = new ArrayList<SNode>();
        for(Elem e : doc.xpath("/sketchy/*")) {
            shapes.add(loadAnyShape(e,zipFile));
        }
        return shapes;
    }

    private static SNode loadGroup(Elem elem, ZipFile zipFile) throws XPathExpressionException {
        SGroup group = new SGroup();
        List<SNode> nodes = new ArrayList<SNode>();
        
        for(Elem element : elem.xpath("*")) {
            SNode node = loadAnyShape(element,zipFile);
            if(node != null) nodes.add(node);
        }
        
        group.addAll(nodes);
        loadNumberAttribute(elem,group,"translateX");
        loadNumberAttribute(elem,group,"translateY");
        loadStringAttribute(elem,group,"id");
        loadProperties(elem,group);
        return group;
    }

    private static SNode loadNode(Elem e, ZipFile zipFile) throws XPathExpressionException {
        SNode node = null;
        if(e.attrEquals("type","image")) {
            try {
                if(zipFile == null) {
                    node = new SImage(e.getDoc().getBaseURI(),"resources/"+e.attr("relativeURL"));
                } else {
                    node = loadSImageFromFile(e,zipFile);
                }
                SImage image = (SImage) node;
                loadStringAttribute(e,image,"id");
                loadNumberAttribute(e,image,"translateX");
                loadNumberAttribute(e,image,"translateY");
                if(e.hasAttr("strokePaint")) {
                    loadFlatColorAttribute(e,image,"strokePaint", FlatColor.class);
                } else {
                    image.setStrokePaint(null);
                }
                loadNumberAttribute(e,image,"strokeWidth");
            } catch (IOException e1) {
                Log.warning(e1);
            }
        }
        loadProperties(e,node);
        return node;
    }

    private static SImage loadSImageFromFile(Elem e, ZipFile zipFile) throws IOException {
        u.p("loading an image from a file");
        String path = e.attr("relativeURL");
        u.p("path = " + path);
        String fullpath = "resources/"+path;
        u.p("fullpath = " + fullpath);
        String zfname = zipFile.getName();
        u.p("zip file.getName() = " + zfname);
        String pth = zfname.substring(
                        zfname.lastIndexOf(File.separator)+1,
                        zfname.lastIndexOf(".")
                    )+"/"+fullpath;
        u.p("calculated pathh = " + pth);
        ZipEntry entry = zipFile.getEntry(pth);
        u.p("entry = " + entry);
        BufferedImage img = ImageIO.read(zipFile.getInputStream(entry));
        return new SImage(img,path);
    }

    private static SNode loadShape(Elem e, ZipFile zipFile) throws XPathExpressionException {
        SShape shape = null;
        if(e.attrEquals("type","path")) {
            shape = new SPath();
        }
        if(e.attrEquals("type","poly")) {
            shape = new SPoly();
            loadPolyPoints(e,(SPoly)shape);
            loadBooleanAttribute(e,shape,"closed");
        }
        if(e.attrEquals("type","ngon")) {
            shape = new NGon();
            loadNumberAttribute(e,shape,"radius");
            loadIntegerAttribute(e,shape,"sides");
            loadNumberAttribute(e,shape,"angle");
        }
        if(e.attrEquals("type","area")) {
            shape = new SArea(new Area());
        }

        if(shape == null) {
            Log.warning("warning. shape not detected. Shape type is: ",e.attr("type"));
            return null;
        }
        loadStringAttribute(e,shape,"id");
        if(e.hasAttr("fillPaint")) {
            loadFillPaint(e,shape, zipFile);
        } else {
            shape.setFillPaint(null);
        }
        loadNumberAttribute(e,shape,"fillOpacity");
        if(e.hasAttr("strokePaint")) {
            loadFlatColorAttribute(e,shape,"strokePaint", FlatColor.class);
        } else {
            shape.setStrokePaint(null);
        }
        loadNumberAttribute(e,shape,"strokeWidth");
        loadNumberAttribute(e,shape,"translateX");
        loadNumberAttribute(e,shape,"translateY");
        if(e.attrEquals("type","path")) {
            SPath path = (SPath) shape;
            loadBooleanAttribute(e,shape,"closed");

            for(Elem element : e.xpath("pathpoint")) {
                path.addPoint(new SPath.PathPoint(
                        Double.parseDouble(element.attr("x")),
                        Double.parseDouble(element.attr("y")),
                        Double.parseDouble(element.attr("cx1")),
                        Double.parseDouble(element.attr("cy1")),
                        Double.parseDouble(element.attr("cx2")),
                        Double.parseDouble(element.attr("cy2"))
                ));
            }
            path.recalcPath();
        }

        if(e.attrEquals("type","area")) {
            SArea area = (SArea) shape;
            Path2D.Double path = new Path2D.Double();
            for(Elem element : e.xpath("*")) {
                if(element.name().equals("move")) {
                    path.moveTo(
                            Double.parseDouble(element.attr("x")),
                            Double.parseDouble(element.attr("y"))
                    );
                }
                if(element.name().equals("lineto")) {
                    path.lineTo(
                            Double.parseDouble(element.attr("x")),
                            Double.parseDouble(element.attr("y"))
                    );
                }
                if(element.name().equals("curveto")) {
                    path.curveTo(
                            Double.parseDouble(element.attr("cx1")),
                            Double.parseDouble(element.attr("cy1")),
                            Double.parseDouble(element.attr("cx2")),
                            Double.parseDouble(element.attr("cy2")),
                            Double.parseDouble(element.attr("x2")),
                            Double.parseDouble(element.attr("y2"))
                    );
                }
                if(element.name().equals("close")) {
                    path.closePath();
                }
            }
            area.setArea(new Area(path));
        }

        loadProperties(e,shape);
        return shape;
    }

    private static void loadFillPaint(Elem e, SShape shape, ZipFile zipFile) throws XPathExpressionException {
        if("gradient".equals(e.attr("fillPaint"))) {
            FlatColor start = null;
            FlatColor end = null;
            double angle = Double.parseDouble(e.xpathString("gradient/@angle"));
            for(Elem stop : e.xpath("gradient/stop")) {
                if(stop.attrEquals("name","start")) {
                    start = new FlatColor(stop.attr("color"));
                }
                if(stop.attrEquals("name","end")) {
                    end = new FlatColor(stop.attr("color"));
                }
            }
            GradientFill fill = new GradientFill(start,end,angle,true);
            shape.setFillPaint(fill);
            return;
        }
        if("linearGradient".equals(e.attr("fillPaint"))) {
            Elem egrad = e.xpath("linearGradient").iterator().next();
            LinearGradientFill fill = new LinearGradientFill()
                    .setStartX(Double.parseDouble(egrad.attr("startX")))
                    .setStartY(Double.parseDouble(egrad.attr("startY")))
                    .setEndX(Double.parseDouble(egrad.attr("endX")))
                    .setEndY(Double.parseDouble(egrad.attr("endY")))
                    ;
            if(egrad.hasAttr("startXSnapped")) fill.setStartXSnapped(LinearGradientFill.Snap.valueOf(egrad.attr("startXSnapped")));
            if(egrad.hasAttr("startYSnapped"))fill.setStartYSnapped(LinearGradientFill.Snap.valueOf(egrad.attr("startYSnapped")));
            if(egrad.hasAttr("endXSnapped"))fill.setEndXSnapped(LinearGradientFill.Snap.valueOf(egrad.attr("endXSnapped")));
            if(egrad.hasAttr("endYSnapped"))fill.setEndYSnapped(LinearGradientFill.Snap.valueOf(egrad.attr("endYSnapped")));
            for(Elem stop : egrad.xpath("stop")) {
                fill.addStop(
                        Double.parseDouble(stop.attr("position")),
                        new FlatColor(stop.attr("color"))
                        );
            }
            shape.setFillPaint(fill);
            return;
        }
        if("radialGradient".equals(e.attr("fillPaint"))) {
            Elem egrad = e.xpath("radialGradient").iterator().next();
            RadialGradientFill fill = new RadialGradientFill()
                    .setCenterX(Double.parseDouble(egrad.attr("centerX")))
                    .setCenterY(Double.parseDouble(egrad.attr("centerY")))
                    .setRadius(Double.parseDouble(egrad.attr("radius")))
                    ;
            for(Elem stop : egrad.xpath("stop")) {
                fill.addStop(
                        Double.parseDouble(stop.attr("position")),
                        new FlatColor(stop.attr("color"))
                        );
            }
            shape.setFillPaint(fill);
            return;
        }
        if("patternPaint".equals(e.attr("fillPaint"))) {
            Elem pp = e.xpath("patternPaint").iterator().next();
            PatternPaint pat = null;
            u.p("using url " + pp.attr("relativeURL"));

            try {
                u.p("loading an image from a file");
                String path = pp.attr("relativeURL");
                String fullpath = "resources/"+path;
                u.p("path = " + path);
                u.p("fullpath = " + fullpath);

                String zfname = zipFile.getName();
                u.p("name = " + zfname);
                String pth = zfname.substring(
                                zfname.lastIndexOf(File.separator)+1,
                                zfname.lastIndexOf(".")
                            )+"/"+fullpath;
                u.p("pth = " + pth);
                ZipEntry entry = zipFile.getEntry(pth);
                u.p("entry = " + entry);
                BufferedImage img = ImageIO.read(zipFile.getInputStream(entry));
                pat = PatternPaint
                    .create(img,path)
                    .deriveNewStart(new Point2D.Double(
                            Double.parseDouble(pp.attr("startX")),
                            Double.parseDouble(pp.attr("startY"))))
                    .deriveNewEnd(new Point2D.Double(
                            Double.parseDouble(pp.attr("endX")),
                            Double.parseDouble(pp.attr("endY"))));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            shape.setFillPaint(pat);
            return;
        }


        FlatColor fc = new FlatColor(e.attr("fillPaint"));
        shape.setFillPaint(fc);

    }

    private static void loadProperties(Elem e, SNode shape) throws XPathExpressionException {
        for(Elem element : e.xpath("property")) {
            shape.setStringProperty(
                    element.attr("name"),
                    element.attr("value"));
        }
    }

    private static SResizeableNode loadResizableNode(Elem e, ZipFile zipFile) throws XPathExpressionException {
        SResizeableNode node = null;
        if(e.attrEquals("type","rect")) {
            node = new SRect();
            loadNumberAttribute(e, node,"corner");
        }
        if(e.attrEquals("type","oval")) {
            node = new SOval();
        }
        if(e.attrEquals("type","grid9")) {
            node = new ResizableGrid9Shape(0,0,0,0);
            loadNumberAttribute(e,node,"left");
            loadNumberAttribute(e,node,"right");
            loadNumberAttribute(e,node,"top");
            loadNumberAttribute(e,node,"bottom");
            loadNumberAttribute(e,node,"originalWidth");
            loadNumberAttribute(e,node,"originalHeight");
            loadBooleanAttribute(e,node,"vLocked");
            loadBooleanAttribute(e,node,"hLocked");
        }
        if(e.attrEquals("type","text")) {
            node = new SText();
            loadNumberAttribute(e,node,"fontSize");
            loadStringAttribute(e,node,"text");
            loadEnumAttribute(e,node,"weight", Font.Weight.class);
            loadEnumAttribute(e,node,"style", Font.Style.class);
            loadEnumAttribute(e,node,"halign",SText.HAlign.class);
            loadBooleanAttribute(e,node,"autoSize");
        }
        if(e.attrEquals("type","image")) {
            try {
                if(zipFile == null) {
                    node = new SImage(e.getDoc().getBaseURI(),"resources/"+e.attr("relativeURL"));
                } else {
                    node = loadSImageFromFile(e,zipFile);
                }
                SImage image = (SImage) node;
                if(e.hasAttr("strokePaint")) {
                    loadFlatColorAttribute(e,image,"strokePaint", FlatColor.class);
                } else {
                    image.setStrokePaint(null);
                }
                loadNumberAttribute(e,image,"strokeWidth");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        if(node instanceof SShape) {
            SShape shape = (SShape)node;
            if(e.hasAttr("fillPaint")) {
                loadFillPaint(e,shape,zipFile);
            } else {
                shape.setFillPaint(null);
            }

            if(e.hasAttr("strokePaint")) {
                loadFlatColorAttribute(e,node,"strokePaint", FlatColor.class);
            } else {
                ((SShape) node).setStrokePaint(null);
            }
            loadNumberAttribute(e,node,"strokeWidth");
            loadNumberAttribute(e,shape,"fillOpacity");
            loadShadow(e,shape);
        }

        if(node == null) {
            Log.warning("we couldn't recognize the resizable rect with type = ",e.attr("type"));
            return null;
        }
        loadNumberAttribute(e,node,"x");
        loadNumberAttribute(e,node,"y");
        loadNumberAttribute(e,node,"translateX");
        loadNumberAttribute(e,node,"translateY");
        loadNumberAttribute(e,node,"width");
        loadNumberAttribute(e,node,"height");
        loadStringAttribute(e,node,"id");

        loadProperties(e, (SNode) node);

        if(e.attrEquals("type","grid9")) {
            List<SNode> nodes = new ArrayList<SNode>();
            for(Elem element : e.xpath("*")) {
                SNode nd = loadAnyShape(element,zipFile);
                if(nd != null) nodes.add(nd);
            }

            ((ResizableGrid9Shape)node).setNodes(nodes);
        }
        return node;
    }

    private static void loadShadow(Elem e, SShape shape) throws XPathExpressionException {
        if(e.xpath("shadow").iterator().hasNext()) {
            Elem shadow = e.xpath("shadow").iterator().next();
            shape.setShadow(new DropShadow()
                    .setColor(new FlatColor(shadow.attr("color")))
                    .setBlurRadius(Integer.parseInt(shadow.attr("radius")))
                    .setOpacity(Double.parseDouble(shadow.attr("opacity")))
                    .setXOffset(Double.parseDouble(shadow.attr("xOffset")))
                    .setYOffset(Double.parseDouble(shadow.attr("yOffset")))
                    );
        }
    }

    private static void loadPolyPoints(Elem e, SPoly sPoly) throws XPathExpressionException {
        for(Elem element : e.xpath("point")) {
            sPoly.addPoint(new Point2D.Double(
                    Double.parseDouble(element.attr("x")),
                    Double.parseDouble(element.attr("y"))
            ));
        }
    }

    private static void loadBooleanAttribute(Elem e, Object node, String name) {
        String value = e.attr(name);
        boolean bool = Boolean.parseBoolean(value);
        try {
            Method method = node.getClass().getMethod(
                    "set"+name.substring(0,1).toUpperCase()+name.substring(1),
                    boolean.class);
            method.invoke(node,bool);
        } catch (Exception e1) {
            Log.error(e1);
        }
    }

    private static void loadStringAttribute(Elem e, Object node, String name) {
        if(!e.hasAttr(name)) return;
        String value = e.attr(name);
        try {
            Method method = node.getClass().getMethod(
                    "set"+name.substring(0,1).toUpperCase()+name.substring(1),
                    String.class);
            method.invoke(node,value);
        } catch (Exception e1) {
            Log.error(e1);
        }
    }

    private static void loadEnumAttribute(Elem e, Object node, String name, Class<? extends java.lang.Enum> enumClass) {
        if(!e.hasAttr(name)) return;
        String value = e.attr(name);
        Enum en = Enum.valueOf(enumClass,value);
        try {
            Method method = node.getClass().getMethod(
                    "set" + name.substring(0, 1).toUpperCase() + name.substring(1),
                    en.getClass());
            method.invoke(node,en);
        } catch (Exception e1) {
            Log.error(e1);
        }


    }

    private static void loadNumberAttribute(Elem e, Object node, String name) {
        if(!e.hasAttr(name)) return;
        String value = e.attr(name);
        Double dval = Double.parseDouble(value);
        try {
            Method method = node.getClass().getMethod(
                    "set"+name.substring(0,1).toUpperCase()+name.substring(1),
                    double.class);
            method.invoke(node,dval);
        } catch (Exception e1) {
            Log.error(e1);
        }
    }

    private static void loadIntegerAttribute(Elem e, Object shape, String name) {
        String value = e.attr(name);
        Integer ival = Integer.parseInt(value);
        try {
            String methodName = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
            Method method = shape.getClass().getMethod(methodName, int.class);
            method.invoke(shape,ival);
        } catch (Exception e1) {
            Log.error(e1);
        }
    }

    private static void loadFlatColorAttribute(Elem e, Object node, String name, Class clazz) {
        String value = e.attr(name);
        FlatColor fc = new FlatColor(value);
        try {
            String methodName = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
            Method method = node.getClass().getMethod(methodName, clazz);
            method.invoke(node,fc);
        } catch (Exception e1) {
            Log.error(e1);
        }

    }

    private class ClassFilterLogCollector extends Log.LogCollector {
        private Class clss;
        private List<Log.LogEvent> infos;
        private List<Log.LogEvent> warnings;
        private List<Log.LogEvent> errors;
        private List<Log.LogEvent> events;

        public ClassFilterLogCollector(Class clss) {
            super();
            this.clss = clss; 
            infos = new ArrayList<Log.LogEvent>();
            warnings = new ArrayList<Log.LogEvent>();
            errors = new ArrayList<Log.LogEvent>();
            events = new ArrayList<Log.LogEvent>();
        }

        @Override
        public void info(Log.LogEvent evt) {
            if(clss.getName().equals(evt.getReportingClass())) {
                infos.add(evt);
                events.add(evt);
            }
        }

        @Override
        public void warning(Log.LogEvent evt) {
            if(clss.getName().equals(evt.getReportingClass())) {
                warnings.add(evt);
                events.add(evt);
            }
        }

        @Override
        public void error(Log.LogEvent evt) {
            if(clss.getName().equals(evt.getReportingClass())) {
                errors.add(evt);
                events.add(evt);
            }
        }

        @Override
        public boolean hasInfo() {
            return !infos.isEmpty();
        }

        @Override
        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        @Override
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        @Override
        public List<Log.LogEvent> getEvents() {
            return events;
        }
    }
}
