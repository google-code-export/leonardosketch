/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assetmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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

    public String getName() {
        return name;
    }

    public File getFile() {
        return new File(filepath);
    }

    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(new File(filepath));
    }
}
