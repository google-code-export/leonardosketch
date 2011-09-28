package org.joshy.sketch.modes.preso;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SText;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 9/27/11
 * Time: 8:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class SwitchTheme {

    public abstract static class PresoThemeAction extends SAction {
        protected FlatColor backgroundFill;
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

            doc.setBackgroundFill(this.backgroundFill);
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
    }

    public static class Cowboy extends PresoThemeAction {

        public Cowboy(SketchDocument doc, VectorDocContext context) {
            super(doc, context);
            this.backgroundFill = FlatColor.hsb(47, 0.20, 1.00);
        }

        @Override
        protected void styleText(SText text) {
            text.setFontName("ChunkFive");
            text.setFillPaint(FlatColor.hsb(47, 0.77, 0.46));
        }
    }
    public static class Future extends PresoThemeAction {
        protected Future(SketchDocument doc, VectorDocContext context) {
            super(doc, context);
            this.backgroundFill = FlatColor.hsb(191, 0.58, 0.23);
        }

        @Override
        protected void styleText(SText text) {
            text.setFontName("Orbitron-Medium");
            text.setFillPaint(FlatColor.hsb(191, 0.80, 0.98));
        }
    }
    public static class Classy extends PresoThemeAction {
        protected Classy(SketchDocument doc, VectorDocContext context) {
            super(doc, context);
            this.backgroundFill = FlatColor.WHITE;//hsb(191, 0.00, 1.00);
        }

        @Override
        protected void styleText(SText text) {
            text.setFontName("Raleway-Thin");
            text.setFillPaint(FlatColor.BLACK);//hsb(191, 0.80, 0.98));
        }
    }
    public static class Standard extends PresoThemeAction {
        protected Standard(SketchDocument doc, VectorDocContext context) {
            super(doc, context);
            this.backgroundFill = FlatColor.hsb(215, 0.07, 1.00);
        }

        @Override
        protected void styleText(SText text) {
            text.setFontName("OpenSans");
            text.setFillPaint(FlatColor.hsb(48, 0.53, 0.05));
        }
    }
}
