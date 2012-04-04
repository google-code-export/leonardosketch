package org.joshy.sketch.modes.powerup;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.LinearGradientFill;
import org.joshy.gfx.draw.MultiGradientFill;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.util.ExportUtils;
import org.joshy.sketch.util.Util;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 3/31/12
 * Time: 4:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class FXMLPowerup extends Powerup {
    @Override
    public CharSequence getMenuName() {
        return "FXML App";
    }

    @Override
    public void enable(DocContext context) {
        u.p("enabling the javafx fxml power up");
        context.getFileMenu().addItem("Run as JavaFX", new RunAsJavaFX(context));
        context.redraw();
    }
}


class RunAsJavaFX extends SAction {
    private DocContext context;

    public RunAsJavaFX(DocContext context) {
        this.context = context;
    }

    @Override
    public void execute() throws Exception {
        u.p("running as JavaFX FXML App");
        context.addNotification("Generating JavaFX App");

        //make temp dir
        File tempdir = Util.makeTempDir();
        //make temp subdir
        File appdir = new File(tempdir,"testapp");
        appdir.mkdirs();

        //copy template to temp subdir
        File templatedir = new File("/Users/josh/projects/javafx/FXMLTemplate/");
        Map<String,String> keys = new HashMap<String, String>();
        Util.copyTemplate(templatedir, appdir, keys);

        File fxmlfile = new File(appdir,"src/fxmltemplate/Generated.fxml");
        try {
            u.p("generating: " + fxmlfile);
            XMLWriter out = new XMLWriter(fxmlfile);
            ExportProcessor.process(new FXMLExport(), out, (SketchDocument) context.getDocument());
            out.close();
        } catch (Exception ex) {
            u.p(ex);
        }
        
        //execute ant script to run on the roku, passing in the IP addr on the commandline
        List<String> args = new ArrayList<String>();
        args.add("ant");
        args.add("-f");
        args.add(new File(appdir,"build.xml").getAbsolutePath());
        args.add("clean");
        args.add("run");
        Process proc = Runtime.getRuntime().exec(
                args.toArray(new String[0]),
                new String[0],
                appdir
        );
        Util.streamToSTDERR(proc.getInputStream());
        context.addNotification("Compiling and Launching JavaFX App");
    }

}

class FXMLExport implements ShapeExporter<XMLWriter> {
    private static DecimalFormat df = new DecimalFormat();
    static {
        df.setMaximumFractionDigits(2);
    }

    public void docStart(XMLWriter out, SketchDocument doc) {
        out.header();

        out.text("<?import java.lang.*?>");
        out.text("<?import javafx.scene.*?>");
        out.text("<?import javafx.scene.control.*?>");
        out.text("<?import javafx.scene.layout.*?>\n");
        out.text("<?import javafx.scene.paint.*?>");
        out.text("<?import javafx.scene.shape.*?>");
        out.text("<?import javafx.scene.text.*?>");
        out.text("\n");
        out.text("\n");

    }

    public void pageStart(XMLWriter out, SketchDocument.SketchPage page) {
        out.start("Group");
    }

    public void exportPre(XMLWriter out, SNode node) {

        //xml start types
        if(node instanceof SRect) out.start("Rectangle");
        if(node instanceof SOval) out.start("Ellipse");
        if(node instanceof NGon)  out.start("Path");
        if(node instanceof SPoly) out.start("Path");
        if(node instanceof SPath) out.start("Path");
        if(node instanceof SText) out.start("Text");

        //custom attributes
        if(node instanceof SResizeableNode) {
            setResizableNodeAttributes(out,node);
        }



        //general attributes
        if(node instanceof SShape) {
            SShape shape = (SShape) node;
            if(shape.getFillPaint() instanceof FlatColor) {
                out.attr("fill", ExportUtils.toHexString((FlatColor) shape.getFillPaint()));
            }
            if(shape.getStrokePaint() instanceof FlatColor) {
                out.attr("stroke", ExportUtils.toHexString(shape.getStrokePaint()));
            }
            if(shape.getStrokeWidth() > 0) {
                out.attr("strokeWidth",df.format(shape.getStrokeWidth()));
            }
        }


        out.attr("translateX",df.format(node.getTranslateX()));
        out.attr("translateY",df.format(node.getTranslateY()));
        out.attr("rotate",df.format(node.getRotate()));
        out.attr("scaleX",df.format(node.getScaleX()));
        out.attr("scaleY",df.format(node.getScaleY()));


        //nested children and sub properties
        
        if(node instanceof SShape) {
            SShape shape = (SShape) node;
            if(shape.getFillPaint() instanceof LinearGradientFill) {
                LinearGradientFill fill = (LinearGradientFill) shape.getFillPaint();
                out.start("fill").start("LinearGradient")
                        .attr("startX",df.format(fill.getStartX()))
                        .attr("startY",df.format(fill.getStartY()))
                        .attr("endX",df.format(fill.getEndX()))
                        .attr("endY",df.format(fill.getEndY()))
                        .attr("proportional","false");
                out.start("stops");
                for(MultiGradientFill.Stop stop: fill.getStops()) {
                    out.start("Stop")
                            .attr("offset",df.format(stop.getPosition()))
                            .attr("color",ExportUtils.toRGBAHexString(stop.getColor()))
                            .end();

                }
                out.end(); // stops
                out.end(); // LinearGradient
                out.end(); // fill

            }
        }
        
        if(node instanceof NGon) {
            toPathNode(out, ((NGon)node).toUntransformedArea(), 0,0 );
        }
        if(node instanceof SPoly) {
            serializePath(out, (SPoly) node);
        }
        if(node instanceof SPath) {
            serializePath(out, (SPath) node);
        }
        if(node instanceof SText) {
            SText text = (SText) node;
            out.start("font")
                .start("Font")
                .attr("size", df.format(text.getFontSize()))
                .attr("name", text.getFontName())
                .end()
            .end();
        }

        out.end();
    }

