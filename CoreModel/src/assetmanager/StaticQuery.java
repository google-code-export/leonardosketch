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
class StaticQuery extends Query{
    final long listid;
    public StaticQuery(String name, long listid) {
        super(name,"nothing",0,0);
        this.listid = listid;
    }
    public List<Asset> execute(AssetDB db) {
        return db.getStaticList(listid);
    }
}
