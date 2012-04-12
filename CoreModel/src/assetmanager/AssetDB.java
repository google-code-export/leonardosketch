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

import org.joshy.gfx.util.u;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;

/**
 *
 * @author josh
 */
public class AssetDB {
    public static final String KIND =           "kind";
    public static final String FONT =           "font";
    public static final String PATTERN =        "pattern";
    public static final String FILEPATH =       "filepath";
    public static final String NAME =           "name";
    public static final String STATIC_LIST =    "STATIC_LIST";
    
    private EmbeddedGraphDatabase graphDb;
    private Index<Node> kindsIndex;
    private Index<Node> listsIndex;
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
        //deleteAll();
        //initInternalTypes();
    }

    private List<Asset> toAssetList(IndexHits<Node> ret) {
        List<Asset> assets = new ArrayList<Asset>();
        for(Node n : ret) {
            Asset asset = toAsset(n);
            if(asset == null) continue;
            assets.add(asset);
        }
        return assets;
    }

    private List<StaticQuery> toStaticListList(IndexHits<Node> ret) {
        List<StaticQuery> queries = new ArrayList<StaticQuery>();
        for(Node n : ret) {
            StaticQuery sq = toStaticList(n);
            if(sq == null) continue;
            queries.add(sq);
        }
        return queries;
    }

    private StaticQuery toStaticList(Node n) {
        String name = (String) n.getProperty(NAME);
        StaticQuery sq = new StaticQuery(this, name, n);
        return sq;
    }

    private Asset toAsset(Node n) {
        String kind = (String) n.getProperty(KIND);
        if(STATIC_LIST.equals(kind)) return null;
        String filepath = (String) n.getProperty(FILEPATH);
        Asset asset = new Asset(this,n,filepath,n.getId());
        return asset;
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

    public static AssetDB getInstance() {
        if(_db == null) {
            _db = new AssetDB();
        }
        return _db;
    }

    public Node getNodeById(long id) {
        return this.graphDb.getNodeById(id);
    }

    public Transaction beginTx() {
        return graphDb.beginTx();
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
        listsIndex = graphDb.index().forNodes("lists");


        u.p("font count = " + kindsIndex.get(KIND,FONT).size());
        u.p("pattern count = " + kindsIndex.get(KIND,PATTERN).size());
        u.p("static lists = " + listsIndex.get(KIND,STATIC_LIST).size());
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

        String name = file.getName();
        try {
            Font fnt = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(file));
            name = fnt.getName();
        } catch (Exception e) {
            e.printStackTrace();
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
        u.p("adding pattern from file: " + file.getAbsolutePath());
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
            listsIndex.add(list,KIND,STATIC_LIST);
            tx.success();
            return new StaticQuery(this, name, list);
        } finally {
            tx.finish();
        }
    }

    void addToStaticList(StaticQuery staticQuery, Asset asset) {
        u.p("adding asset: " + asset.getName() + " to query: " + staticQuery.getName());
        Transaction tx = graphDb.beginTx();
        try { 
            Node list = staticQuery.getNode();
            Node assetNode = graphDb.getNodeById(asset.id);
            assetNode.createRelationshipTo(list,RelTypes.OWNED);
            tx.success();
        } finally {
            tx.finish();
        }
    }
    
    void removeFromStaticList(StaticQuery staticQuery, Asset asset) {
        u.p("removing asset: " + asset.getName() + " from query " + staticQuery.getName());
        Transaction tx = graphDb.beginTx();
        try { 
            Node list = staticQuery.getNode();
            Node assetNode = graphDb.getNodeById(asset.id);
            for(Relationship rel : assetNode.getRelationships(RelTypes.OWNED,Direction.BOTH)) {
                if(rel.getEndNode().equals(list)) {
                    rel.delete();
                }
            }
            tx.success();
        } finally {
            tx.finish();
        }
    }

    public void delete(StaticQuery sq) {
        Transaction tx = graphDb.beginTx();
        try {
            for(Relationship rel : sq.getNode().getRelationships()) {
                rel.delete();
            }
            sq.getNode().delete();
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

    public void copyAndAddPattern(InputStream stream, String name) throws IOException {
        File file = new File(patternDir,"pattern-"+Math.random()+".png");
        copyStreamToFile(stream,file);
        Transaction tx = graphDb.beginTx();
        try {
            u.p("adding pattern from file: " + file.getAbsolutePath());
            Node asset = graphDb.createNode();
            asset.setProperty(KIND, PATTERN);
            asset.setProperty(NAME, name);
            asset.setProperty(FILEPATH,file.getAbsolutePath());
            kindsIndex.add(asset, KIND, PATTERN);
            kindsIndex.add(asset, NAME, name.toLowerCase());
            tx.success();
        } finally {
            tx.finish();
        }
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
            tx.success();
        } finally {
            tx.finish();
        }
        return toAssetList(ret);
    }
    public List<StaticQuery> getStaticLists() {
        Transaction tx = graphDb.beginTx();
        IndexHits<Node> ret;
        try {
            ret = listsIndex.query(KIND, STATIC_LIST);
            tx.success();
        } finally {
            tx.finish();
        }
        return toStaticListList(ret);
    }


    private List<Asset> getAllByKind(String kind) {
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

    public List<Asset> getAllFonts() {
        return getAllByKind(FONT);
    }

    public List<Asset> getAllPatterns() {
        return getAllByKind(PATTERN);
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

    /*
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
    */
    

    private void deleteAll() {
        u.p("NUKING ENTIRE DB");
        final String KIND = "kind";
        Transaction tx = graphDb.beginTx();
        try {
            for(Node n : kindsIndex.query(KIND,"*")) {
                u.p("nuking asset");
                kindsIndex.remove(n);
                for(Relationship r : n.getRelationships()) {
                    r.delete();
                }
                n.delete();
            }
            u.p("there are " + kindsIndex.get(KIND, FONT).size() + " fonts now");
            u.p("there are " + kindsIndex.get(KIND,PATTERN).size() + " patterns now");

            for(Node n : listsIndex.query(KIND,"*")) {
                u.p("nuking static list");
                listsIndex.remove(n);
                for(Relationship r : n.getRelationships()) {
                    r.delete();
                }
                n.delete();
            }
            tx.success();
        } finally {
            tx.finish();
        }
    }

    public void removeFromLibrary(Asset asset) {
        u.p("deleting asset: " + asset.getName() + " id = " + asset.id);
        Transaction tx = graphDb.beginTx();
        try {
                Node node = graphDb.getNodeById(asset.id);
            for(Relationship r : node.getRelationships()) {
                r.delete();
            }
            node.delete();
            tx.success();
        } finally {
            tx.finish();
        }
    }


    public List<Asset> searchByAnyText(String query) {
        u.p("searching for " + query);
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
