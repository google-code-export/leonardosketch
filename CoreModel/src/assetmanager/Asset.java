/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assetmanager;

import org.joshy.gfx.util.u;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 *
 * @author josh
 */
public class Asset {
    protected String filepath;
    protected AssetDB db;
    protected Node node;

    Asset(AssetDB assetDB, Node node, String filepath) {
        this.node = node;
        this.filepath = filepath;
        this.db = assetDB;
    }

    protected Asset(AssetDB db, Node node) {
        this.node = node;
        this.db = db;
    }

    public String getName() {
        return (String) this.node.getProperty(AssetDB.NAME);
    }

    public String getKind() {
        return (String) this.node.getProperty(AssetDB.KIND);
    }

    public File getFile() {
        u.p("getting file for path: " + filepath);
        return new File(filepath);
    }

    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(new File(filepath));
    }

    public void setName(String name) {
        Transaction tx = db.beginTx();
        try {
            node.setProperty(AssetDB.NAME,name);
            tx.success();
        } finally {
            tx.finish();
        }
    }

    public Node getNode() {
        return node;
    }
}
