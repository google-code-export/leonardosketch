/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assetmanager;

import java.util.List;

/**
 *
 * @author josh
 */
class Query {
    private final String name;
    public  final int x;
    public  final int y;
    private final String kind;
    
    Query(String name, String kind, int x, int y) {
        this.name = name;
        this.kind = kind;
        this.x = x;
        this.y = y;
    }

    String getName() {
        return this.name;
    }
    
    public List<Asset> execute(AssetDB db) {
        return db.getByKind(kind);
    }
    
}