    private void setResizableNodeAttributes(XMLWriter out, SNode node) {
        if(node instanceof SOval) {
            SOval oval = (SOval) node;
            out.attr("centerX", df.format(oval.getX() + oval.getWidth() / 2));
            out.attr("centerY",df.format(oval.getY()+oval.getHeight()/2));
            out.attr("radiusX",df.format(oval.getWidth()/2));
            out.attr("radiusY",df.format(oval.getHeight()/2));
            return;
        }

        if(node instanceof SText) {
            SText text = (SText) node;
            out.attr("text",((SText)node).getText());
            out.attr("x",""+text.getX());
            out.attr("y", "" + text.getY());
            if(text.isWrapText()) {
                out.attr("wrappingWidth",df.format(text.getWidth()));
            }
            return;
        }
        
        SResizeableNode rect = (SResizeableNode) node;
        out.attr("x",""+rect.getX())
                .attr("y", "" + rect.getY())
                .attr("width", "" + rect.getWidth())
                .attr("height", "" + rect.getHeight())
        ;
    }


    public void exportPost(XMLWriter out, SNode shape) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void pageEnd(XMLWriter out, SketchDocument.SketchPage page) {
        out.end();
    }

    public void docEnd(XMLWriter out, SketchDocument document) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isContainer(SNode n) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterable<? extends SNode> getChildNodes(SNode n) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }




    private void toPathNode(XMLWriter out, Area area, double xoff, double yoff) {
        double dx = xoff;
        double dy = yoff;
        PathIterator it = area.getPathIterator(null);
        generateElements(out, it, dx, dy);
    }

    private void serializePath(XMLWriter out, SPath node) {
        Path2D.Double j2dpath = SPath.toPath(node);
        PathIterator it = j2dpath.getPathIterator(null);
        generateElements(out, it, 0, 0);
    }

    private void generateElements(XMLWriter out, PathIterator it, double dx, double dy) {
        out.start("elements");
        while(!it.isDone()) {
            double[] coords = new double[6];
            int n = it.currentSegment(coords);
            if(n == PathIterator.SEG_MOVETO) {
                out.start("MoveTo")
                        .attr("x",df.format(coords[0]-dx))
                        .attr("y",df.format(coords[1]-dy))
                        .end();
            }
            if(n == PathIterator.SEG_LINETO) {
                out.start("LineTo")
                        .attr("x",df.format(coords[0]-dx))
                        .attr("y",df.format(coords[1]-dy))
                        .end();
            }
            if(n == PathIterator.SEG_CUBICTO) {
                out.start("CubicCurveTo")
                        .attr("controlX1",df.format(coords[0]-dx))
                        .attr("controlY1",df.format(coords[1]-dy))
                        .attr("controlX2",df.format(coords[2]-dx))
                        .attr("controlY2",df.format(coords[3]-dy))
                        .attr("x",df.format(coords[4]-dx))
                        .attr("y",df.format(coords[5]-dy))
                        .end();
                //out.println(".curveTo("+
                //       (coords[0]-dx)+","+(coords[1]-dy)+","+(coords[2]-dx)+","+(coords[3]-dy)+
                //       ","+(coords[4]-dx)+","+(coords[5]-dy)+")"
                //);
            }
            if(n == PathIterator.SEG_CLOSE) {
                //out.println(".closeTo()");
                out.start("ClosePath").end();
                break;
            }
            it.next();
        }
        out.end();
    }


    private void serializePath(XMLWriter out, SPoly path) {
        out.start("elements");
        //out.println("new Path()");
        List<Point2D> points = path.getPoints();
        for(int i=0; i<points.size(); i++) {
            Point2D pt = points.get(i);
            if(i == 0) {
                out.start("MoveTo")
                        .attr("x",df.format(pt.getX()))
                        .attr("y",df.format(pt.getY()))
                        .end();
            } else {
                out.start("LineTo")
                        .attr("x",df.format(pt.getX()))
                        .attr("y",df.format(pt.getY()))
                        .end();
            }
        }
        if(path.isClosed()) {
            //out.println(".closeTo()");
        }
        out.end();
    }
    
}
