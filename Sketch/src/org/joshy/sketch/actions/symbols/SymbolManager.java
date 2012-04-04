package org.joshy.sketch.actions.symbols;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.control.ListModel;
import org.joshy.gfx.node.control.ListView;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.OpenAction;
import org.joshy.sketch.actions.io.NativeExport;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.util.Util;

import java.io.*;
import java.util.*;

/**
 * A manager for reusable symbols. 
 */
public class SymbolManager {
    private ListModel<SNode> model;
    
    public Map<File,SymbolSet> sets = new HashMap<File,SymbolSet>();
    private SymbolSet currentSet;
    private File basedir;
    private List<SymbolSet> list = new ArrayList<SymbolSet>();
    private SymbolSet virtuals = new SymbolSet();

    public SymbolManager(File file) {
        basedir = file;
        try {
            loadSymbols(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        sets.put(new File("virtual"),virtuals);
        list.add(virtuals);

        model = new ListModel<SNode>() {
            public SNode get(int i) {
                if(i < currentSet.symbols.size()) {
                    return currentSet.symbols.get(i);
                } else {
                    return null;
                }
            }
            public int size() {
                if(currentSet == null) return 0;
                if(currentSet.symbols == null) return 0;
                return currentSet.symbols.size();
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
        SymbolSet set = new SymbolSet();
        set.file = new File(basedir,name+".xml");
        sets.put(set.file,set);
        list.add(set);
        return set;
    }

    public SymbolSet getSet(int i) {
        return list.get(i);
    }

    public Iterable<? extends SymbolSet> getSets() {
        return sets.values();
    }


    public static class SymbolSet {
        private List<SNode> symbols = new ArrayList<SNode>();
        public File file;
        public String toString() {
            return ""+getName();
        }

        public CharSequence getName() {
            if(file != null && file.getName() != null) {
                return file.getName();
            }

            return "unknown";
        }
    }

    private void loadSymbols(File basedir) throws Exception {
        if(!basedir.exists()) {
            boolean success = basedir.mkdirs();
            if(!success) {
                if(sets.isEmpty()) {
                    SymbolSet set = new SymbolSet();
                    set.file = new File(basedir,"default.xml");
                    sets.put(set.file,set);
                    list.add(set);
                    currentSet = set;
                }
                throw new Exception("Error making the directory: " + basedir);
            }
        }

        try {
            File COMMON_SYMBOLS = new File(basedir, "leo_common.xml");
            if(!COMMON_SYMBOLS.exists()) {
                Util.copyToFile(
                        SymbolManager.class.getResourceAsStream("leo_common.xml")
                        ,COMMON_SYMBOLS);
            }
            File MOCKUPS_SYMBOLS = new File(basedir, "leo_mockups.xml");
            if(!MOCKUPS_SYMBOLS.exists()) {
                Util.copyToFile(
                        SymbolManager.class.getResourceAsStream("leo_mockups.xml")
                        ,MOCKUPS_SYMBOLS);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        for(File file : basedir.listFiles()) {
            if(file.getName().endsWith(".xml") && file.exists()) {
                List<SNode> shapes = OpenAction.loadShapes(file,null);
                SymbolSet set = new SymbolSet();
                set.file = file;
                set.symbols = shapes;
                currentSet = set;
                sets.put(file,set);
                list.add(set);
            }
        }
        if(sets.isEmpty()) {
            SymbolSet set = new SymbolSet();
            set.file = new File(basedir,"default.xml");
            sets.put(set.file,set);
            list.add(set);
            currentSet = set;
        }
        fireUpdate();
    }

    public void add(SNode dupe) {
        currentSet.symbols.add(dupe);
        fireUpdate();
        try {
            saveSymbols(currentSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addVirtual(SNode node) {
        virtuals.symbols.add(node);
        fireUpdate();
    }

    public void save() {
        try {
            saveSymbols(currentSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ListModel<SNode> getModel() {
        return model;
    }
    
    public void remove(SNode shape) {
        currentSet.symbols.remove(shape);
        fireUpdate();
        save();
    }

    private void saveSymbols(SymbolSet set) throws FileNotFoundException, UnsupportedEncodingException {
        XMLWriter out = new XMLWriter(new PrintWriter(new OutputStreamWriter(new FileOutputStream(set.file), "UTF-8")),
                set.file.toURI());
        out.header();
        out.start("sketchy","version","-1");
        ExportProcessor.processFragment(new NativeExport(), out, set.symbols);
        out.end();
        out.close();
    }

}
