package org.joshy.sketch.controls;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.GradientFill;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.ScrollPane;
import org.joshy.gfx.node.control.Scrollbar;
import org.joshy.gfx.node.layout.Container;
import org.joshy.sketch.model.CanvasDocument;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.util.DrawUtils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Sep 10, 2010
* Time: 5:12:39 PM
* To change this template use File | Settings | File Templates.
*/
public class Ruler extends Container {
    private boolean vertical;
    private double offset;
    private DocContext context;
    private MouseEvent lastMouse;
    private List<GuidelineHandle> guideHandles = new ArrayList<GuidelineHandle>();
    private CanvasDocument doc;

    public Ruler(boolean vertical, ScrollPane scrollPane, final DocContext context) {
        this.vertical = vertical;
        this.context = context;

        Scrollbar sp;
        if(vertical) {
            sp = scrollPane.getVerticalScrollBar();
        } else {
            sp = scrollPane.getHorizontalScrollBar();
        }

        EventBus.getSystem().addListener(sp, ChangedEvent.DoubleChanged, new Callback<ChangedEvent>(){
            public void call(ChangedEvent event) {
                offset = (Double) event.getValue();
                for(GuidelineHandle g : guideHandles) {
                    if(g.guideline.isVertical()) {
                        g.setTranslateX(g.guideline.getPosition() - g.size / 2 - offset);
                    } else {
                        g.setTranslateY(g.guideline.getPosition() - g.size / 2 - offset);
                    }
                }
                setDrawingDirty();
            }
        });
        EventBus.getSystem().addListener(context.getCanvas(), MouseEvent.MouseAll, new Callback<MouseEvent>() {
            public void call(MouseEvent mouseEvent) {
                lastMouse = mouseEvent;
                setDrawingDirty();
            }
        });
        EventBus.getSystem().addListener(this, MouseEvent.MouseAll, new Callback<MouseEvent>() {
            public SketchDocument.Guideline newGuide;

            public void call(MouseEvent mouseEvent) throws Exception {
                if(mouseEvent.getType() == MouseEvent.MouseDragged) {
                    if(doc instanceof SketchDocument) {
                        SketchDocument sdoc = (SketchDocument) doc;
                        if(Ruler.this.vertical) {
                            if(mouseEvent.getPointInNodeCoords(Ruler.this).getX() > getWidth()) {
                                if(newGuide == null) {
                                    newGuide = sdoc.createGuideline(0,true);
                                } else {
                                    newGuide.setPosition(mouseEvent.getPointInNodeCoords(context.getCanvas()).getX());
                                }
                            }
                        } else {
                            if(mouseEvent.getPointInNodeCoords(Ruler.this).getY() > getHeight()) {
                                if(newGuide == null) {
                                    newGuide = sdoc.createGuideline(0,false);
                                } else {
                                    newGuide.setPosition(mouseEvent.getPointInNodeCoords(context.getCanvas()).getY());
                                }
                            }
                        }
                    }
                }
                if(mouseEvent.getType() == MouseEvent.MouseReleased) {
                    newGuide = null;
                }
            }
        });
    }


