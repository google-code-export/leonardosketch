package org.joshy.sketch.actions;

import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SketchDocument;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: May 15, 2010
* Time: 6:16:32 PM
* To change this template use File | Settings | File Templates.
*/
public interface ShapeExporter <T> {
    public void docStart(T out, SketchDocument doc);
    public void pageStart(T out, SketchDocument.SketchPage page);
    public void exportPre(T out, SNode shape);
    public void exportPost(T out, SNode shape);
    public void pageEnd(T out, SketchDocument.SketchPage page);
    public void docEnd(T out, SketchDocument document);
    public boolean isContainer(SNode n);
    public Iterable<? extends SNode> getChildNodes(SNode n);
}
