package org.joshy.sketch.actions.io;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.draw.*;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.*;

import javax.imageio.ImageIO;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NativeExport implements ShapeExporter<XMLWriter> {
    public static final int CURRENT_VERSION = 0;
    private boolean delayedImageWriting = false;
    private List delayedImages = new ArrayList();

    public void docStart(XMLWriter out, SketchDocument doc) {
        out.header();
        out.start("sketchy","version",""+CURRENT_VERSION);

        //document info
        out.start("info");
        if(doc.isPresentation()){
            out.attr("type","presentation");
        } else {
            out.attr("type","generic-drawing");
        }
        if(doc.getBackgroundFill() instanceof FlatColor) {
            saveAttribute(out,"backgroundFill",doc);
        } else {
            out.attr("backgroundFill","patternPaint");
        }
        saveBooleanAttribute(out,"gridActive",doc);

        if(!doc.getProperties().isEmpty()) {
            for(Map.Entry entry : (Set<Map.Entry>)doc.getProperties().entrySet()) {
                out.start("property","name",""+entry.getKey(),"value",""+entry.getValue());
                out.end();
            }
        }
        if(doc.getBackgroundFill() instanceof PatternPaint) {
            PatternPaint pt = (PatternPaint) doc.getBackgroundFill();
            savePatternPaint(out, pt);
        }
        out.end();
    }

    private void savePatternPaint(XMLWriter out, PatternPaint pattern) {
        u.p("saving a pttern. url = " + pattern.getRelativeURL());
        out.start("patternPaint")
                .attr("startX",""+pattern.getStart().getX())
                .attr("startY",""+pattern.getStart().getY())
                .attr("endX",""+pattern.getEnd().getX())
                .attr("endY",""+pattern.getEnd().getY())
                .attr("relativeURL", ""+pattern.getRelativeURL())
        ;
        out.end();
        if(delayedImageWriting) {
            delayedImages.add(pattern);
        } else {
            try {
                saveRelativeImage(out,pattern);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void pageStart(XMLWriter out, SketchDocument.SketchPage page) {
        out.start("page");
        out.start("guidelines");
        for(SketchDocument.Guideline gl : page.getGuidelines()) {
            out.start("guideline")
                    .attr("position",""+gl.getPosition())
                    .attr("vertical",""+gl.isVertical())
                    .end();
        }
        out.end();
    }

    public void exportPre(XMLWriter out, SNode shape) {
        if(shape instanceof SGroup) {
            SGroup group = (SGroup) shape;
            out.start("group");
            saveAttribute(out,"translateX",group);
            saveAttribute(out,"translateY",group);
            exportProperties(out,shape);
            return;
        }
        if(shape instanceof SResizeableNode) {
            out.start("resizeableNode");
        } else if(shape instanceof SShape) {
            out.start("shape");
        } else {
            out.start("node");
        }

        if(shape.getId() != null && !"".equals(shape.getId())) {
            out.attr("id",shape.getId());
        }
        if(shape instanceof SImage) {
            out.attr("type","image");
        }
        if(shape instanceof SRect) {
            out.attr("type","rect");
        }
        if(shape instanceof SOval) {
            out.attr("type","oval");
        }
        if(shape instanceof ResizableGrid9Shape) {
            out.attr("type","grid9");
        }
        if(shape instanceof SText) {
            out.attr("type","text");
        }
        if(shape instanceof SPoly) {
            out.attr("type","poly");
        }
        if(shape instanceof SPath) {
            out.attr("type","path");
        }
        if(shape instanceof NGon) {
            out.attr("type","ngon");
        }
        if(shape instanceof SArea) {
            out.attr("type","area");
        }

        if(shape instanceof SImage) {
            saveAttribute(out,"strokePaint",shape);
            saveAttribute(out,"strokeWidth",shape);
            saveAttribute(out,"relativeURL",shape);
            if(delayedImageWriting) {
                delayedImages.add((SImage)shape);
            } else {
                try {
                    saveRelativeImage(out,(SImage)shape);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if(shape instanceof SShape) {
            SShape sh = (SShape) shape;
            if(sh.getFillPaint() instanceof LinearGradientFill) {
                out.attr("fillPaint","linearGradient");
            }
            if(sh.getFillPaint() instanceof RadialGradientFill) {
                out.attr("fillPaint","radialGradient");
            }
            if(sh.getFillPaint() instanceof GradientFill) {
                out.attr("fillPaint","gradient");
            }
            if(sh.getFillPaint() instanceof PatternPaint) {
                out.attr("fillPaint","patternPaint");
            }
            if(sh.getFillPaint() instanceof FlatColor) {
                saveAttribute(out,"fillPaint",shape);
            }
            saveAttribute(out,"fillOpacity",shape);
            saveAttribute(out,"strokePaint",shape);
            saveAttribute(out,"strokeWidth",shape);
        }
        
        saveAttribute(out,"translateX",shape);
        saveAttribute(out,"translateY",shape);

        if(shape instanceof SResizeableNode) {
            saveAttribute(out,"x",shape);
            saveAttribute(out,"y",shape);
            saveAttribute(out,"width",shape);
            saveAttribute(out,"height",shape);
        }

        if(shape instanceof SRect) {
            saveAttribute(out,"corner",shape);
        }
        
        if(shape instanceof ResizableGrid9Shape) {
            saveAttribute(out,"top",shape);
            saveAttribute(out,"bottom",shape);
            saveAttribute(out,"left",shape);
            saveAttribute(out,"right",shape);
            saveAttribute(out,"originalWidth",shape);
            saveAttribute(out,"originalHeight",shape);
            saveBooleanAttribute(out,"vLocked",shape);
            saveBooleanAttribute(out,"hLocked",shape);
        }

        if(shape instanceof SText) {
            saveAttribute(out,"fontSize",shape);
            saveAttribute(out,"weight",shape);
            saveAttribute(out,"style",shape);
            saveAttribute(out,"halign",shape);
            saveAttribute(out,"fontName",shape);
            saveBooleanAttribute(out,"autoSize",shape);
            saveBooleanAttribute(out,"bulleted",shape);
        }

        if(shape instanceof SPoly) {
            saveBooleanAttribute(out,"closed",shape);
        }

        if(shape instanceof SPath) {
            saveBooleanAttribute(out,"closed",shape);
        }

        if(shape instanceof NGon) {
            saveAttribute(out,"radius",shape);
            saveAttribute(out,"sides",shape);
            saveAttribute(out,"angle",shape);
        }



        if(shape instanceof SShape) {
            SShape sh = (SShape) shape;

            if(sh.getFillPaint() instanceof GradientFill) {
                GradientFill grad = (GradientFill) sh.getFillPaint();
                out.start("gradient");
                out.attr("angle",""+grad.angle);
                out.start("stop"
                        ,"name","start"
                        ,"color",serialize(grad.start)
                        ).end();
                out.start("stop"
                        ,"name","end"
                        ,"color",serialize(grad.end)
                        ).end();
                out.end();
            }
            if(sh.getFillPaint() instanceof LinearGradientFill) {
                LinearGradientFill grad = (LinearGradientFill) sh.getFillPaint();
                out.start("linearGradient")
                        .attr("startX", "" + grad.getStartX())
                        .attr("startY", "" + grad.getStartY())
                        .attr("endX", "" + grad.getEndX())
                        .attr("endY", "" + grad.getEndY())
                        .attr("startXSnapped",""+grad.getStartXSnapped())
                        .attr("startYSnapped",""+grad.getStartYSnapped())
                        .attr("endXSnapped",""+grad.getEndXSnapped())
                        .attr("endYSnapped",""+grad.getEndYSnapped())
                ;
                for(MultiGradientFill.Stop stop : grad.getStops()) {
                    out.start("stop")
                            .attr("position",""+stop.getPosition())
                            .attr("color",serialize(stop.getColor())).end();

                }
                out.end();
            }
            if(sh.getFillPaint() instanceof RadialGradientFill) {
                RadialGradientFill grad = (RadialGradientFill) sh.getFillPaint();
                out.start("radialGradient")
                        .attr("centerX", "" + grad.getCenterX())
                        .attr("centerY", "" + grad.getCenterY())
                        .attr("radius", "" + grad.getRadius())
                ;
                for(MultiGradientFill.Stop stop : grad.getStops()) {
                    out.start("stop")
                            .attr("position",""+stop.getPosition())
                            .attr("color",serialize(stop.getColor())).end();

                }
                out.end();
            }
            if(sh.getFillPaint() instanceof PatternPaint) {
                PatternPaint pattern = (PatternPaint) sh.getFillPaint();
                savePatternPaint(out,pattern);
            }
            if(sh.getShadow() != null) {
                DropShadow shadow = sh.getShadow();
                out.start("shadow")
                    .attr("radius",""+shadow.getBlurRadius())
                    .attr("color",serialize(shadow.getColor()))
                        .attr("opacity",""+shadow.getOpacity())
                        .attr("xOffset",""+shadow.getXOffset())
                        .attr("yOffset",""+shadow.getYOffset())
                    .end();
            }
        }


        if(shape instanceof SPoly) {
            SPoly poly = (SPoly) shape;
            for(int i=0; i<poly.pointCount(); i++) {
                out.start("point","x",""+poly.getPoint(i).getX(),"y",""+poly.getPoint(i).getY());
                out.end();
            }
        }
        if(shape instanceof SPath) {
            SPath path = (SPath) shape;
            for(SPath.PathPoint pt : path.getPoints()) {

                out.start("pathpoint");
                out.attr("x",""+pt.x);
                out.attr("y",""+pt.y);
                out.attr("cx1",""+pt.cx1);
                out.attr("cy1",""+pt.cy1);
                out.attr("cx2",""+pt.cx2);
                out.attr("cy2",""+pt.cy2);
                out.end();
            }
        }
        if(shape instanceof SArea) {
            SArea area = (SArea) shape;
            Area jarea = area.toArea();
            PathIterator it = jarea.getPathIterator(null);
            while(!it.isDone()) {
                double[] coords = new double[6];
                int n = it.currentSegment(coords);
                if(n == PathIterator.SEG_MOVETO) {
                    out.start("move","x",""+coords[0],"y",""+coords[1]).end();
                }
                if(n == PathIterator.SEG_LINETO) {
                    out.start("lineto","x",""+coords[0],"y",""+coords[1]).end();
                }
                if(n == PathIterator.SEG_CUBICTO) {
                    out.start("curveto",
                            "cx1",""+coords[0],"cy1",""+coords[1],
                            "cx2",""+coords[2],"cy2",""+coords[3],
                            "x2",""+coords[4],"y2",""+coords[5]
                    ).end();
                }
                if(n == PathIterator.SEG_CLOSE) {
                    out.start("close").end();
                    break;
                }
                it.next();
            }
        }
        exportProperties(out,shape);

    }

    private void saveRelativeImage(XMLWriter out, PatternPaint pattern) throws IOException {
        BufferedImage img = pattern.getImage();
        URI baseURI = out.getBaseURI();
        u.p("document URI = " + baseURI);
        URI imageURI = baseURI.resolve("resources/"+pattern.getRelativeURL());
        u.p("image URI = " + imageURI);
        File imageFile = new File(imageURI);
        if(!imageFile.getParentFile().exists()) {
            imageFile.getParentFile().mkdir();
        }
        ImageIO.write(img,"png",imageFile);
        u.p("wrote out image");
    }

    private void saveRelativeImage(XMLWriter out, SImage image) throws URISyntaxException, IOException {
        BufferedImage img = image.getBufferedImage();
        File imgFile = image.getFile();

        URI baseURI = out.getBaseURI();
        u.p("document URI = " + baseURI);
        URI imageURI = baseURI.resolve("resources/"+imgFile.getName());
        u.p("image URI = " + imageURI);
        File imageFile = new File(imageURI);
        if(!imageFile.getParentFile().exists()) {
            imageFile.getParentFile().mkdir();
        }
        ImageIO.write(img,"png",imageFile);
        u.p("wrote out image");
    }

    private void exportProperties(XMLWriter out, SNode shape) {
        if(!shape.getProperties().isEmpty()) {
            for(Map.Entry entry : (Set<Map.Entry>)shape.getProperties().entrySet()) {
                out.start("property","name",""+entry.getKey(),"value",""+entry.getValue());
                out.end();
            }
        }
    }

    public void exportPost(XMLWriter out, SNode shape) {
        if(shape instanceof SText) {
            SText text = (SText) shape;
            out.start("text");
            out.text(text.getText());
            out.end();
        }

        out.end();
    }

    public void pageEnd(XMLWriter out, SketchDocument.SketchPage page) {
        out.end();
    }

    public void docEnd(XMLWriter out, SketchDocument document) {
        out.end();
    }

    public boolean isContainer(SNode n) {
        if(n instanceof SGroup) return true;
        if(n instanceof ResizableGrid9Shape) return true;
        return false;
    }

    public Iterable<? extends SNode> getChildNodes(SNode n) {
        if(n instanceof SGroup) return ((SGroup)n).getNodes();
        if(n instanceof ResizableGrid9Shape) return ((ResizableGrid9Shape)n).getNodes();
        return null;
    }

    private static void saveAttribute(XMLWriter out, String name, Object node) {
        try {
            Method method = node.getClass().getMethod("get"+name.substring(0,1).toUpperCase()+name.substring(1));
            Object value = method.invoke(node);
            if(value != null) {
                out.attr(name,serialize(value));
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    private static void saveBooleanAttribute(XMLWriter out, String name, Object node) {
        try {
            Method method = node.getClass().getMethod("is"+name.substring(0,1).toUpperCase()+name.substring(1));
            Object value = method.invoke(node);
            if(value != null) {
                out.attr(name,serialize(value));
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static String serialize(Object value) {
        if(value instanceof FlatColor) {
            return "#"+Integer.toHexString(((FlatColor)value).getRGBA());
        }
        return ""+value;
    }

    public void setDelayedImageWriting(boolean delayedImageWriting) {
        this.delayedImageWriting = delayedImageWriting;
    }

    public List getDelayedImages() {
        return delayedImages;
    }
}
