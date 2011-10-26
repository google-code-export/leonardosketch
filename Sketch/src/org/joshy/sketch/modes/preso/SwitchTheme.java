package org.joshy.sketch.modes.preso;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.LinearGradientFill;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.draw.PatternPaint;
import org.joshy.gfx.node.Bounds;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.DocumentActions;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SText;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.util.Util;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 9/27/11
 * Time: 8:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class SwitchTheme {

    public abstract static class PresoThemeAction extends SAction {
        protected Paint backgroundFill;
        private VectorDocContext context;
        private SketchDocument doc;

        protected PresoThemeAction(SketchDocument doc, VectorDocContext context) {
            this.doc = doc;
            this.context = context;
        }
        @Override
        public void execute() throws Exception {


            SketchDocument doc = null;
            if(this.doc != null) doc = this.doc;
            if(this.context != null) doc = context.getDocument();

            Paint fill = this.backgroundFill;
            if(this.backgroundFill instanceof LinearGradientFill) {
                fill = DocumentActions.resizeTo((LinearGradientFill) this.backgroundFill,new Bounds(0,0,doc.getWidth(),doc.getHeight()));
            }
            doc.setBackgroundFill(fill);
            doc.getProperties().put("theme",this);

            for(SketchDocument.SketchPage page : doc.getPages()) {
                for(SNode node : page.getNodes()) {
                    if(node instanceof SText) {
                        SText text = (SText) node;
                        styleText(text);
                    }
                }
            }
            if(context != null) {
                context.redraw();
            }
        }

        protected abstract void styleText(SText text);

        public abstract void exportResources(XMLWriter out, File resources);
    }

    public static class Cowboy extends PresoThemeAction {

        public Cowboy(SketchDocument doc, VectorDocContext context) {
            super(doc, context);
            this.backgroundFill = FlatColor.hsb(47, 0.20, 1.00);
            try {
                this.backgroundFill = PatternPaint.create(
                        SwitchTheme.class.getResource("resources/cowboybg.png")
                        ,"cowboybg.png");
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        @Override
        protected void styleText(SText text) {
            text.setFontName("ChunkFive");
            text.setFillPaint(FlatColor.hsb(47, 0.77, 0.46));
        }

        @Override
        public void exportResources(XMLWriter out, File resources) {
            out.start("link").attr("rel", "stylesheet").attr("href", "resources/common.css");
            out.start("link").attr("rel", "stylesheet").attr("href", "resources/cowboy.css");
            try {
                Util.copyToFile(this.getClass().getResourceAsStream("resources/common.css"),
                        new File(resources, "common.css"));
                Util.copyToFile(this.getClass().getResourceAsStream("resources/cowboy.css"),
                        new File(resources, "cowboy.css"));
                Util.copyToFile(this.getClass().getResourceAsStream("resources/cowboybg.png"),
                        new File(resources, "cowboybg.png"));
                Util.copyToFile(Main.getFontMap().get("ChunkFive").getInputStream(),
                        new File(resources, "ChunkFive.ttf"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    public static class Future extends PresoThemeAction {
        protected Future(SketchDocument doc, VectorDocContext context) {
            super(doc, context);
            int size = 100;
            this.backgroundFill = new LinearGradientFill()
                    .setStartX(size / 2).setStartXSnapped(LinearGradientFill.Snap.Middle)
                    .setEndX(size / 2).setEndXSnapped(LinearGradientFill.Snap.Middle)
                    .setStartY(0).setStartYSnapped(LinearGradientFill.Snap.Start)
                    .setEndY(size).setEndYSnapped(LinearGradientFill.Snap.End)
                    .addStop(0, FlatColor.fromRGBInts(0, 0, 0))
                    .addStop(0.87, new FlatColor("#ff004f57"))
                    .addStop(1, new FlatColor("#ff000a08"));
        }

        @Override
        protected void styleText(SText text) {
            text.setFontName("Orbitron-Medium");
            text.setFillPaint(FlatColor.hsb(191, 0.80, 0.98));
        }

        @Override
        public void exportResources(XMLWriter out, File resources) {
            out.start("link").attr("rel","stylesheet").attr("type","text/css")
                    .attr("href","http://fonts.googleapis.com/css?family=Orbitron")
                    .end();
            out.start("link").attr("rel", "stylesheet").attr("href", "resources/common.css").end();
            out.start("link").attr("rel", "stylesheet").attr("href", "resources/future.css").end();
            try {
                Util.copyToFile(this.getClass().getResourceAsStream("resources/common.css"),
                        new File(resources, "common.css"));
                Util.copyToFile(this.getClass().getResourceAsStream("resources/future.css"),
                        new File(resources, "future.css"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static class Classy extends PresoThemeAction {
        protected Classy(SketchDocument doc, VectorDocContext context) {
            super(doc, context);
            this.backgroundFill = FlatColor.WHITE;
        }

        @Override
        protected void styleText(SText text) {
            text.setFontName("Raleway-Thin");
            text.setFillPaint(FlatColor.BLACK);
        }

        @Override
        public void exportResources(XMLWriter out, File resources) {
            out.start("link").attr("rel","stylesheet").attr("type","text/css")
                    .attr("href","http://fonts.googleapis.com/css?family=Raleway:100")
                    .end();
            out.start("link").attr("rel", "stylesheet").attr("href", "resources/common.css").end();
            out.start("link").attr("rel", "stylesheet").attr("href", "resources/classy.css").end();

            try {
                Util.copyToFile(this.getClass().getResourceAsStream("resources/common.css"),
                        new File(resources, "common.css"));
                Util.copyToFile(this.getClass().getResourceAsStream("resources/classy.css"),
                        new File(resources, "classy.css"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Standard extends PresoThemeAction {
        protected Standard(SketchDocument doc, VectorDocContext context) {
            super(doc, context);
            this.backgroundFill = FlatColor.BLACK;
        }

        @Override
        protected void styleText(SText text) {
            text.setFontName("OpenSans");
            text.setFillPaint(FlatColor.WHITE);
        }

        @Override
        public void exportResources(XMLWriter out, File resources) {
            out.start("link").attr("rel","stylesheet").attr("type","text/css")
                    .attr("href","http://fonts.googleapis.com/css?family=Open+Sans")
                    .end();
            out.start("link").attr("rel", "stylesheet").attr("href", "resources/common.css").end();
            out.start("link").attr("rel", "stylesheet").attr("href", "resources/standard.css").end();

            try {
                Util.copyToFile(this.getClass().getResourceAsStream("resources/common.css"),
                        new File(resources, "common.css"));
                Util.copyToFile(this.getClass().getResourceAsStream("resources/standard.css"),
                        new File(resources, "standard.css"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