    @Override
    public void draw(GFX g) {
        Font fnt = Font.name("Arial").size(10).resolve();
        Bounds oldBounds = g.getClipRect();
        g.setClipRect(new Bounds(0,0,getWidth(),getHeight()));

        g.setPaint(FlatColor.RED);
        if(vertical) {
            g.setPaint(new GradientFill(FlatColor.hsb(0,0,0.9),FlatColor.hsb(0,0,0.5),90,false,0,0,getWidth(),0));
        } else {
            g.setPaint(new GradientFill(FlatColor.hsb(0,0,0.9),FlatColor.hsb(0,0,0.5),90,false,0,0,0,getHeight()));
        }
        g.fillRect(0,0,getWidth()-1,getHeight()-1);
        g.setPaint(FlatColor.BLACK);
        g.drawRect(0,0,getWidth()-1,getHeight()-1);
        g.setPaint(new FlatColor(0x505050));
        int o = (int) offset;
        int step = 50;

        double scale = context.getCanvas().getScale();
        
        if(vertical) {
            int y = 0;
            int w = (int) getWidth();
            while(true) {
                if(y-o > -step) {
                    //major ticks
                    g.drawLine(10,y-o,w,y-o);
                    g.drawText(""+(int)(y/scale), fnt,2, y+12-o);

                    for(int i=1; i<=4; i++) {
                        g.drawLine(20,y+i*10-o,w,y+i*10-o);
                    }
                }

                y+=step;
                if(y-o > getHeight()) break;
            }

        } else {
            int x = 0;
            int h = (int) getHeight();
            while(true) {
                if(x-o > -step) {
                    g.drawLine(x-o,10,x-o,h);
                    g.drawText(""+(int)(x/scale), fnt,x+3-o, 12);
                    for(int i=1; i<=4; i++) {
                        g.drawLine(x+i*10-o,20,x+i*10-o,h);
                    }
                }
                x+=step;
                if(x-o > getWidth()) break;
            }
        }

        //draw the mouse indicator
        if(lastMouse != null) {
            g.setPaint(FlatColor.BLUE);
            Point2D pt = NodeUtils.convertToScene((Node) lastMouse.getSource(), lastMouse.getX(), lastMouse.getY());
            NodeUtils.convertFromScene(this,pt);
            //TODO: joshm: I don't know why I need this adjustment, but we do.
            pt = new Point2D.Double(pt.getX()-60,pt.getY()-30);
            if(vertical){
                g.drawLine(0,pt.getY(),getWidth(),pt.getY());
            } else {
                g.drawLine(pt.getX(),0,pt.getX(),getHeight());
            }
        }

        for(Node child : children()) {
            g.translate(child.getTranslateX(),child.getTranslateY());
            child.draw(g);
            g.translate(-child.getTranslateX(),-child.getTranslateY());
        }
        
        g.setClipRect(oldBounds);
    }

    public void setDocument(final CanvasDocument doc) {
        this.doc = doc;
        if(doc instanceof SketchDocument) {
            final SketchDocument sdoc = (SketchDocument) doc;
            for(SketchDocument.Guideline g : sdoc.getGuidelines()) {
                if(g.isVertical() && !vertical) {
                    GuidelineHandle gl = new GuidelineHandle(this, sdoc, g);
                    gl.setTranslateY(17);
                    gl.setTranslateX(g.getPosition()-GuidelineHandle.size/2);
                    this.add(gl);
                    guideHandles.add(gl);
                }
                if(!g.isVertical() && vertical) {
                    GuidelineHandle gl = new GuidelineHandle(this, sdoc, g);
                    gl.setTranslateX(17);
                    gl.setTranslateY(g.getPosition()-GuidelineHandle.size/2);
                    this.add(gl);
                    guideHandles.add(gl);
                }
            }

            EventBus.getSystem().addListener(sdoc,CanvasDocument.DocumentEvent.PageGuidelineAdded, new Callback<CanvasDocument.DocumentEvent>(){
                public void call(CanvasDocument.DocumentEvent documentEvent) throws Exception {
                    Object target = documentEvent.getTarget();
                    if(target instanceof SketchDocument.Guideline) {
                        SketchDocument.Guideline g = (SketchDocument.Guideline) target;
                        if(vertical && !g.isVertical()) {
                            GuidelineHandle gl = new GuidelineHandle(Ruler.this, sdoc, g);
                            gl.setTranslateX(17);
                            gl.setTranslateY(g.getPosition()-GuidelineHandle.size/2);
                            Ruler.this.add(gl);
                            guideHandles.add(gl);
                        }
                        if(!vertical && g.isVertical()) {
                            GuidelineHandle gl = new GuidelineHandle(Ruler.this, sdoc, g);
                            gl.setTranslateY(17);
                            gl.setTranslateX(g.getPosition()-GuidelineHandle.size/2);
                            Ruler.this.add(gl);
                            guideHandles.add(gl);
                        }
                    }
                }
            });

            EventBus.getSystem().addListener(sdoc,CanvasDocument.DocumentEvent.PageGuidelineRemoved, new Callback<CanvasDocument.DocumentEvent>(){
                public void call(CanvasDocument.DocumentEvent documentEvent) throws Exception {
                    Object target = documentEvent.getTarget();
                    if(target instanceof SketchDocument.Guideline) {
                        SketchDocument.Guideline g = (SketchDocument.Guideline) target;
                        ListIterator<GuidelineHandle> it = guideHandles.listIterator();
                        while(it.hasNext()) {
                            GuidelineHandle gh = it.next();
                            if(gh.guideline == g) {
                                it.remove();
                                remove(gh);
                            }
                        }
                    }
                }
            });

        }
    }

