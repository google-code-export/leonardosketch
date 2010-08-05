package org.joshy.sketch.script;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.modes.vector.VectorDocContext;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Aug 4, 2010
 * Time: 11:35:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScriptTools {

    public static class RunScriptAction extends SAction {
        private DocContext context;
        private File file;

        public RunScriptAction(File file, DocContext context) {
            this.file = file;
            this.context = context;
        }

        @Override
        public void execute() {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByExtension("js");
            u.p("engine = " + engine);
            engine.put("ctx",context);
            try {
                u.p("js = " + engine.eval(new InputStreamReader(new FileInputStream(file))));
            } catch (Exception e) {
                e.printStackTrace();
            }

                                       /*
            VectorDocContext ctx = (VectorDocContext) context;
            SketchDocument doc = ctx.getDocument();
            Selection sel = ctx.getSelection();
            if(!sel.isEmpty()) {
                SNode item = sel.firstItem();
                for(int i=0; i<5; i++) {
                    SNode dupe = item.duplicate(null);
                    dupe.setTranslateX(dupe.getTranslateX()+i*(dupe.getBounds().getWidth()+10));
                    doc.getCurrentPage().model.add(dupe);
                    if(dupe instanceof SShape) {
                        SShape shape = (SShape) dupe;
                        shape.setFillPaint(FlatColor.hsb(30*i,1,1));
                    }
                }
                doc.setDirty(true);
                ctx.getCanvas().redraw();
            }*/
        }
    }
}
