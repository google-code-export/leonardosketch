package org.joshy.sketch.actions.flickr;

import com.joshondesign.xml.Doc;
import com.joshondesign.xml.Elem;
import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.util.ArrayListModel;
import org.joshy.gfx.util.image.MasterImageCache;
import org.joshy.gfx.util.image.SizingMethod;
import org.joshy.gfx.util.u;
import org.joshy.gfx.util.xml.XMLRequest;
import org.joshy.sketch.model.SImage;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.modes.vector.VectorDocContext;

import javax.xml.xpath.XPathExpressionException;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

import static org.joshy.gfx.util.localization.Localization.getString;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 1, 2010
 * Time: 4:52:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class FlickrPanel extends VFlexBox {
    private ListView photoList;
    private ArrayListModel<Photo> photos;
    private Textbox searchBox;
    private MasterImageCache imageCache;
    private ScrollPane scrollPane;
    private VectorDocContext context;

    public static void main(String ... args) throws Exception {
        Core.init();
        Core.getShared().defer(new Runnable(){
            public void run() {
                try {
                    new FlickrPanel(null).doSearch.call(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public FlickrPanel(final VectorDocContext context) {
        this.context = context;
        imageCache = new MasterImageCache(false,10,"foo");
        photos = new ArrayListModel<Photo>();
        photoList = new ListView();
        photoList.setRowHeight(75);
        photoList.setModel(photos);

        EventBus.getSystem().addListener(photoList, MouseEvent.MouseAll, new Callback<MouseEvent>() {
            public double prevx;
            public boolean created;
            public SNode dupe;

            public void call(MouseEvent event) {
                if(event.getType() == MouseEvent.MouseDragged) {
                    if(created && dupe != null) {
                        dupe.setTranslateX(event.getPointInNodeCoords(context.getCanvas()).getX());
                        dupe.setTranslateY(event.getPointInNodeCoords(context.getCanvas()).getY());
                        context.redraw();
                    }
                    if(event.getX() < 0 && prevx >= 0 && !created) {
                        created = true;
                        if(photoList.getSelectedIndex() < 0) return;
                        Photo photo  = (Photo) photoList.getModel().get(photoList.getSelectedIndex());
                        try {
                            SketchDocument sd = context.getDocument();
                            dupe = new SImage(photo.getFullURL().toURI(),true, photo.getImage(), context);
                            sd.getCurrentPage().model.add(dupe);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        context.redraw();
                    }
                    prevx = event.getX();
                }
                if(event.getType() == MouseEvent.MouseReleased) {
                    if(created) {
                        context.getSketchCanvas().selection.setSelectedNode(dupe);
                        dupe = null;
                        created = false;
                        prevx = 0;
                        Core.getShared().getFocusManager().setFocusedNode(context.getSketchCanvas());
                    }
                    context.redraw();
                }
            }
        });

        photoList.setRenderer(new ListView.ItemRenderer<Photo>(){
            public void draw(GFX gfx, ListView listView, Photo item, int index, double x, double y, double width, double height) {
                gfx.setPaint(FlatColor.BLACK);
                gfx.fillRect(x,y,width,height);
                if(item != null) {

                    if(item.isLoaded()) {
                        gfx.drawImage(item.getImage(),x,y);
                    }
                    gfx.setPaint(FlatColor.WHITE);
                    drawWrappedText(gfx, item.getTitle(), Font.name("Helvetica").size(12).resolve(),x+80,y+17,110);
                }
                if(index == listView.getSelectedIndex()) {
                    gfx.setPaint(new FlatColor(1.0,1.0,1.0,0.2));
                    gfx.fillRect(x,y,width,height);
                }
                gfx.setPaint(FlatColor.WHITE);
                gfx.drawRect(x,y,width,height);
            }
        });
        scrollPane = new ScrollPane();
        scrollPane.setHorizontalVisiblePolicy(ScrollPane.VisiblePolicy.Never);
        scrollPane.setContent(photoList);
        searchBox = new Textbox();
        searchBox.setHintText(getString("sidebar.flickr.search.hint"));
        searchBox.onAction(doSearch);
        Control box = new HFlexBox()
                .setBoxAlign(HFlexBox.Align.Baseline)
                .add(searchBox,1)
                .add(new Button(getString("sidebar.search")).onClicked(doSearch),0)
                ;

        this.setBoxAlign(VFlexBox.Align.Stretch);
        this.add(box,0);
        this.add(scrollPane,1);
    }

    private void drawWrappedText(GFX g, String text, Font font, double x, double y, double width) {
        String line = "";
        String[] words = text.split(" ");
        for(String word : words) {
            if(font.calculateWidth(line) > width){
                g.drawText(line, font, x, y);
                y += font.calculateHeight(line);
                line = "";
            }
            line += (word + " ");
        }
        g.drawText(line, font, x, y);
    }


    Callback<ActionEvent> doSearch = new Callback<ActionEvent>() {
        public void call(ActionEvent event) {
            try {
                XMLRequest req = new XMLRequest().setURL("http://api.flickr.com/services/rest/?");//?method=flickr.test.echo&name=value");
                req.setMethod(XMLRequest.METHOD.GET);
                req.setParameter("api_key","7659144a7b5510b894b33cb105737618");
                req.setParameter("method","flickr.photos.search");
                req.setParameter("tags",searchBox.getText().replaceAll("\\s+",","));
                req.setParameter("sort","relevance");
                req.setParameter("license","4,5,7");
                req.onComplete(new Callback<Doc>() {
                    public void call(Doc doc) {
                        doc.dump();
                        photos.clear();
                        try {
                            for(Elem photo : doc.xpath("//photo")) {
                                Photo ph = new Photo(
                                        context,
                                        photo.attr("farm"),
                                        photo.attr("server"),
                                        photo.attr("id"),
                                        photo.attr("secret"),
                                        photo.attr("owner"),
                                        photo.attr("title")
                                );
                                photos.add(ph);
                                ph.load();
                            }
                        } catch (XPathExpressionException e) {
                            e.printStackTrace();
                        }
                        context.redraw();
                        //scroll back to the top
                        scrollPane.scrollToShow(new Bounds(0,0,10,10));
                    }
                });
                req.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private class Photo {
        private String id;
        private String title;
        private String farm;
        private String server;
        private String secret;
        private String owner;
        private boolean loaded;
        private Image image;
        private String ID;
        private DocContext context;

        public Photo(DocContext context, String farm, String server, String id, String secret, String owner, String title) {
            this.context = context;
            this.farm = farm;
            this.server = server;
            this.id = id;
            this.secret = secret;
            this.owner = owner;
            this.title = title;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public URL getThumbnailURL() throws MalformedURLException {
            return new URL("http://farm"+farm+".static.flickr.com/"+server+"/"+id+"_"+secret+"_s.jpg");
        }
        public URL getFullURL() throws MalformedURLException {
            return new URL("http://farm"+farm+".static.flickr.com/"+server+"/"+id+"_"+secret+".jpg");
        }

        public void load() {
            try {
                imageCache.getImage(getThumbnailURL().toString(),null,75,75, SizingMethod.Preserve,new MasterImageCache.Callback() {
                    public void fullImageLoaded(BufferedImage image) {
                        u.p("image loaded!");
                        loaded = true;
                        Photo.this.image = Image.create(image);
                        context.redraw();
                    }
                    public void thumnailImageLoaded(BufferedImage image) {
                    }
                    public void error() {
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }

        public boolean isLoaded() {
            return loaded;
        }

        public Image getImage() {
            return image;
        }

        public String getID() {
            return ID;
        }
    }
}
