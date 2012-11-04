package org.joshy.sketch.modes.powerup;

import com.joshondesign.amino.LayoutTest;
import com.joshondesign.xml.XMLWriter;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.Togglebutton;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.GraphicsUtil;
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
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.util.Util;

/**
 * Created with IntelliJ IDEA.
 * User: josh
 * Date: 11/3/12
 * Time: 8:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class AminoXMLPowerup extends Powerup {
    private Stage testStage;
    private Panel testPanel;

    @Override
    public CharSequence getMenuName() {
        return "Amino Panel";
    }

    @Override
    public void enable(DocContext context, Main main) {
        context.getFileMenu().addItem("Export as Amino XML", new ExportAsXML(context, main));

        SymbolManager.VirtualSymbolSet set = main.symbolManager.createVirtualSet("Amino");

        context.getSidebar().setSelected(((VectorDocContext)context).getSymbolPanel());
        context.sidebarContainer.setOpen(true);

        //label
        Map<String,Object> label_props = new HashMap<String, Object>();
        label_props.put("text", "label");
        set.addSymbol(new GenericAminoNode(label_delegate,label_props, 100, 30, "label"));
        //button
        Map<String,Object> button_props = new HashMap<String, Object>();
        button_props.put("text", "button");
        set.addSymbol(new GenericAminoNode(
                button_delegate,button_props, 100, 30, "button"));
        //checkbox
        Map<String,Object> checkbox_props = new HashMap<String, Object>();
        checkbox_props.put("text", "checkbox");
        set.addSymbol(new GenericAminoNode(
                checkbox_delegate, checkbox_props, 100, 30, "checkbox"));
        //radiobutton
        Map<String,Object> radio_props = new HashMap<String, Object>();
        radio_props.put("text", "radiobutton");
        set.addSymbol(new GenericAminoNode(
                radio_delegate, radio_props, 100, 30, "radiobutton"));
        //togglebutton
        Map<String,Object> togglebutton_props = new HashMap<String, Object>();
        togglebutton_props.put("text", "togglebutton");
        set.addSymbol(new GenericAminoNode(
                togglebutton_delegate, togglebutton_props, 100, 30, "togglebutton"));
        //textbox
        Map<String,Object> textbox_props = new HashMap<String, Object>();
        textbox_props.put("text", "text field");
        set.addSymbol(new GenericAminoNode(
                textbox_delegate, textbox_props, 100, 30, "textbox"));


        //panel
        Map<String,Object> panel_props = new HashMap<String, Object>();
        GenericAminoNode panelSymbol = new GenericAminoNode(
                panel_delegate, panel_props, 500, 400, "Panel");
        set.addSymbol(panelSymbol);

        SketchDocument.SketchPage page = ((VectorDocContext) context).getDocument().getCurrentPage();
        SNode rootPanel = panelSymbol.duplicate(null);
        page.add(rootPanel);
        rootPanel.setLocked(true);
        rootPanel.setTranslateX(100);
        rootPanel.setTranslateY(100);

    }

    private static interface DrawDelegate {
        public void draw(GFX g, GenericAminoNode node);
    }


    //label
    DrawDelegate label_delegate = new DrawDelegate() {
        public void draw(GFX g, GenericAminoNode c) {
            g.setPaint(FlatColor.BLACK);
            Font.drawCenteredVertically(g, "label", Font.DEFAULT, c.getX() + 5, c.getY(), c.getWidth(), c.getHeight(), true);
        }
    };

    //button
    DrawDelegate button_delegate = new DrawDelegate() {
        public void draw(GFX g, GenericAminoNode c) {
            g.setPaint(FlatColor.GRAY);
            g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 10, 10);
            g.setPaint(FlatColor.BLACK);
            Font.drawCentered(g, "button", Font.DEFAULT, c.getX(), c.getY(), c.getWidth(), c.getHeight(), true);
        }
    };

    //panel
    DrawDelegate panel_delegate = new DrawDelegate() {
        public void draw(GFX g, GenericAminoNode c) {
            g.setPaint(FlatColor.WHITE);
            g.fillRoundRect(c.getX(),c.getY(), c.getWidth(), c.getHeight(),10,10);
            g.setPaint(FlatColor.BLACK);
            g.drawRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 10, 10);
        }
    };

    //checkbox
    DrawDelegate checkbox_delegate = new DrawDelegate() {
        public void draw(GFX g, GenericAminoNode c) {
            g.setPaint(FlatColor.GRAY);
            g.fillRoundRect(c.getX(), c.getY(), c.getHeight(), c.getHeight(), 3, 3);
            g.setPaint(FlatColor.BLACK);
            Font.drawCenteredVertically(g, "checkbox", Font.DEFAULT, c.getX() + c.getHeight() + 5, c.getY(), c.getWidth(), c.getHeight(), true);
        }
    };

    //radio button
    DrawDelegate radio_delegate = new DrawDelegate() {
        public void draw(GFX g, GenericAminoNode c) {
            g.setPaint(FlatColor.GRAY);
            g.fillCircle(c.getX() + c.getHeight() / 2, c.getY() + c.getHeight() / 2, c.getHeight() / 3);
            g.setPaint(FlatColor.BLACK);
            Font.drawCenteredVertically(g, "radiobutton", Font.DEFAULT, c.getX() + c.getHeight() + 5, c.getY(), c.getWidth(), c.getHeight(), true);
        }
    };

    //textfield
    DrawDelegate textbox_delegate = new DrawDelegate() {
        public void draw(GFX g, GenericAminoNode c) {
            g.setPaint(FlatColor.WHITE);
            g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 3, 3);
            g.setPaint(FlatColor.BLACK);
            g.drawRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 3, 3);
            g.setPaint(FlatColor.BLACK);
            Font.drawCenteredVertically(g, "text field", Font.DEFAULT, c.getX() + c.getHeight() + 5, c.getY(), c.getWidth(), c.getHeight(), true);
        }
    };

    //toggle button
    DrawDelegate togglebutton_delegate = new DrawDelegate() {
        public void draw(GFX g, GenericAminoNode c) {
            g.setPaint(FlatColor.GRAY);
            g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 10, 10);
            g.setPaint(FlatColor.BLACK);
            Font.drawCentered(g, "toggle button", Font.DEFAULT, c.getX(), c.getY(), c.getWidth(), c.getHeight(), true);
        }
    };

    //slider
    DrawDelegate slider_delegate = new DrawDelegate() {
        public void draw(GFX g, GenericAminoNode c) {
            g.setPaint(FlatColor.GRAY);
            g.fillRoundRect(c.getX(), c.getY() + 3, c.getWidth(), c.getHeight()-6, 10, 10);
            g.setPaint(FlatColor.BLACK);
            g.drawRoundRect(c.getX(), c.getY() + 3, c.getWidth(), c.getHeight()-6, 10, 10);
            g.fillRoundRect(c.getX() + c.getWidth() / 2 - 10,c.getY(),20,c.getHeight(),4,4);
        }
    };

    //progbar
    GenericFXComponent.DrawDelegate progbar_delegate = new GenericFXComponent.DrawDelegate() {
        public void draw(GFX g, GenericFXComponent c) {
            g.setPaint(FlatColor.WHITE);
            g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 10, 10);
            g.setPaint(FlatColor.BLUE);
            g.fillRoundRect(c.getX(), c.getY(), c.getWidth()/2, c.getHeight(), 10, 10);
            g.setPaint(FlatColor.BLACK);
            g.drawRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 10, 10);
        }
    };

    //progindicator
    GenericFXComponent.DrawDelegate progind_delegate = new GenericFXComponent.DrawDelegate() {
        public void draw(GFX g, GenericFXComponent c) {
            g.setPaint(FlatColor.BLUE);
            g.fillOval(c.getX(), c.getY(), c.getHeight(), c.getHeight());
            g.setPaint(FlatColor.BLACK);
            g.drawOval(c.getX(), c.getY(), c.getHeight(), c.getHeight());
            g.setPaint(FlatColor.WHITE);
            Font.drawCentered(g,"100%",Font.DEFAULT,c.getX(),c.getY(),c.getHeight(),c.getHeight(),true);
        }
    };

    //choice box
    DrawDelegate choicebox_delegate = new DrawDelegate() {
        public void draw(GFX g, GenericAminoNode c) {
            g.setPaint(FlatColor.WHITE);
            g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 3, 3);
            g.setPaint(FlatColor.BLACK);
            g.drawRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 3, 3);
            g.setPaint(FlatColor.BLACK);
            GraphicsUtil.fillDownArrow(g, c.getX() + c.getWidth() - 15 - 5, c.getY() + 5, 15);
            Font.drawCenteredVertically(g, "choice box", Font.DEFAULT, c.getX() +  5, c.getY(), c.getWidth(), c.getHeight(), true);
        }
    };

    public static class GenericAminoNode extends SNode implements CustomProperties, SResizeableNode {

        private DrawDelegate delegate;
        private Map<String, Object> aminoProps;
        private int initialWidth;
        private int initialHeight;
        private String name;
        private double width;
        private double height;
        private double x;
        private double y;
        boolean rightAnchored = false;
        boolean leftAnchored = false;
        boolean topAnchored = false;
        boolean bottomAnchored = false;

        public GenericAminoNode(DrawDelegate delegate, Map<String, Object> props, int width, int height, String name) {
            this.delegate = delegate;
            this.aminoProps = props;
            this.initialWidth = width;
            this.width = width;
            this.initialHeight = height;
            this.height = height;
            this.x = 0;
            this.y = 0;
            this.name = name;
        }

        @Override
        public SNode duplicate(SNode dupe) {
            if(dupe == null) {
                dupe = new GenericAminoNode(this.delegate, aminoProps, initialWidth, initialHeight, name);
            }
            GenericAminoNode sdupe = (GenericAminoNode) dupe;
            sdupe.setX(this.getX());
            sdupe.setY(this.getY());
            sdupe.setWidth(this.getWidth());
            sdupe.setHeight(this.getHeight());
            return super.duplicate(dupe);
        }


        public Iterable<Control> getControls() {
            ArrayList<Control> controls = new ArrayList<Control>();
            HFlexBox row = new HFlexBox();

            final Togglebutton left = new Togglebutton("<");
            left.setSelected(leftAnchored);
            left.onClicked(new Callback<ActionEvent>() {
                public void call(ActionEvent event) throws Exception {
                    leftAnchored = left.isSelected();
                }
            });
            row.add(left);

            final Togglebutton right = new Togglebutton(">");
            right.setSelected(rightAnchored);
            right.onClicked(new Callback<ActionEvent>() {
                public void call(ActionEvent event) throws Exception {
                    rightAnchored = right.isSelected();
                }
            });
            row.add(right);

            final Togglebutton top = new Togglebutton("^");
            top.setSelected(topAnchored);
            top.onClicked(new Callback<ActionEvent>() {
                public void call(ActionEvent event) throws Exception {
                    topAnchored = top.isSelected();
                }
            });
            row.add(top);

            final Togglebutton bottom = new Togglebutton("v");
            bottom.setSelected(bottomAnchored);
            bottom.onClicked(new Callback<ActionEvent>() {
                public void call(ActionEvent event) throws Exception {
                    bottomAnchored = bottom.isSelected();
                }
            });
            row.add(bottom);
            controls.add(row);
            return controls;
        }

        @Override
        public Bounds getBounds() {
            return new Bounds(x,y,width, height);
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
            double x = getX() + getTranslateX();
            if(point.getX() >= x && point.getX() <= x + getWidth()) {
                double y = getY() + getTranslateY();
                if(point.getY() >= y && point.getY() <= y + this.getHeight()) {
                    return true;
                }
            }
            return false;
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
            return 1;
        }

        public boolean constrainByDefault() {
            return false;
        }

        public Constrain getConstrain() {
            return Constrain.None;
        }

        public void draw(GFX g) {
            this.delegate.draw(g,this);
        }

        public String getXMLName() {
            return name;
        }
    }

    private class ExportAsXML extends SAction {
        private DocContext context;
        private Main main;

        public ExportAsXML(DocContext context, Main main) {
            super();
            this.context = context;
            this.main = main;
        }

        @Override
        public void execute() throws Exception {
            File tempdir = Util.makeTempDir();
            File appdir = new File(tempdir,"testapp");
            appdir.mkdirs();
            File outfile = new File(appdir,"layout.xml");
            u.p("generating: " + outfile.getAbsolutePath());
            XMLWriter out = new XMLWriter(outfile);
            ExportProcessor.process(new AminoXMLExport(), out, (SketchDocument) context.getDocument());
            out.close();

            String str = u.fileToString(new FileInputStream(outfile));
            u.p("result = \n" + str);

            testPanel = LayoutTest.loadGUI(new FileInputStream(outfile));
            if(testStage == null) {
                testStage = Stage.createStage();
                testStage.setAlwaysOnTop(true);
                testStage.centerOnScreen();
            }
            testStage.setContent(testPanel);

        }
    }

    private static class AminoXMLExport implements ShapeExporter<XMLWriter> {

        private int width;
        private int height;

        private AminoXMLExport() {
            this.width = 500;
            this.height = 400;
        }

        private static DecimalFormat df = new DecimalFormat();
        static {
            df.setMaximumFractionDigits(2);
        }

        public void docStart(XMLWriter out, SketchDocument doc) {
            out.header();
            out.start("layout","defaultbounds","0,0,500,300");
        }

        public void pageStart(XMLWriter out, SketchDocument.SketchPage page) {
            out.start("anchorpanel");
        }

        public void exportPre(XMLWriter out, SNode shape) {
            if(!(shape instanceof GenericAminoNode)) return;

            GenericAminoNode node = (GenericAminoNode) shape;

            u.p("bounds = " + node.getBounds());
            u.p("tbounds = " + node.getTransformedBounds());
            out.start(node.getXMLName());
            out.attr("x", df.format(node.getTranslateX() + node.getX()));
            out.attr("y", df.format(node.getTranslateY() + node.getY()));
            out.attr("width",df.format(node.getWidth()));
            out.attr("height",df.format(node.getHeight()));
            for(String key : node.aminoProps.keySet()) {
                out.attr(key,objectToString(node.aminoProps.get(key)));
            }

            if(node.getId() != null) out.attr("id",node.getId());
            if(node.rightAnchored) {
                out.attr("right",df.format(width-node.getTransformedBounds().getX2()));
            }
            if(node.bottomAnchored) {
                out.attr("bottom",df.format(height-node.getTransformedBounds().getY2()));
            }
        }

        private String objectToString(Object o) {
            if(o == null) return "";
            if(o instanceof String) {
                return (String) o;
            }
            return "";
        }

        public void exportPost(XMLWriter out, SNode shape) {
            if(!(shape instanceof GenericAminoNode)) return;
            out.end();
        }

        public void pageEnd(XMLWriter out, SketchDocument.SketchPage page) {
            out.end();
        }

        public void docEnd(XMLWriter out, SketchDocument document) {
            out.end();
        }

        public boolean isContainer(SNode n) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Iterable<? extends SNode> getChildNodes(SNode n) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
