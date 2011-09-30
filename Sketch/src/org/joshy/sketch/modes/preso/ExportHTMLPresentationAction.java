package org.joshy.sketch.modes.preso;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.util.OSUtil;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SText;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 9/30/11
 * Time: 11:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExportHTMLPresentationAction extends SAction {
    private VectorDocContext context;

    public ExportHTMLPresentationAction(VectorDocContext ctx) {
        this.context = ctx;
    }

    @Override
    public void execute() throws Exception {
        File basedir = new File("output");
        basedir.mkdir();
        File file = new File(basedir,"presentation.html");
        XMLWriter out = new XMLWriter(new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8")),file.toURI());
        ExportProcessor.process(new HTMLPresoExport(basedir), out, context.getDocument());
        OSUtil.openBrowser(file.toURI().toASCIIString());
    }

    private class HTMLPresoExport implements ShapeExporter<XMLWriter> {
        private File basedir;

        public HTMLPresoExport(File basedir) {
            this.basedir = basedir;
        }

        public void docStart(XMLWriter out, SketchDocument doc) {
            out.start("html");
            out.start("head");
            out.start("title").text("a presentation crafted with Leonardo Sketch").end();
            File resources = new File(basedir,"resources");
            resources.mkdir();
            SwitchTheme.PresoThemeAction theme = (SwitchTheme.PresoThemeAction) doc.getProperties().get("theme");
            if(theme != null) {
                theme.exportResources(out,resources);
            }
            out.end();
            out.start("body");
        }

        public void pageStart(XMLWriter out, SketchDocument.SketchPage page) {
            out.start("div","class","page");
            for(SNode node : page.getNodes()) {
                if(node instanceof SText) {
                    SText text = (SText) node;
                    if(text.isBulleted()) {
                        String[] lines = text.getText().split("\n");
                        out.start("ul");
                        for(String line : lines) {
                            out.start("li").text(line).end();
                        }
                        out.end();
                        continue;
                    }
                    if("title".equals(text.getStringProperty("text-class"))) {
                        out.start("h1").text(text.getText()).end();
                        continue;
                    }
                    if("subtitle".equals(text.getStringProperty("text-class"))) {
                        out.start("h3").text(text.getText()).end();
                        continue;
                    }
                    if("header".equals(text.getStringProperty("text-class"))) {
                        out.start("h2").text(text.getText()).end();
                        continue;
                    }
                    out.start("p").text(text.getText()).end();
                }
            }
        }

        public void exportPre(XMLWriter out, SNode shape) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void exportPost(XMLWriter out, SNode shape) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void pageEnd(XMLWriter out, SketchDocument.SketchPage page) {
            out.end();
        }

        public void docEnd(XMLWriter out, SketchDocument document) {
            out.end();
            out.end();
            out.close();
        }

        public boolean isContainer(SNode n) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Iterable<? extends SNode> getChildNodes(SNode n) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
