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
    private String name;
    public  final int x;
    public  final int y;
    private final String kind;
    private boolean selectable;

    Query(String name, String kind) {
        this(name,kind,-1,-1);
    }
    Query(String name, String kind, int x, int y) {
        this.name = name;
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.selectable = true;
    }

    String getName() {
        return this.name;
    }
    
    public List<Asset> execute(AssetDB db) {
        return db.getByKind(kind);
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setName(String name) {
        this.name = name;
    }
}
