package org.joshy.sketch.actions;

import com.joshondesign.xml.Doc;
import com.joshondesign.xml.Elem;
import com.joshondesign.xml.XMLParser;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.io.NativeExport;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.modes.preso.PresoModeHelper;
import org.joshy.sketch.modes.vector.VectorModeHelper;

import javax.imageio.ImageIO;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OpenAction extends SAction {
    private File specificFile;
    private DocContext context;

    public OpenAction(DocContext context) {
        this.context = context;
    }

    public OpenAction(DocContext context, File specificFile) {
        this.context = context;
        this.specificFile = specificFile;
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
            FileDialog fd = new FileDialog((Frame)context.getStage().getNativeWindow());
            fd.setMode(FileDialog.LOAD);
            fd.setTitle("Open Sketchy File");
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
    }


    private void load(File file) throws Exception {
        if(file.getName().toLowerCase().endsWith(".png")) {
            loadPng(file);
        } else {
            SketchDocument doc = load(new FileInputStream(file), file, file.getName());
            if(doc.isPresentation()) {
                context.getMain().setupNewDoc(new PresoModeHelper(context.getMain()),doc);
            } else {
                context.getMain().setupNewDoc(new VectorModeHelper(context.getMain()),doc);
            }
        }
    }

    private void loadPng(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            PixelDocument doc = new PixelDocument(img);
            context.setDocument(doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SketchDocument load(InputStream in, File file, String fileName) throws Exception {
        Doc doc = XMLParser.parse(in);
        if(file != null) {
            doc.setBaseURI(file.toURI());
        }
        SketchDocument sdoc = new SketchDocument();
        sdoc.removePage(sdoc.getCurrentPage());
        int version = Integer.parseInt(doc.xpathString("/sketchy/@version"));

        u.p("version = " + version);
        if(version < NativeExport.CURRENT_VERSION) {
            doc = upgradeDocument(doc);
        }
        for(Elem e : doc.xpath("/sketchy/page")) {
            loadPage(sdoc,e);
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
        }

        sdoc.setFile(file);
        sdoc.setTitle(fileName+"foo");
        //if(context.getStage() != null) {
        //            context.getStage().setTitle(fileName+"foo");
        //        }
        sdoc.setCurrentPage(0);
        sdoc.setDirty(false);
        return sdoc;
    }

    private Doc upgradeDocument(Doc doc) {
        URL upgradeXSL = NativeExport.class.getResource("upgrade_-1_0.xsl");
        //u.p("upgrade XSL = " + upgradeXSL);
        try {
            u.p("upgrading document to version 0");
            doc = XMLParser.translate(doc,upgradeXSL.toURI());
            //doc.dump();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    private void loadPage(SketchDocument sdoc, Elem epage) throws XPathExpressionException {
        SketchDocument.SketchPage page = sdoc.addPage();
        for(Elem e : epage.xpath("./*")) {
            page.add(loadAnyShape(e));
        }
    }

    private static SNode loadAnyShape(Elem e) throws XPathExpressionException {
        if("group".equals(e.name())) {
            return loadGroup(e);
        }
        if("node".equals(e.name())) {
            return loadNode(e);
        }
        if("shape".equals(e.name())) {
            return loadShape(e);
        }
        if("resizeableNode".equals(e.name())) {
            return (SNode) loadResizableNode(e);
        }
        return null;
    }

    public static List<SNode> loadShapes(File file) throws Exception {
        Doc doc = XMLParser.parse(new FileInputStream(file));
        doc.setBaseURI(file.toURI());
        List<SNode> shapes = new ArrayList<SNode>();
        for(Elem e : doc.xpath("/sketchy/*")) {
            shapes.add(loadAnyShape(e));
        }
        return shapes;
    }

    private static SNode loadGroup(Elem elem) throws XPathExpressionException {
        SGroup group = new SGroup();
        List<SNode> nodes = new ArrayList<SNode>();
        
        for(Elem element : elem.xpath("*")) {
            SNode node = loadAnyShape(element);
            if(node != null) nodes.add(node);
        }
        
        group.addAll(nodes);
        loadNumberAttribute(elem,group,"translateX");
        loadNumberAttribute(elem,group,"translateY");
        loadProperties(elem,group);
        return group;
    }

    private static SNode loadNode(Elem e) throws XPathExpressionException {
        SNode node = null;
        if(e.attrEquals("type","image")) {
            try {
                node = new SImage(e.getDoc().getBaseURI(),"resources/"+e.attr("relativeURL"));
                loadNumberAttribute(e,node,"translateX");
                loadNumberAttribute(e,node,"translateY");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        loadProperties(e,node);
        return node;
    }

    private static SNode loadShape(Elem e) throws XPathExpressionException {
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
        if(e.hasAttr("fillPaint")) {
            loadFlatColorAttribute(e,shape,"fillPaint", Paint.class);
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

        loadProperties(e,shape);
        return shape;
    }

    private static void loadProperties(Elem e, SNode shape) throws XPathExpressionException {
        for(Elem element : e.xpath("property")) {
            shape.setStringProperty(
                    element.attr("name"),
                    element.attr("value"));
        }
    }

    private static SResizeableNode loadResizableNode(Elem e) throws XPathExpressionException {
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
        }
        if(e.attrEquals("type","image")) {
            try {
                node = new SImage(e.getDoc().getBaseURI(),"resources/"+e.attr("relativeURL"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        if(node instanceof SShape) {
            loadFlatColorAttribute(e,node,"fillPaint", Paint.class);
            if(e.hasAttr("strokePaint")) {
                loadFlatColorAttribute(e,node,"strokePaint", FlatColor.class);
            } else {
                ((SShape) node).setStrokePaint(null);
            }
            loadNumberAttribute(e,node,"strokeWidth");
        }

        loadNumberAttribute(e,node,"x");
        loadNumberAttribute(e,node,"y");
        loadNumberAttribute(e,node,"translateX");
        loadNumberAttribute(e,node,"translateY");
        loadNumberAttribute(e,node,"width");
        loadNumberAttribute(e,node,"height");

        loadProperties(e, (SNode) node);

        if(e.attrEquals("type","grid9")) {
            List<SNode> nodes = new ArrayList<SNode>();
            for(Elem element : e.xpath("*")) {
                SNode nd = loadAnyShape(element);
                if(nd != null) nodes.add(nd);
            }

            ((ResizableGrid9Shape)node).setNodes(nodes);
        }
        return node;
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
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
    }

    private static void loadStringAttribute(Elem e, Object node, String name) {
        String value = e.attr(name);
        try {
            Method method = node.getClass().getMethod(
                    "set"+name.substring(0,1).toUpperCase()+name.substring(1),
                    String.class);
            method.invoke(node,value);
        } catch (Exception e1) {
            e1.printStackTrace();
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
            e1.printStackTrace();
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
            e1.printStackTrace();
        }
    }

    private static void loadIntegerAttribute(Elem e, Object shape, String name) {
        String value = e.attr(name);
        Integer ival = Integer.parseInt(value);
        try {
            String methodName = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
//            u.p("looking for method: " + methodName + " with args: " + value);
//            u.p("object = " + shape);
            Method method = shape.getClass().getMethod(methodName, int.class);
            method.invoke(shape,ival);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private static void loadFlatColorAttribute(Elem e, Object node, String name, Class clazz) {
        String value = e.attr(name);
        //u.p("looking at value: "+ value + " in object: " + node + " from element: " + e + " " + e.attr("type"));
        FlatColor fc = new FlatColor(value);
        try {
            String methodName = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
            //u.p("looking for method: " + methodName + " with args: " + clazz + " on object " + node);
            Method method = node.getClass().getMethod(methodName, clazz);
            method.invoke(node,fc);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }
}