package org.joshy.sketch.actions.symbols;

import assetmanager.AssetDB;
import assetmanager.SymbolSetAsset;
import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.control.ListModel;
import org.joshy.gfx.node.control.ListView;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.OpenAction;
import org.joshy.sketch.actions.io.NativeExport;
import org.joshy.sketch.model.SNode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A manager for reusable symbols. 
 */
public class SymbolManager {
    private ListModel<SNode> model;
    private SymbolSet currentSet;
    private List<SymbolSet> list = new ArrayList<SymbolSet>();
    private AssetDB db;

    public SymbolManager(AssetDB assetDatabase) {
        this.db = assetDatabase;
        if(db.getAllSymbols().size() < 1) {
            loadDefaultSymbols();
        }
        for(SymbolSetAsset asset : db.getAllSymbols()) {
            list.add(new SymbolSet(asset));
        }
        currentSet = list.get(0);
        model = new ListModel<SNode>() {
            public SNode get(int i) {
                if(i < currentSet.getSymbols().size()) {
                    return currentSet.getSymbols().get(i);
                } else {
                    return null;
                }
            }
            public int size() {
                if(currentSet == null) return 0;
                return currentSet.getSymbols().size();
            }
        };
    }


    public void setCurrentSet(SymbolSet set) {
        currentSet = set;
        fireUpdate();
    }

    private void fireUpdate() {
        EventBus.getSystem().publish(new ListView.ListEvent(ListView.ListEvent.Updated,model));
    }

    public SymbolSet createNewSet(String name) {
        SymbolSetAsset asset = db.createSymbolSet(name);
        SymbolSet set = new SymbolSet(asset);
        list.add(set);
        return set;
    }

    public SymbolSet getSet(int i) {
        return list.get(i);
    }

    public Iterable<? extends SymbolSet> getSets() {
        return list;
    }

    public int getSetCount() {
        return list.size();
    }


    public static class SymbolSet {
        protected List<SNode> symbols;
        protected SymbolSetAsset asset;

        public SymbolSet(SymbolSetAsset asset) {
            this.asset = asset;
        }

        public String toString() {
            return ""+getName();
        }

        public CharSequence getName() {
            return asset.getName();
        }

        public List<SNode> getSymbols() {
            if(symbols == null) {
                symbols = new ArrayList<SNode>();
                try {
                    if(asset.getFile().exists()) {
                        List<SNode> shapes = OpenAction.loadShapes(asset.getFile(),null);
                        symbols.addAll(shapes);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return symbols;
        }

        public void addSymbol(SNode dupe) {
            symbols.add(dupe);
            try {
                saveSymbols();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private void saveSymbols() throws FileNotFoundException, UnsupportedEncodingException {
            u.p("persisting symbol set to: " + asset.getFile().getName());
            XMLWriter out = new XMLWriter(new PrintWriter(new OutputStreamWriter(new FileOutputStream(asset.getFile()), "UTF-8")),
                    asset.getFile().toURI());
            out.header();
            out.start("sketchy","version","-1");
            ExportProcessor.processFragment(new NativeExport(), out, getSymbols());
            out.end();
            out.close();
        }

    }

    public static class VirtualSymbolSet extends SymbolSet {

        private String name;

        public VirtualSymbolSet(String name) {
            super(null);
            this.name = name;
            this.symbols = new ArrayList<SNode>();
        }

        @Override
        public CharSequence getName() {
            return this.name;
        }

        @Override
        public void addSymbol(SNode dupe) {
            this.symbols.add(dupe);
        }
    }

    private void loadDefaultSymbols() {
        u.p("========= loading default symbol sets =========");
        try {
            db.copyAndAddSymbolSet(SymbolManager.class.getResourceAsStream("leo_common.xml"),"Common");
            db.copyAndAddSymbolSet(SymbolManager.class.getResourceAsStream("leo_mockups.xml"),"Mockups");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void add(SNode dupe) {
        currentSet.addSymbol(dupe);
        fireUpdate();
        u.p("total number of symbol sets = " + db.getAllSymbols().size());
    }
    
    public VirtualSymbolSet createVirtualSet(String name) {
        VirtualSymbolSet v = new VirtualSymbolSet(name);
        list.add(v);
        return v;
    }
    public void addVirtual(SNode node) {
        //virtuals.symbols.add(node);
    }

    public ListModel<SNode> getModel() {
        return model;
    }
    
    public void remove(SNode shape) {
        currentSet.symbols.remove(shape);
        try {
            currentSet.saveSymbols();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fireUpdate();
    }


}
