/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assetmanager;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.List;

/**
 *
 * @author josh
 */
class StaticQuery extends Query{
    private AssetDB db;
    private Node node;

    public StaticQuery(AssetDB db, String name, Node node) {
        super(name,"nothing",0,0);
        this.db = db;
        this.node = node;
    }
    public List<Asset> execute(AssetDB db) {
        return db.getStaticList(node.getId());
    }

    public String getName() {
        return (String) this.node.getProperty(AssetDB.NAME);
    }

    @Override
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
