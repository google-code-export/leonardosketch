package org.joshy.sketch.pixel.model;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.Event;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.SystemMenuEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.stage.Stage;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/17/11
 * Time: 5:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class PixelDocTest implements Runnable {
    public static void main(String ... args) throws Exception {
        Core.init();
        Core.getShared().defer(new PixelDocTest());
    }

    public void run() {

        EventBus.getSystem().addListener(SystemMenuEvent.Quit, new Callback<Event>() {
            public void call(Event event) throws Exception {
                System.exit(0);
            }
        });
        PixelDoc doc = new PixelDoc();
        PixelGraphics g = null;


        //draw a purple rect to layer 1
        PixelLayer layer1 = new PixelLayer(doc);
        doc.add(layer1);
        g = layer1.getGraphics();
        g.setFill(FlatColor.PURPLE);
        g.fillRect(200,100,100,100); // this should span one tile to the right


        //draw a translucent green rect to layer 2
        PixelLayer layer2 = new PixelLayer(doc);
        doc.add(layer2);
        g = layer2.getGraphics();
        g.setFill(new FlatColor(0,1,0,0.5));
        g.fillRect(150,150,200,200);


        //draw an oval filled with blue
        PixelLayer layer3 = new PixelLayer(doc);
        doc.add(layer3);
        g = layer3.getGraphics();
        g.setFill(FlatColor.BLUE);
        g.fillOval(40,300,100,60,true);

        //draw an oval filled with red, then make a rectangular selection and fill it with green
        PixelLayer layer4 = new PixelLayer(doc);
        doc.add(layer4);
        g = layer4.getGraphics();
        g.setFill(FlatColor.RED);
        g.fillOval(100,50,100,60,true);
        PixelSelection selection = new PixelSelection(doc);
        selection.addRect(100,50,30,200);
        g.setFill(FlatColor.GREEN);
        g.fillSelection(selection);

        //fill a rectangular selection with a difference cloud
        PixelLayer layer5 = new PixelLayer(doc);
        doc.add(layer5);
        g = layer5.getGraphics();
        g.fillDisplacementClouds(20,20,100,200);




        Stage stage = Stage.createStage();
        stage.setContent(new PixelCanvasNode(doc));
        stage.setWidth(800);
        stage.setHeight(600);
    }

    private static class PixelCanvasNode extends Node {
        private PixelDoc doc;

        private PixelCanvasNode(PixelDoc doc) {
            this.doc = doc;
        }

        @Override
        public void draw(GFX gfx) {
            //u.p("------------- drawing");
            for(PixelLayer layer : doc.getLayers()) {
                for(int y=0; y<600; y+=256) {
                    for(int x=0; x<600; x+=256) {
                        PixelTile tile = layer.getTile(x/256,y/256);
                        if(tile != null) {
                            //u.p("drawing tile at : " + x + " " + y);
                            Image image = tile.getImage();
                            if(image != null) {
                                gfx.drawImage(image,x,y);
                            }
                        }
                        //tile grid lines
                        gfx.setPaint(FlatColor.GRAY);
                        //gfx.drawRect(x,y,256,256);
                    }
                }
            }
        }

        @Override
        public Bounds getVisualBounds() {
            return new Bounds(0,0,600,600);
        }

        @Override
        public Bounds getInputBounds() {
            return getVisualBounds();
        }
    }
}
