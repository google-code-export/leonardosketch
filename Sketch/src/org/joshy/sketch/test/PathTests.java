package org.joshy.sketch.test;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.u;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jun 10, 2010
 * Time: 3:45:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathTests implements Runnable {
    static P p1 = new P(0,0);
    static P p2 = new P(0,100);
    static P p3 = new P(100,100);
    static P p4 = new P(100,0);
    private P realClosest;
    private MyNode node;

    public static void main(String ... args) throws InvocationTargetException, InterruptedException {
        Core.setUseJOGL(false);
        Core.init();
        Core.getShared().defer(new PathTests());
    }

    public void run() {
        Stage stage = Stage.createStage();
        EventBus.getSystem().addListener(MouseEvent.MouseMoved, new Callback<MouseEvent>(){
            public void call(MouseEvent event) {
                //u.p("-----");
                P closest = calculatePoint(p1,p2,p3,p4,0);
                double closestDistance = calculateDistance(event.getX(),event.getY(),closest);
                double closestT = 0;
                for(double t=0; t<=1.0; t+=0.01) {
                    P b = calculatePoint(p1,p2,p3,p4,t);
                    double distance = calculateDistance(event.getX(),event.getY(),b);
                    if(distance < closestDistance) {
                        closestDistance = distance;
                        closest = b;
                        closestT = t;
                    }
                }
                //u.p("point = " + closest.x + " " + closest.y + " " + closestDistance + " " + closestT);
                realClosest = closest;
                node.redraw();
            }

            private double calculateDistance(double x, double y, P b) {
                double dx = x-b.x;
                double dy = y-b.y;
                double distance = Math.sqrt(dx*dx+dy*dy);
                return distance;
            }
        });
        node = new MyNode();

        stage.setContent(node);
    }

    private void drawx(GFX g) {
        g.setPaint(FlatColor.RED);
        P a = calculatePoint(p1,p2,p3,p4,0);
        for(double t=0; t<=1.0; t+=0.01) {
            P b = calculatePoint(p1,p2,p3,p4,t);
            g.drawLine(b.x+1,b.y+1,b.x,b.y);
            a = b;
        }

        g.setPaint(FlatColor.BLUE);
        g.drawLine(realClosest.x,realClosest.y,realClosest.x+1,realClosest.y+1);
    }

    private static P calculatePoint(P p1, P p2, P p3, P p4, double mu) {
        double mum1 = 1 - mu;
        double mum13 = mum1 * mum1 * mum1;
        double mu3 = mu*mu*mu;

        P p = new P();
        p.x = mum13*p1.x + 3*mu*mum1*mum1*p2.x + 3*mu*mu*mum1*p3.x + mu3*p4.x;
        p.y = mum13*p1.y + 3*mu*mum1*mum1*p2.y + 3*mu*mu*mum1*p3.y + mu3*p4.y;
        return p;
    }

    public static class P {
        private double x;
        private double y;

        public P(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public P() {
        }
    }

    private class MyNode extends Node {

        @Override
        public void draw(GFX g) {
            //g.translate(100,100);
            drawx(g);
            //g.translate(-100,-100);
        }

        @Override
        public Bounds getVisualBounds() {
            return new Bounds(0,0,100,100);
        }

        @Override
        public Bounds getInputBounds() {
            return getVisualBounds();
        }

        public void redraw() {
            setDrawingDirty();
        }
    }
}
