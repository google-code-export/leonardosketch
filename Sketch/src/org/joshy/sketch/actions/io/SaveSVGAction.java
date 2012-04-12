package org.joshy.sketch.actions.io;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.draw.*;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;

public class SaveSVGAction extends BaseExportAction {
    private static DecimalFormat df = new DecimalFormat();
    static {
        df.setMaximumFractionDigits(2);
    }


    public SaveSVGAction(DocContext context) {
        super(context);
    }

    @Override
    protected String getStandardFileExtension() {
        return "svg";
    }

    public void export(File file, SketchDocument doc) {
        try {
            XMLWriter out = new XMLWriter(file);
            ExportProcessor.process(new SVGExport(), out, doc);
            out.close();
        } catch (Exception ex) {
            u.p(ex);
        }
    }

    private  static class SVGExport implements ShapeExporter<XMLWriter> {
        public void docStart(XMLWriter out, SketchDocument doc) {
            out.header();
            out.start("svg")
                    .attr("xmlns","http://www.w3.org/2000/svg")
                    .attr("version","1.2")
                    .attr("baseProfile","tiny");
            out.attr("viewBox","0 0 500 500");
        }

        public void pageStart(XMLWriter out, SketchDocument.SketchPage page) {
        }

        public void exportPre(XMLWriter out, SNode shape) {
            if(shape instanceof SGroup) {
                out.start("g")
                    .attr("transform","translate("+shape.getTranslateX()+","+shape.getTranslateY()+")");
            }
            if(shape instanceof SRect) draw(out,(SRect)shape);
            if(shape instanceof SArea) draw(out,(SArea)shape);
            if(shape instanceof SOval) draw(out,(SOval)shape);
            if(shape instanceof SText) draw(out,(SText)shape);
            if(shape instanceof SPoly) draw(out,(SPoly)shape);
            if(shape instanceof NGon)  draw(out, (NGon)shape);
            if(shape instanceof SPath) draw(out, (SPath)shape);
        }

        public void exportPost(XMLWriter out, SNode shape) {
            if(shape instanceof SGroup) out.end();
        }

