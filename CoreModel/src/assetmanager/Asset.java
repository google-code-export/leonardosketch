/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assetmanager;

import java.io.File;

/**
 *
 * @author josh
 */
public class Asset {
    final String name;
    final String kind;
    final String filepath;
    final long id;

    Asset(String name, String kind, String filepath, long id) {
        this.name = name;
        this.kind = kind;
        this.filepath = filepath;
        this.id = id;
    }

    File getFile() {
        return new File(filepath);
    }
    
}
