/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assetmanager;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;

/**
 *
 * @author josh
 */
public class AssetDB {
    private static final String KIND = "kind";
    public static final String FONT = "font";
    public static final String PATTERN = "pattern";
    private static final String FILEPATH = "filepath";
    private static final String NAME = "name";
    private static final String STATIC_LIST = "STATIC_LIST";
    
    private EmbeddedGraphDatabase graphDb;
    private Index<Node> kindsIndex;
    private File resourceDir;
    private final File patternDir;
    private final File fontDir;
    private static AssetDB _db;

    public AssetDB() {
        resourceDir = new File("/Users/josh/Library/Preferences/Leonardo/");
        resourceDir.mkdir();
        patternDir = new File(resourceDir,"patterns");
        patternDir.mkdir();
        fontDir = new File(resourceDir,"fonts");
        fontDir.mkdir();
        
        initDatabase();
        deleteAll();
        initInternalTypes();
    }

    private List<Asset> toAssetList(IndexHits<Node> ret) {
        List<Asset> assets = new ArrayList<Asset>();
        for(Node n : ret) {
            assets.add(toAsset(n));
        }
        return assets;
    }
    
    private Asset toAsset(Node n) {
        String name = (String) n.getProperty(NAME);
        String kind = (String) n.getProperty(KIND);
        String filepath = (String) n.getProperty(FILEPATH);
        Asset asset = new Asset(name,kind,filepath,n.getId());
        return asset;
    }

    private void p(String string) {
        System.out.println(string);
    }

    private static void copyFileToFile(File srcfile, File outfile) throws IOException {
        FileInputStream in = new FileInputStream(srcfile);
        FileOutputStream out = new FileOutputStream(outfile);
        byte[] buf = new byte[1024];
        while(true) {
            int n = in.read(buf);
            if(n == -1) {
                break;
            }
            out.write(buf,0,n);
        }
        in.close();
        out.close();
    }

    private static void copyStreamToFile(InputStream in, File outfile) throws IOException {
        FileOutputStream out = new FileOutputStream(outfile);
        byte[] buf = new byte[1024];
        while(true) {
            int n = in.read(buf);
            if(n == -1) {
                break;
            }
            out.write(buf,0,n);
        }
        in.close();
        out.close();
    }

    private String randomString(String prefix) {
        return prefix+Math.random();
    }

    public static AssetDB getInstance() {
        if(_db == null) {
            _db = new AssetDB();
        }
        return _db;
    }



    private static enum RelTypes implements RelationshipType {
        KNOWS, OWNED
    }

