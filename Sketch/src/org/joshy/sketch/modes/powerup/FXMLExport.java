package org.joshy.sketch.modes.powerup;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.LinearGradientFill;
import org.joshy.gfx.draw.MultiGradientFill;
import org.joshy.gfx.draw.RadialGradientFill;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.*;
import org.joshy.sketch.util.ExportUtils;

import javax.imageio.ImageIO;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 4/4/12
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
class FXMLExport implements ShapeExporter<XMLWriter> {
    private static DecimalFormat df = new DecimalFormat();
    private File outdir;
    private File imagesdir;
    private SketchDocument.SketchPage currentPage;

    public FXMLExport(File outdir) {
        this.outdir = outdir;
        imagesdir = new File(outdir,"images");
        imagesdir.mkdirs();
    }

    static {
        df.setMaximumFractionDigits(2);
    }

    public void docStart(XMLWriter out, SketchDocument doc) {
        out.header();

        out.text("<?import java.lang.*?>\n");
        out.text("<?import javafx.collections.*?>\n");
        out.text("<?import javafx.scene.*?>\n");
        out.text("<?import javafx.scene.control.*?>\n");
        out.text("<?import javafx.scene.effect.*?>\n");
        out.text("<?import javafx.scene.image.*?>\n");
        out.text("<?import javafx.scene.layout.*?>\n");
        out.text("<?import javafx.scene.paint.*?>\n");
        out.text("<?import javafx.scene.shape.*?>\n");
        out.text("<?import javafx.scene.text.*?>\n");
        out.text("\n");
        out.text("\n");

    }

    public void pageStart(XMLWriter out, SketchDocument.SketchPage page) {
        //out.start("Group");
        out.start("AnchorPane");
        out.attr("xmlns:fx", "http://javafx.com/fxml");
        out.attr("prefWidth", df.format(page.getDocument().getWidth()));
        out.attr("prefHeight",df.format(page.getDocument().getHeight()));
        currentPage = page;
    }

    public void exportPre(XMLWriter out, SNode node) {

        //xml start types
        if(node instanceof SRect) out.start("Rectangle");
        if(node instanceof SOval) out.start("Ellipse");
        if(node instanceof NGon)  out.start("Path");
        if(node instanceof SPoly) out.start("Path");
        if(node instanceof SPath) out.start("Path");
        if(node instanceof SText) out.start("Text");
        if(node instanceof SImage) out.start("ImageView");

        if(node instanceof FXComponent) out.start(((FXComponent) node).getXMLElementName());


        if(node instanceof SGroup) return;
        if(node instanceof SArrow) return;
        if(node instanceof SArea) return;

        //custom attributes
        if(node instanceof SResizeableNode) {
            setResizableNodeAttributes(out,node);
        }

        if(node instanceof SRect) {
            SRect rect = (SRect) node;
            if(rect.getCorner() > 0) {
                out.attr("arcWidth",df.format(rect.getCorner()));
                out.attr("arcHeight",df.format(rect.getCorner()));
            }
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
            if(shape.getFillOpacity() < 1) {
                out.attr("opacity",df.format(shape.getFillOpacity()));
            }
        }


        if(!(node instanceof FXComponent)) {
            out.attr("translateX",df.format(node.getTranslateX()));
            out.attr("translateY",df.format(node.getTranslateY()));
            out.attr("rotate",df.format(node.getRotate()));
            out.attr("scaleX",df.format(node.getScaleX()));
            out.attr("scaleY",df.format(node.getScaleY()));
        }

        if(node instanceof FXComponent) {
            FXComponent fxcomp = (FXComponent) node;
            fxcomp.exportAttributes(out);
        }


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
            if(shape.getFillPaint() instanceof RadialGradientFill) {
                RadialGradientFill fill = (RadialGradientFill) shape.getFillPaint();
                out.start("fill").start("RadialGradient")
                        .attr("centerX",df.format(fill.getCenterX()))
                        .attr("centerY",df.format(fill.getCenterY()))
                        .attr("radius",df.format(fill.getRadius()))
                        .attr("proportional","false");
                out.start("stops");
                for(MultiGradientFill.Stop stop: fill.getStops()) {
                    out.start("Stop")
                            .attr("offset",df.format(stop.getPosition()))
                            .attr("color",ExportUtils.toRGBAHexString(stop.getColor()))
                            .end();

                }
                out.end(); // stops
                out.end(); // RadialGradient
                out.end(); // fill
            }
        }


        if(node instanceof SImage) {
            SImage image = (SImage) node;
            out.start("image");
            out.start("Image")
                .attr("url", "@images/" + image.getRelativeURL());

            File imageFile = new File(imagesdir,image.getRelativeURL());
            try {
                ImageIO.write(image.getBufferedImage(), "PNG", imageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            out.end(); // Image
            out.end(); // image
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
        
        if(node instanceof FXComponent) {
            FXComponent comp = (FXComponent) node;
            if("ChoiceBox".equals(comp.getXMLElementName())) {
                out.start("items")
                        .start("FXCollections").attr("fx:factory","observableArrayList")
                        .start("String").attr("fx:value","item1").end()
                        .start("String").attr("fx:value","item2").end()
                        .end()
                    .end();
            }
        }
        
        //effects
        if(node instanceof SShape) {
            SShape shape = (SShape) node;
            if(shape.getShadow() != null) {
                DropShadow sh = shape.getShadow();
                out.start("effect");
                out.start("DropShadow")
                        .attr("offsetX", df.format(sh.getXOffset()))
                        .attr("offsetY", df.format(sh.getYOffset()))
                        .attr("color",ExportUtils.toHexString(sh.getColor()))
                        .attr("radius",df.format(sh.getBlurRadius()))
                        .end();
                out.end();
            }
        }

        out.end();
    }

    private void setResizableNodeAttributes(XMLWriter out, SNode node) {
        if(node instanceof FXComponent) {
            FXAbstractComponent b = (FXAbstractComponent) node;
            if(b.leftAnchored) {
                out.attr("AnchorPane.leftAnchor",df.format(b.getX()+b.getTranslateX()));
            }
            if(b.topAnchored) {
                out.attr("AnchorPane.topAnchor", df.format(b.getY()+b.getTranslateY()));
            }
            if(b.rightAnchored) {
                double r = b.getX()+b.getTranslateX()+b.getWidth();
                double w = currentPage.getDocument().getWidth();
                out.attr("AnchorPane.rightAnchor",df.format(w-r));
            }
            if(b.bottomAnchored) {
                double bot = b.getY()+b.getTranslateY()+b.getHeight();
                double h = currentPage.getDocument().getHeight();
                out.attr("AnchorPane.bottomAnchor", df.format(h-bot));
            }
            return;
        }
        if(node instanceof SOval) {
            SOval oval = (SOval) node;
            out.attr("centerX", df.format(oval.getX() + oval.getWidth() / 2));
            out.attr("centerY",df.format(oval.getY()+oval.getHeight()/2));
            out.attr("radiusX",df.format(oval.getWidth()/2));
            out.attr("radiusY",df.format(oval.getHeight()/2));
            return;
        }

        if(node instanceof SImage) {
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