        public void pageEnd(XMLWriter out, SketchDocument.SketchPage page) {
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

        private void draw(XMLWriter out, SPath shape) {
            out.start("path");
            out.attr("transform", "translate(" + shape.getTranslateX() + "," + shape.getTranslateY() + ")");
            StringBuffer data = new StringBuffer();
            int count = 0;
            
            for(SPath.SubPath sub : shape.getSubPaths()) {
                boolean first = true;
                SPath.PathPoint prev = null;
                for(SPath.PathPoint point : sub.getPoints()) {
                    if(first) {
                        data.append(" M " + point.x + " " +  point.y);
                    } else {
                        data.append(" C " + prev.cx2 + " " + prev.cy2);
                        data.append(" " + point.cx1 + " " + point.cy1);
                        data.append(" " + point.x + " " + point.y);
                    }
                    data.append(" ");
                    prev = point;
                    first = false;
                }
            }

            data.append(" z");
            out.attr("d", data.toString());

            out.attr("fill", toRGBString(shape.getFillPaint()));
            out.attr("fill-opacity", df.format(shape.getFillOpacity()));
            out.attr("stroke", toRGBString(shape.getStrokePaint()));
            out.attr("stroke-width", shape.getStrokeWidth() + "");
            out.end();
        }

        private void draw(XMLWriter out, SArea shape) {
            out.start("path");
            out.attr("transform","translate("+ shape.getTranslateX()+","+ shape.getTranslateY()+")");
            //the vector data
            StringBuffer data = new StringBuffer();
            int count = 0;
            Area area = shape.toArea();
            PathIterator it = area.getPathIterator(new AffineTransform());
            while(!it.isDone()) {
                double[] coords = new double[6];
                int n = it.currentSegment(coords);
                if(n == PathIterator.SEG_MOVETO) {
                    data.append(" M "+coords[0]+" "+coords[1]);
                }
                if(n == PathIterator.SEG_LINETO) {
                    data.append(" L " + coords[0]+" " +coords[1]);
                }
                if(n == PathIterator.SEG_CUBICTO) {
                    data.append(" C "
                            +coords[0]+" "+coords[1] + " "
                            +coords[2]+" "+coords[3] + " "
                            +coords[4]+" "+coords[5] + " "
                            );
                }
                if(n == PathIterator.SEG_CLOSE) {
                    data.append(" z");
                    break;
                }
                it.next();
            }
            out.attr("d", data.toString());
/*
        for(int i=0; i<points.size(); i++) {
            if(i == 0) {
                out.print(" M "+points.get(i).x + " " + points.get(i).y);
            } else {
                out.print(" C "
                        +points.get(i-1).cx2 + " " + points.get(i-1).cy2 + " "
                        +points.get(i).cx1 + " " + points.get(i).cy1 + " "
                        +points.get(i).x + " " + points.get(i).y + " "
                        );
            }
            out.print(" ");
        }*/
            //out.println(" z'");

            out.attr("fill",toRGBString(shape.getFillPaint())+"");
            out.attr("fill-opacity",df.format(shape.getFillOpacity())+"");
            out.attr("stroke",toRGBString(shape.getStrokePaint())+"");
            out.attr("stroke-width",shape.getStrokeWidth()+"");
            out.end();
        }


        private void draw(XMLWriter out, SRect rect) {
            //String id = Math.random();
            String id = "A"+Long.toHexString(Double.doubleToLongBits(Math.random()));
            /*
            if(rect.getFillPaint() instanceof GradientFill) {
                GradientFill grad = (GradientFill) rect.getFillPaint();
                out.start("linearGradient")
                        .attr("id",id);
                out.start("stop")
                        .attr("offset","0.0")
                        .attr("style","stop-color:"+toRGBString(grad.start))
                        .end();
                out.start("stop")
                        .attr("offset","1.0")
                        .attr("style","stop-color:"+toRGBString(grad.end))
                        .end();
                out.end();
            }
            */

            if(rect.getFillPaint() instanceof LinearGradientFill) {
                LinearGradientFill grad = (LinearGradientFill) rect.getFillPaint();
                out.start("g");
                out.start("defs");
                out.start("linearGradient")
                        .attr("id",id)
                        .attr("x1",(grad.getStartX()/rect.getWidth()*100)+"%")
                        .attr("y1",(grad.getStartY()/rect.getHeight()*100)+"%")
                        .attr("x2",(grad.getEndX()/rect.getWidth()*100)+"%")
                        .attr("y2",(grad.getEndY()/rect.getHeight()*100)+"%");
                for(MultiGradientFill.Stop stop : grad.getStops()) {
                    out.start("stop")
                            .attr("offset",stop.getPosition()+"")
                            .attr("stop-color",toHexString(stop.getColor()))
                            .end();
                }
                out.end(); //linearGradient
                out.end(); //defs
            }
            out.start("rect")
                    .attr("x",""+rect.getX())
                    .attr("y",""+rect.getY())
                    .attr("width",""+rect.getWidth())
                    .attr("height",""+rect.getHeight())
                    .attr("transform","translate("+rect.getTranslateX()+","+rect.getTranslateY()+")");

            if(rect.getFillPaint() instanceof FlatColor) {
                out.attr("fill",toRGBString(rect.getFillPaint()));
            }
            if(rect.getFillPaint() instanceof LinearGradientFill) {
                out.attr("fill","url(#"+id+")");
            }
            if(rect.getFillOpacity() != 1) {
                out.attr("fill-opacity",df.format(rect.getFillOpacity()));
            }

            out.attr("stroke",toRGBString(rect.getStrokePaint()));
            out.attr("stroke-width",""+rect.getStrokeWidth());

            out.end();//rect
            if(rect.getFillPaint() instanceof LinearGradientFill) {
                out.end(); //g
            }
        }

        private void draw(XMLWriter out, SOval shape) {
            out.start("ellipse")
                    .attr("cx",(shape.getX() + shape.getWidth() /2)+"");
            out.attr("cy", (shape.getY() + shape.getHeight() / 2) + "");
            out.attr("rx", shape.getWidth() / 2 + "");
            out.attr("ry", shape.getHeight() / 2 + "");
            out.attr("transform", "translate(" + shape.getTranslateX() + "," + shape.getTranslateY() + ")");
            out.attr("fill", toRGBString(shape.getFillPaint()) + "");
            out.attr("fill-opacity", df.format(shape.getFillOpacity()));
            out.attr("stroke", toRGBString(shape.getStrokePaint()) + "");
            out.attr("stroke-width", shape.getStrokeWidth() + "");
            out.end();
        }

        private void draw(XMLWriter out, SText text) {
            org.joshy.gfx.draw.Font font = org.joshy.gfx.draw.Font.DEFAULT;
            font = org.joshy.gfx.draw.Font.name(font.getName())
                    .size((float)text.getFontSize())
                    .weight(text.getWeight())
                    .style(text.getStyle())
                    .resolve();
            out.start("text");
            out.attr("x", (text.getX() + text.getTranslateX()) + "");
            out.attr("y", (text.getY() + text.getTranslateY() + font.getAscender()) + "");
            out.attr("fill", toRGBString(text.getFillPaint()) + "");
            out.attr("fill-opacity", df.format(text.getFillOpacity()) + "");
            out.attr("font-family", font.getName() + "");
            out.attr("font-size", text.getFontSize() + "");
            out.text(text.getText());
            out.end();
        }

        private void draw(XMLWriter out, SPoly shape) {
            if(shape.isClosed()) {
                out.start("polygon");
            } else {
                out.start("polyline");
            }
            StringBuffer points = new StringBuffer();
            for(int i=0; i<shape.pointCount(); i++) {
                points.append("" + (shape.getPoint(i).getX() + shape.getTranslateX())
                        + "," + (shape.getPoint(i).getY() + shape.getTranslateY()) + " ");
            }
            out.attr("points", points.toString());

            if(shape.isClosed()) {
                out.attr("fill", toRGBString(shape.getFillPaint()));
                out.attr("fill-opacity",df.format(shape.getFillOpacity()));
            } else {
                out.attr("fill", "none");
            }
            out.attr("stroke", toRGBString(shape.getStrokePaint()) + "");
            out.attr("stroke-width", shape.getStrokeWidth() + "");

            out.end();
        }

        private void draw(XMLWriter out, NGon shape) {
            out.start("polygon");

            //the vector data
            StringBuffer data = new StringBuffer();
            double[] points = shape.toPoints();
            for(int i=0; i<points.length; i+=2) {
                data.append(""  + (points[i]+shape.getTranslateX())
                        + "," + (points[i+1]+shape.getTranslateY()) + " ");
            }
            out.attr("points", data.toString());


            out.attr("fill", toRGBString(shape.getFillPaint()));
            out.attr("fill-opacity", df.format(shape.getFillOpacity()));
            out.attr("stroke", toRGBString(shape.getStrokePaint()) + "");
            out.attr("stroke-width", shape.getStrokeWidth() + "");
            out.end();
        }

        private static String toHexString(FlatColor color) {
            return "#"+Integer.toHexString(color.getRGBA()&0x00FFFFFF);
        }

        private static String toRGBString(Paint paint) {
            if(paint instanceof FlatColor){
                FlatColor color = (FlatColor) paint;
                return "rgb("+df.format(color.getRed()*100)+"%,"
                        +df.format(color.getGreen()*100)+"%,"
                        +df.format(color.getBlue()*100)+"%)";
            } else {
                return toRGBString(FlatColor.BLACK);
            }
        }
    }

}
