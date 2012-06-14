/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assetmanager;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.SelectionEvent;
import org.joshy.gfx.event.SystemMenuEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.FlexBox;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.ArrayListModel;
import org.joshy.sketch.actions.swatches.Palette;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author josh
 */
public class AssetManager {
    private static ListView<Query> sidebarList;
    private static TableView<Asset, String> tableView;
    private static LibraryQuery all;
    private static AssetDB db;

    public static void main(String[] args) throws Exception {
        Core.init();
        Core.getShared().defer(new Runnable() {
            public void run() {
                Stage stage = Stage.createStage();
                stage.centerOnScreen();

                stage.setContent(setupMain());
                stage.setWidth(800);
                stage.setHeight(600);

                EventBus.getSystem().addListener(SystemMenuEvent.All, new Callback<SystemMenuEvent>() {
                    public void call(SystemMenuEvent systemMenuEvent) throws Exception {
                        if (systemMenuEvent.getType() == SystemMenuEvent.Quit) {
                            System.exit(0);
                        }
                    }
                });
            }
        });
    }

    public static Control setupMain() {
        db = AssetDB.getInstance();

        VFlexBox vbox = new VFlexBox();
        vbox.setBoxAlign(FlexBox.Align.Stretch);

        HFlexBox toolbar = new HFlexBox();
        toolbar.add(new Button("Add"));
        toolbar.add(new Button("New List"));
        toolbar.add(new Button("Delete"));
        toolbar.add(new Button("Table View"));
        toolbar.add(new Button("Thumb View"));

        toolbar.add(new Textbox("").setHintText("search").setWidth(100),1.0);
        vbox.add(toolbar);


        HFlexBox main = new HFlexBox();
        main.setBoxAlign(FlexBox.Align.Stretch);
        Query library = new Query("LIBRARY","----");
        library.setSelectable(false);
        all = new LibraryQuery("Everything","*",0,1);
        Query fonts = new Query("Fonts",AssetDB.FONT,6,2);
        Query symbols = new Query("Symbols",AssetDB.SYMBOLSET,4,0);
        Query textures = new Query("Textures",AssetDB.PATTERN,10,0);
        Query gradients = new Query("Gradients","gradient",19,1);
        Query images = new Query("Images","image",19,2);
        Query palettes = new Query("Palettes",AssetDB.PALETTE,4,5);
        final Query staticList = new Query("LISTS","----");
        staticList.setSelectable(false);

        sidebarList = new ListView<Query>();
        ArrayListModel<Query> sidebarModel = new ArrayListModel<Query>();
        sidebarModel.add(library);
        sidebarModel.add(all);
        sidebarModel.add(staticList);
        sidebarModel.add(fonts);
        sidebarModel.add(symbols);
        sidebarModel.add(textures);
        sidebarModel.add(gradients);
        sidebarModel.add(images);
        sidebarModel.add(palettes);
        sidebarList.setModel(sidebarModel);

        sidebarList.setRenderer(new ListView.ItemRenderer<assetmanager.Query>() {
            public void draw(GFX gfx, ListView listView, Query query, int i, double x, double y, double w, double h) {
                if(query == null) return;
                if(!query.isSelectable()) {
                    gfx.setPaint(FlatColor.GRAY);
                    gfx.fillRect(x, y, w, h);
                    gfx.setPaint(FlatColor.BLACK);
                    Font.drawCenteredVertically(gfx, query.getName(), Font.DEFAULT, x, y, w, h, false);
                    return;
                }

                gfx.setPaint(FlatColor.WHITE);
                if(listView.getSelectedIndex() == i) {
                    gfx.setPaint(FlatColor.BLUE);
                }
                gfx.fillRect(x, y, w, h);
                gfx.setPaint(FlatColor.BLACK);
                if(listView.getSelectedIndex() == i) {
                    gfx.setPaint(FlatColor.WHITE);
                }
                Font.drawCenteredVertically(gfx, query.getName(), Font.DEFAULT, x, y, w, h, false);
            }
        });


        EventBus.getSystem().addListener(sidebarList, SelectionEvent.Changed, new Callback<SelectionEvent>() {
            public void call(SelectionEvent selectionEvent) throws Exception {
                Query query = sidebarList.getModel().get(sidebarList.getSelectedIndex());
                if(query == null || !query.isSelectable()) return;
                tableView.setModel(new AssetTableModel(query.execute(db)));

            }
        });

        tableView = new TableView<Asset,String>();
        /*
        tableView.setSorter(new TableView.Sorter() {
            public Comparator createComparator(TableView.TableModel tableModel, int col, TableView.SortOrder sortOrder) {
                return Collator.getInstance();
            }
        });
        */
        tableView.setModel(new AssetTableModel(new ArrayList<Asset>()));
        tableView.setRenderer(new TableView.DataRenderer<assetmanager.Asset>() {
            public void draw(GFX gfx, TableView tableView, Asset asset, int row, int column, double x, double y, double w, double h) {
                if(asset == null) return;
                gfx.setPaint(FlatColor.WHITE);
                gfx.fillRect(x,y,w,h);
                gfx.setPaint(FlatColor.BLACK);
                if(column == 0) {
                    Font.drawCenteredVertically(gfx, asset.getName(), Font.DEFAULT, x, y, w, h, false);
                }
                if(column == 1) {
                    Font.drawCenteredVertically(gfx, asset.getKind(), Font.DEFAULT, x, y, w, h, false);
                }
                if(column == 2) {
                    Image img = null;
                    if(asset.getKind().equals(AssetDB.PATTERN)) {
                        img = RenderUtil.patternToImage(asset);
                    }
                    if(asset.getKind().equals(AssetDB.PALETTE)) {
                        img = RenderUtil.toImage((Palette)asset);
                    }
                    if(asset.getKind().equals(AssetDB.FONT)) {
                        img = RenderUtil.fontToImage(asset);
                    }
                    if(img != null) {
                        Bounds oldClip = gfx.getClipRect();
                        gfx.setClipRect(new Bounds(x,y,w,h));
                        gfx.drawImage(img,x,y);
                        gfx.setClipRect(oldClip);
                    }
                }
            }
        });
        main.add(new ScrollPane(sidebarList).setPrefWidth(200));
        main.add(new ScrollPane(tableView).setPrefWidth(200),1.0);
        vbox.add(main,1.0);
        return vbox;
    }

    private static class AssetTableModel implements TableView.TableModel<Asset,String> {

        private List<Asset> data;

        public AssetTableModel(List<Asset> data) {
            this.data = data;
        }

        public int getRowCount() {
            return data.size();
        }

        public int getColumnCount() {
            return 3;
        }

        public String getColumnHeader(int col) {
            switch(col) {
                case 0: return "Name";
                case 1: return "Kind";
                case 2: return "Preview";
                default: return "col?";
            }
        }

        public Asset get(int row, int col) {
            if(row >= data.size()) return null;
            return data.get(row);
        }
    }
}
