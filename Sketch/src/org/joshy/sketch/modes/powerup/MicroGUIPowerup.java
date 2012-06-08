package org.joshy.sketch.modes.powerup;

import com.joshondesign.xml.XMLWriter;
import com.sun.tools.doclets.formats.html.resources.standard;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.BackgroundTask;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.control.Textbox;
import org.joshy.gfx.node.layout.FlexBox;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.util.OSUtil;
import org.joshy.gfx.util.u;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.actions.symbols.SymbolManager;
import org.joshy.sketch.model.CustomProperties;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SResizeableNode;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.util.Util;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 6/7/12
 * Time: 1:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class MicroGUIPowerup extends Powerup {
    private Main main;
    private DocContext context;

    @Override
    public CharSequence getMenuName() {
        return "MicroGUI App";
    }

    @Override
    public void enable(DocContext context, Main main) {
        this.main = main;
        this.context = context;

        context.getFileMenu().addItem("Run as Canvas in Browser", new RunInBrowser(context, main));

        SymbolManager.VirtualSymbolSet set = main.symbolManager.createVirtualSet("MicroGUI");
        //checkbox
        DrawDelegate button = new DrawDelegate() {
            public void draw(GFX g, GenericMicroGUIComponent c) {
                g.setPaint(FlatColor.GRAY);
                g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 10, 10);
                g.setPaint(FlatColor.BLACK);
                Font.drawCentered(g, (String)c.propz.get("text"), Font.DEFAULT, c.getX(), c.getY(), c.getWidth(), c.getHeight(), true);
            }
        };

        Map<String,Object> button_props = new HashMap<String, Object>();
        button_props.put("text", "buttons");
        set.addSymbol(new GenericMicroGUIComponent(button, button_props, 100, 30, "button"));

        main.symbolManager.setCurrentSet(set);
        if(!context.sidebarContainer.isOpen()) {
            context.sidebarContainer.setOpen(true);
        }
    }

    private abstract class DrawDelegate {
        public abstract void draw(GFX g, GenericMicroGUIComponent comp);
    }

    private class GenericMicroGUIComponent extends SNode implements SResizeableNode, CustomProperties {
        private DrawDelegate delegate;
        private double width;
        private double height;
        private Map<String, Object> propz;
        private double y;
        private double x;
        private String kind;

        public GenericMicroGUIComponent(DrawDelegate delegate, Map<String, Object> props, double w, double h, String kind) {
            super();
            this.delegate = delegate;
            this.width = w;
            this.height = h;
            this.propz = props;
            this.kind = kind;
        }

        public Iterable<Control> getControls() {
            List<Control> list = new ArrayList<Control>();

            if(propz.containsKey("text")) {
                HFlexBox text_row  = new HFlexBox();
                text_row.setBoxAlign(FlexBox.Align.Stretch);
                text_row.add(new Label("caption"));
                final Textbox tb = new Textbox((String) this.propz.get("text"));
                tb.onAction(new Callback<ActionEvent>() {
                    public void call(ActionEvent actionEvent) throws Exception {
                        propz.put("text",tb.getText());
                        context.redraw();
                    }
                });
                text_row.add(tb.setPrefWidth(100));
                list.add(text_row);
            }

            return list;
        }
        
        @Override
        public SNode duplicate(SNode dupe) {
            if(dupe == null) {
                Map<String,Object> propzCopy = new HashMap<String, Object>(propz);
                dupe = new GenericMicroGUIComponent(this.delegate, propzCopy, this.width, this.height, this.kind);
            }
            return super.duplicate(dupe);
        }

        @Override
        public Bounds getBounds() {
            return new Bounds(x,y,width,height);
        }

        @Override
        public Bounds getTransformedBounds() {
            java.awt.geom.Rectangle2D r = new Rectangle2D.Double(getX(),getY(),getWidth(),getHeight());
            AffineTransform af = new AffineTransform();
            af.translate(getTranslateX(),getTranslateY());

            af.translate(getAnchorX(),getAnchorY());
            af.rotate(Math.toRadians(getRotate()));
            af.scale(getScaleX(), getScaleY());
            af.translate(-getAnchorX(),-getAnchorY());

            Shape sh = af.createTransformedShape(r);
            Rectangle2D bds = sh.getBounds2D();
            return Util.toBounds(bds);
        }

        @Override
        public boolean contains(Point2D point) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
        public void draw(GFX g) {
            this.delegate.draw(g, this);
        }

        public double getX() {
            return this.x;
        }

        public double getWidth() {
            return this.width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getHeight() {
            return this.height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public double getY() {
            return this.y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getPreferredAspectRatio() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean constrainByDefault() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

    }

    private class RunInBrowser extends SAction {
        private DocContext context;
        private Main main;
        private BackgroundTask<DocContext, String> task;

        public RunInBrowser(DocContext context, Main main) {
            this.context = context;
            this.main = main;
        }

        @Override
        public void execute() throws Exception {
            task = new BackgroundTask<DocContext, String>() {
                @Override
                protected void onStart(DocContext data) {
                    super.onStart(data);
                    context.addNotification("Generating JavaFX App");
                }

                @Override
                protected String onWork(DocContext data) {
                    try {
                        dobg();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return "done";
                }
            };

            task.setData(context);
            task.start();
        }

        private void dobg() throws IOException {
            //create temp dir
            File dir = new File("/Users/josh/projects/compilerclass/MicroGUI/temp");
            dir.mkdirs();
            File src_base = new File("/Users/josh/projects/compilerclass/MicroGUI");
            
            //copy in microgui common
            File common = new File(dir, "common");
            common.mkdirs();
            Util.copyToFile(new File(src_base, "common/controls.json"), new File(common, "controls.json"));
            Util.copyToFile(new File(src_base,"common/skin.png"), new File(common,"skin.png"));
            u.p("generated common = " + common);

            //copy in microgui js files
            File js = new File(dir,"ports/js");
            js.mkdirs();
            Util.copyTemplate(new File(src_base,"ports/js"),js, new HashMap<String, String>());
            u.p("copied all javascript to: ");
            u.p(js.getAbsolutePath());

            //generate generated.js
            PrintWriter stub = new PrintWriter(new FileWriter(new File(js,"generated.js")));
            stub.println("require([\"misc\",\"common\",\"controls\"], function(N,Common,Controls) {\n" +
                    "Common.init(function() {");
            stub.println("var root = new Controls.Group();");

            ExportProcessor.process(new MicroGUIJSExport(), stub, (SketchDocument) context.getDocument());
            stub.println("Common.setRoot(root);});});");
            stub.close();


            //generate stub html to load generated.js
            PrintWriter writer = new PrintWriter(new FileWriter(new File(js,"generated.html")));
            writer.println("<html><head>");
            writer.println("<script data-main='generated' src='require.js'></script>");
            writer.println("</head><body>");
            writer.println("<canvas id=\"microcanvas\" width=\"800\" height=\"600\"></canvas>");
            writer.println("</body></html>");
            writer.close();

            //open the browser
            String url = "http://localhost/~josh/projects/compilerclass/MicroGUI/temp/ports/js/generated.html";
            OSUtil.openBrowser(url);
        }
    }

    private class MicroGUIJSExport implements ShapeExporter<PrintWriter> {
        public void docStart(PrintWriter out, SketchDocument doc) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void pageStart(PrintWriter out, SketchDocument.SketchPage page) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void exportPre(PrintWriter out, SNode shape) {
            if(shape instanceof GenericMicroGUIComponent) {
                GenericMicroGUIComponent comp = (GenericMicroGUIComponent) shape;
                if("button".equals(comp.kind)) {
                    String vname = "var"+((int)(Math.random()*10000));
                    out.println("var "+vname+" = new Controls.Button(\""+comp.propz.get("text")+"\");");
                    out.println(vname + ".tx = " + comp.getTranslateX() + ";");
                    out.println(vname+".ty = " + comp.getTranslateY()+";");
                    out.println("root.children.push("+vname+");");
                }
            }
        }

        public void exportPost(PrintWriter out, SNode shape) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void pageEnd(PrintWriter out, SketchDocument.SketchPage page) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void docEnd(PrintWriter out, SketchDocument document) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean isContainer(SNode n) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Iterable<? extends SNode> getChildNodes(SNode n) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