    private class GuidelineHandle extends Control {
        private SketchDocument.Guideline guideline;
        private Ruler ruler;
        static final double size = 14;

        private GuidelineHandle(final Ruler ruler, final SketchDocument doc, final SketchDocument.Guideline guideline) {
            this.ruler = ruler;
            this.guideline = guideline;
            EventBus.getSystem().addListener(this,MouseEvent.MouseDragged,new Callback<MouseEvent>() {
                public void call(MouseEvent mouseEvent) throws Exception {
                    Point2D pt = mouseEvent.getPointInNodeCoords(ruler);
                    if(guideline.isVertical()) {
                        guideline.setPosition(pt.getX() + offset);
                        setTranslateX(guideline.getPosition()- size /2 - offset);

                    } else {
                        guideline.setPosition(pt.getY() + offset);
                        setTranslateY(guideline.getPosition()- size /2 - offset);
                    }
                }
            });
            EventBus.getSystem().addListener(this,MouseEvent.MouseReleased,new Callback<MouseEvent>() {
                public void call(MouseEvent mouseEvent) throws Exception {
                    Point2D pt = mouseEvent.getPointInNodeCoords(ruler);
                    if(guideline.isVertical()) {
                        if(pt.getX() < 0) {
                            doc.removeGuideline(guideline);
                        }
                    } else {
                        if(pt.getY() < 0) {
                            doc.removeGuideline(guideline);
                        }
                    }
                }
            });
            EventBus.getSystem().addListener(doc, CanvasDocument.DocumentEvent.PageGuidelineMoved, new Callback<CanvasDocument.DocumentEvent>(){
                public void call(CanvasDocument.DocumentEvent documentEvent) throws Exception {
                    if(documentEvent.getTarget() == guideline) {
                        if(guideline.isVertical()) {
                            setTranslateX(guideline.getPosition()- size /2 - offset);
                        } else {
                            setTranslateY(guideline.getPosition()- size /2 - offset);
                        }
                    }
                }
            });
            setWidth(size);
            setHeight(size);
        }

        @Override
        public void doLayout() {
            this.setWidth(size);
            this.setHeight(size);
        }

        @Override
        public void doPrefLayout() {

        }

        @Override
        public void doSkins() {

        }

        @Override
        public void draw(GFX gfx) {
            if(guideline.isVertical() && !vertical) {
                gfx.translate(size/2,0);
                DrawUtils.drawTriangleHandle(gfx,getWidth()/2,getHeight()/2,FlatColor.RED,true);
                gfx.translate(-size/2,0);
            }
            if(!guideline.isVertical() && vertical) {
                gfx.translate(0,size/2);
                DrawUtils.drawTriangleHandle(gfx,getWidth()/2,getHeight()/2,FlatColor.RED,false);
                gfx.translate(0,-size/2);
            }
        }
    }
}
