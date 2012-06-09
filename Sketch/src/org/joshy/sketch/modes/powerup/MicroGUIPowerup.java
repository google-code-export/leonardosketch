package org.joshy.sketch.modes.powerup;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Image;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MicroGUIPowerup extends Powerup {
    private Main main;
    private DocContext context;
    private static File src_base = new File("/Users/josh/projects/compilerclass/MicroGUI");

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

        /*
        //checkbox
        DrawDelegate button = new DrawDelegate() {
            public void draw(GFX g, GenericMicroGUIComponent comp) {
                g.setPaint(FlatColor.GRAY);
                g.fillRoundRect(comp.getX(), comp.getY(), comp.getWidth(), comp.getHeight(), 10, 10);
                g.setPaint(FlatColor.BLACK);
                Font.drawCentered(g, (String) comp.propz.get("text"), Font.DEFAULT, comp.getX(), comp.getY(), comp.getWidth(), comp.getHeight(), true);
            }
        };

        Map<String,Object> button_props = new HashMap<String, Object>();
        button_props.put("text", "button");
        set.addSymbol(new GenericMicroGUIComponent(button, button_props, 100, 30, "button", true, false));
        */
        try {
            File json_file = new File(src_base,"common/controls2.json");
            String text = new Scanner( json_file ).useDelimiter("\\A").next();
            JSONObject rootJSON = new JSONObject(text);
            final BufferedImage skinpng = ImageIO.read(new File(src_base,"common/skin.png"));
            final Image skin = Image.create(skinpng);

            JSONArray root = rootJSON.getJSONArray("controls");
            for(int i=0; i<root.length(); i++) {
                u.p("control = " + root.getJSONObject(i).getString("name"));
                set.addSymbol(createControl(root.getJSONObject(i),skin));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        main.symbolManager.setCurrentSet(set);
        if(!context.sidebarContainer.isOpen()) {
            context.sidebarContainer.setOpen(true);
        }
    }

    private GenericMicroGUIComponent createControl(final JSONObject root, final Image skin) throws JSONException {
        DrawDelegate drawDelegate = new DrawDelegate() {
            @Override
            public void draw(GFX g, GenericMicroGUIComponent comp) throws JSONException {
                g.translate(comp.getX(), comp.getY());
                JSONArray regions = root.getJSONArray("regions");
                for(int i=0; i<regions.length(); i++) {
                    JSONObject region = regions.getJSONObject(i);
                    //draw if the normal state, or no state
                    if(!region.has("state") || region.getString("state").equals("normal")) {
                        g.setPaint(FlatColor.RED);
                        String align = region.getString("align");
                        JSONObject bounds = region.getJSONObject("bounds");
                        int x = bounds.getInt("x");
                        int y = bounds.getInt("y");
                        int w = bounds.getInt("w");
                        int h = bounds.getInt("h");
                        if("all".equals(align)) {
                            x = (int) comp.getX();
                            y = (int) comp.getY();
                            w = (int) comp.getWidth();
                            h = (int) comp.getHeight();
                        }
                        if("none".equals("align")) {
                            //do nothing
                        }
                        if(region.getString("kind").equals("image")) {
                            JSONObject imgbounds = region.getJSONObject("imagebounds");
                            g.drawImage(skin,
                                    x,y,w,h,
                                    imgbounds.getInt("x"),
                                    imgbounds.getInt("y"),
                                    imgbounds.getInt("w"),
                                    imgbounds.getInt("h")
                                    );
                            g.drawRect(x, y, w, h);
                        }
                        if(region.getString("kind").equals("text")) {
                            g.setPaint(FlatColor.BLACK);
                            Font.drawCentered(g,region.getString("text"),Font.DEFAULT,x,y,w,h,true);
                        }
                    }
                }
                g.setPaint(FlatColor.GREEN);
                g.drawRect(0,0,comp.getWidth(),comp.getHeight());
                g.translate(-comp.getX(),comp.getY());
            }
        };
        JSONObject bounds = root.getJSONObject("bounds");
        Map<String,Object> control_props = new HashMap<String, Object>();
        String constrain = root.getString("constrain");
        boolean horz = false;
        boolean vert = false;
        if("horizontal".equals(constrain)) horz = true;
        if("vertical".equals(constrain)) vert = true;
        return new GenericMicroGUIComponent(
                drawDelegate,
                control_props,
                bounds.getInt("w"),bounds.getInt("h"),
                root.getString("name"),
                horz,vert
        );
    }

    private abstract class DrawDelegate {
        public abstract void draw(GFX g, GenericMicroGUIComponent comp) throws JSONException;
    }

    private class GenericMicroGUIComponent extends SNode implements SResizeableNode, CustomProperties {
        private DrawDelegate delegate;
        private double width;
        private double height;
        private Map<String, Object> propz;
        private double y;
        private double x;
        private String kind;
        private boolean horz;
        private boolean vert;

        public GenericMicroGUIComponent(DrawDelegate delegate, Map<String, Object> props, double w, double h, String kind, boolean horz, boolean vert) {
            super();
            this.delegate = delegate;
            this.width = w;
            this.height = h;
            this.propz = props;
            this.kind = kind;
            this.horz = horz;
            this.vert = vert;
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
                dupe = new GenericMicroGUIComponent(this.delegate, propzCopy, this.width, this.height, this.kind, this.horz, this.vert);
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
            return false;
        }
        public void draw(GFX g) {
            try {
                this.delegate.draw(g, this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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

        public Constrain getConstrain() {
            if(this.horz) {
                return Constrain.Horizontal;
            }
            if(this.vert) {
                return Constrain.Vertical;
            }
            return Constrain.None;
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