    private void initDatabase() {
        graphDb = new EmbeddedGraphDatabase(new File("mydb").getAbsolutePath());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("shutting down the database");
                graphDb.shutdown();
            }
        });
        kindsIndex = graphDb.index().forNodes("kinds");
    }  
    
    private void initInternalTypes() {
        Transaction tx = graphDb.beginTx();
        try {
            /*
            addFont("Open Sans");
            addFont("League Gothic");
            addFont("Helvetica");
            addFont("Driftwood");
            */
            
            
            for(File file : patternDir.listFiles()) {
                addPatternFast(file);
            }
            
            for(File file : fontDir.listFiles()) {
                addFontFast(file);
            }
            
            int count = kindsIndex.get(KIND,FONT).size();
            System.out.println("initting");
            tx.success();
        } finally {
            tx.finish();
        }
    }
    
    
    
    
    
    private Node addFont(String name) {
        Node asset = graphDb.createNode();
        asset.setProperty(KIND, FONT);
        asset.setProperty(NAME, name);
        asset.setProperty(FILEPATH, "unknown");
        kindsIndex.add(asset, KIND, FONT);
        kindsIndex.add(asset, NAME, name.toLowerCase());
        return asset;
    }
    
    private Node addFontFast(File file) {
        p("adding font from file: " + file.getAbsolutePath());

        String name = file.getName();
        try {
            Font fnt = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(file));
            p("loaded up the font");
            p("font name = " + fnt.getFontName());
            p("name = " + fnt.getName());
            p("family = " + fnt.getFamily());
            name = fnt.getName();
        } catch (FontFormatException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Node asset = graphDb.createNode();
        asset.setProperty(KIND, FONT);
        asset.setProperty(NAME, name);
        asset.setProperty(FILEPATH, file.getAbsolutePath());
        kindsIndex.add(asset, KIND, FONT);
        kindsIndex.add(asset, NAME, name.toLowerCase());
        return asset;
    }
    
    private void addPatternFast(File file) {
        p("adding pattern from file: " + file.getAbsolutePath());
        Node asset = graphDb.createNode();
        asset.setProperty(KIND, PATTERN);
        asset.setProperty(NAME, file.getName());
        asset.setProperty(FILEPATH,file.getAbsolutePath());
        kindsIndex.add(asset, KIND, PATTERN);
        kindsIndex.add(asset, NAME, file.getName().toLowerCase());
    }
    
    public StaticQuery createStaticList(String name) {
        Transaction tx = graphDb.beginTx();
        try {
            Node list = graphDb.createNode();
            list.setProperty(KIND, STATIC_LIST);
            list.setProperty(NAME, name);

            /*
            Node font1 = addFont("foo font");
            Node font2 = addFont("bar font");
            font1.createRelationshipTo(list, RelTypes.OWNED);
            font2.createRelationshipTo(list, RelTypes.OWNED);
            */

            tx.success();
            return new StaticQuery(name, list.getId());
        } finally {
            tx.finish();
        }
    }

    void addToStaticList(StaticQuery staticQuery, Asset asset) {
        p("adding asset: " + asset.name + " to query: " + staticQuery.getName());
        Transaction tx = graphDb.beginTx();
        try { 
            Node list = graphDb.getNodeById(staticQuery.listid);
            Node assetNode = graphDb.getNodeById(asset.id);
            assetNode.createRelationshipTo(list,RelTypes.OWNED);
            tx.success();
        } finally {
            tx.finish();
        }
    }
    
    void removeFromStaticList(StaticQuery staticQuery, Asset asset) {
        p("removing asset: " + asset.name + " from query " + staticQuery.getName());
        Transaction tx = graphDb.beginTx();
        try { 
            Node list = graphDb.getNodeById(staticQuery.listid);
            Node assetNode = graphDb.getNodeById(asset.id);
            Relationship rel = assetNode.getSingleRelationship(RelTypes.OWNED, Direction.BOTH);
            if(rel != null) rel.delete();
            tx.success();
        } finally {
            tx.finish();
        }
    }

    
    List<Asset> getStaticList(long listid) {
        Node list = graphDb.getNodeById(listid);
        List<Asset> assets = new ArrayList<Asset>();
        for(Relationship rel : list.getRelationships(RelTypes.OWNED,Direction.INCOMING)) {
            assets.add(toAsset(rel.getStartNode()));
        }
        return assets;
    }
    
    public void copyAndAddPattern(File file) throws IOException {
        
        File file2 = new File(patternDir,"pattern-"+Math.random()+".png");
        copyFileToFile(file,file2);
        Transaction tx = graphDb.beginTx();
        try {
            addPatternFast(file2);
            tx.success();
        } finally {
            tx.finish();
        }
    }
    

    public void copyAndAddFont(File file) throws IOException {
        File file2 = new File(fontDir,"font-"+Math.random()+".font");
        copyFileToFile(file,file2);
        Transaction tx = graphDb.beginTx();
        try {
            addFontFast(file2);
            tx.success();
        } finally {
            tx.finish();
        }
    }

    public Asset copyAndAddFont(URL font) throws IOException {
        
        File file2 = new File(fontDir,"font-"+Math.random()+".font");
        p("font = " + font);
        copyStreamToFile(font.openStream(), file2);
        Transaction tx = graphDb.beginTx();
        try {
            Node assetNode = addFontFast(file2);
            tx.success();
            return toAsset(assetNode);
        } finally {
            tx.finish();
        }
    }

    
    public List<Asset> getAllAssets() {
        Transaction tx = graphDb.beginTx();
        IndexHits<Node> ret;
        try {
            ret = kindsIndex.query(KIND, "*");
            p("got back: " + ret.size());
            tx.success();
        } finally {
            tx.finish();
        }
        return toAssetList(ret);
    }
    
    public List<Asset> getAllFonts() {
        Transaction tx = graphDb.beginTx();
        IndexHits<Node> ret;
        try {
            ret = kindsIndex.get(KIND,FONT);
            tx.success();
        } finally {
            tx.finish();
        }
        return toAssetList(ret);
    }
    
    public Asset getFontByName(String fontName) {
        for(Node n : kindsIndex.get(KIND,FONT)) {
            if(n.getProperty(NAME).equals(fontName)) {
                return toAsset(n);
            }
        }
        return null;
    }
    
    public List<Asset> getByKind(String kind) {
        if("*".equals(kind)) return getAllAssets();
        
        Transaction tx = graphDb.beginTx();
        IndexHits<Node> ret;
        try {
            ret = kindsIndex.get(KIND,kind);
            tx.success();
        } finally {
            tx.finish();
        }
        return toAssetList(ret);
    }
    public List<Asset> getAllPatterns() {
        Transaction tx = graphDb.beginTx();
        IndexHits<Node> ret;
        try {
            ret = kindsIndex.get(KIND,PATTERN);
            tx.success();
        } finally {
            tx.finish();
        }
        return toAssetList(ret);
    }
    

    private void deleteAll() {
        final String KIND = "kind";
        Transaction tx = graphDb.beginTx();
        try {
            for(Node n : kindsIndex.query(KIND,"*")) {
                kindsIndex.remove(n);
                for(Relationship r : n.getRelationships()) {
                    r.delete();
                }
                n.delete();
            }
            int count = kindsIndex.get(KIND,"font").size();
            System.out.println("there are " + count + " fonts now");
            tx.success();
        } finally {
            tx.finish();
        }
    }
    
    
    public List<Asset> searchByAnyText(String query) {
        p("searching for " + query);
        if(query == null || query.length() < 3) {
            return getAllAssets();
        }
        Transaction tx = graphDb.beginTx();
        IndexHits<Node> ret;
        try {
            ret = kindsIndex.query(NAME,"*"+query.toLowerCase()+"*");
            tx.success();
        } finally {
            tx.finish();
        }
        return toAssetList(ret);
    }
    
}
