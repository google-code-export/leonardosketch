package assetmanager;

import org.neo4j.graphdb.Node;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: josh
 * Date: 4/12/12
 * Time: 4:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class SymbolSetAsset extends Asset{
    protected SymbolSetAsset(AssetDB db, Node node) {
        super(db, node);
    }

    public File getFile() {
        filepath = (String) this.node.getProperty(AssetDB.FILEPATH);
        return new File(filepath);
    }
}
