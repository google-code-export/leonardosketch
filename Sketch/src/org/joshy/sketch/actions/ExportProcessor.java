package org.joshy.sketch.actions;

import org.joshy.sketch.model.SGroup;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.model.SketchDocument;

import java.io.PrintWriter;
import java.util.List;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: May 15, 2010
* Time: 6:23:57 PM
* To change this template use File | Settings | File Templates.
*/
public class ExportProcessor {
    
    public static void process(ShapeExporter exporter, Object out, SketchDocument document) {
        exporter.docStart(out,document);
        for(SketchDocument.SketchPage page : document.getPages()) {
            exporter.pageStart(out,page);
            exportList(out,exporter,page.getNodes());
            exporter.pageEnd(out,page);
        }
        exporter.docEnd(out,document);
    }
    
    private static void exportList(Object out, ShapeExporter exporter, Iterable<? extends SNode> shapes) {
        for(SNode n : shapes) {
            exporter.exportPre(out,n);
            if(exporter.isContainer(n)){
                exportList(out,exporter, exporter.getChildNodes(n));
            }
            exporter.exportPost(out,n);
        }
    }

    public static void processFragment(ShapeExporter exporter, Object out, Iterable<SNode> shapes) {
        exportList(out,exporter,shapes);        
    }
}
