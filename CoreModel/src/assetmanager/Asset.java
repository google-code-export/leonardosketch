/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assetmanager;

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
    final String filepath;
    final long id;
    private AssetDB db;
    private Node node;

    Asset(AssetDB assetDB, Node node, String filepath, long id) {
        this.node = node;
        this.filepath = filepath;
        this.id = id;
        this.db = assetDB;
    }

    public String getName() {
        return (String) this.node.getProperty(AssetDB.NAME);
    }

    public String getKind() {
        return (String) this.node.getProperty(AssetDB.KIND);
    }

    public File getFile() {
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
}
