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
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.HAlign;
import org.joshy.gfx.node.layout.HBox;
import org.joshy.gfx.node.layout.Panel;
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
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 1, 2010
 * Time: 4:52:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class FlickrPanel extends Panel {
    private ListView photoList;
    private List<Photo> photos;
    private Textbox searchBox;
    private MasterImageCache imageCache;
    private ScrollPane scrollPane;
    private VectorDocContext context;

    public static void main(String ... args) throws InvocationTargetException, InterruptedException {
        Core.init();
        Core.getShared().defer(new Runnable(){
            public void run() {
                new FlickrPanel(null).doSearch.call(null);
            }
        });
    }
    public FlickrPanel(final VectorDocContext context) {
        this.context = context;
        imageCache = new MasterImageCache(false,10,"foo");
        photos = new ArrayList<Photo>();
        photoList = new ListView();
        photoList.setRowHeight(75);
        photoList.setModel(new ListModel<Photo>(){
            public Photo get(int i) {
                if(i < photos.size()) {
                    return photos.get(i);
                } else {
                    return null;
                }
            }
            public int size() {
                return photos.size();
            }
        });
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
                    gfx.setPaint(new FlatColor(0.0,0.0,0.0,0.8));
                    gfx.drawText(item.getTitle(), Font.name("Helvetica").size(12).resolve(),x+5+1,y+17+1);
                    //gfx.drawText(item.getTitle(), Font.name("Helvetica").size(12).resolve(),x+5-1,y+17-1);
                    gfx.setPaint(new FlatColor(0.9,0.9,0.9,1.0));
                    gfx.drawText(item.getTitle(), Font.name("Helvetica").size(12).resolve(),x+5,y+17);
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
        scrollPane.setContent(photoList);
        searchBox = new Textbox();
        HBox box = new HBox();
        //box.setFill(FlatColor.GRAY);
        box.setHAlign(HAlign.TOP);
        box.setHeight(35);
        box.add(searchBox,new Button("Search").onClicked(doSearch));
        this.add(box);

        this.add(scrollPane);
    }

    @Override
    public void doLayout() {
        for(Control c : controlChildren()) {
            c.doLayout();
            if(c == scrollPane){
                c.setWidth(getWidth());
                c.setHeight(getHeight()-30);
                c.setTranslateY(30);
            }
        }
    }

    Callback<ActionEvent> doSearch = new Callback<ActionEvent>() {
        public void call(ActionEvent event) {
            u.p("doing a search on flickr");
            try {
                XMLRequest req = new XMLRequest().setURL("http://api.flickr.com/services/rest/?");//?method=flickr.test.echo&name=value");
                req.setMethod(XMLRequest.METHOD.GET);
                req.setParameter("api_key","7659144a7b5510b894b33cb105737618");
                req.setParameter("method","flickr.photos.search");
                req.setParameter("tags",searchBox.getText());
                req.setParameter("sort","relevance");
                req.setParameter("license","4,5,7");
                //http://farm{farm-id}.static.flickr.com/{server-id}/{id}_{secret}.jpg
                //http://farm5.static.flickr.com/4102/4753003371_6e12cf0eb7.jpg

                //http://www.flickr.com/photos/{user-id}/{photo-id} - individual photo
                //http://api.flickr.com/services/rest/?method=flickr.test.echo&name=foo&api_key=7659144a7b5510b894b33cb105737618
                //req.setURL("http://api.flickr.com/services/rest/?method=flickr.test.echo&name=foo&api_key=7659144a7b5510b894b33cb105737618");
                req.onComplete(new Callback<Doc>() {
                    public void call(Doc doc) {
                        doc.dump();
                        photos.clear();
                        try {
                            for(Elem photo : doc.xpath("//photo")) {
                                /*
                                u.p("http://farm"
                                        +photo.attr("farm")
                                        +".static.flickr.com/"
                                        +photo.attr("server")
                                        +"/"+photo.attr("id")+"_"+photo.attr("secret")+"_s.jpg");
                                u.p("http://www.flickr.com/photos/"+photo.attr("owner")+"/"+photo.attr("id"));*/
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