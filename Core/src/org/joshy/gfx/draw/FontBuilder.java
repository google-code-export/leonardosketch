package org.joshy.gfx.draw;

import org.joshy.gfx.Core;
import org.joshy.gfx.stage.jogl.JOGLFont;
import org.joshy.gfx.util.u;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
* Created by IntelliJ IDEA.
* User: josh
* Date: Jan 21, 2010
* Time: 7:37:41 PM
* To change this template use File | Settings | File Templates.
*/
public class FontBuilder {
    private String name = "Arial";
    private float size = 10;

    private static Map<String, Font> cache;
    static Map<String, java.awt.Font> customRootFontCache;
    private boolean vector = false;
    private File file = null;
    private Font.Weight weight = Font.Weight.Regular;
    private Font.Style style;
    private URL url;

    public FontBuilder(String name) {
        this.name = name;
    }

    public FontBuilder(File file) {
        this.file = file;
    }

    public FontBuilder(URL url) {
        this.url = url;
    }

    public FontBuilder size(float size) {
        this.size = size;
        return this;
    }

    public Font resolve() {
        if(cache == null) {
            cache = new HashMap<String,Font>();
            customRootFontCache = new HashMap<String,java.awt.Font>();
        }

        //load from file to get the name
        if(file != null) {
            try {
                java.awt.Font fnt = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,file);
                name = fnt.getFontName();
                customRootFontCache.put(name,fnt);
                System.out.println("loaded font from file : " + file.getAbsolutePath() + " name = " + name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(url != null) {
            try {
                java.awt.Font fnt = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,url.openStream());
                name = fnt.getFontName();
                customRootFontCache.put(name,fnt);
                System.out.println("loaded font from url : " + url + " name = " + name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String key = "Font:"+this.name+":"+size+":"+weight+":"+style+":".intern();
        if(cache.containsKey(key)) {
            return cache.get(key);
        }

        Font font = null;
        if(Core.getShared().isUseJOGL()) {
            font = new JOGLFont(name,size,vector,file);
        } else {
            font = new Font(name,size,vector,weight,style,file,url);
        }
        cache.put(key,font);
        return font;
    }

    public FontBuilder setVector(boolean vector) {
        this.vector = vector;
        return this;
    }

    public FontBuilder weight(Font.Weight weight) {
        this.weight = weight;
        return this;
    }

    public FontBuilder style(Font.Style style) {
        this.style = style;
        return this;
    }
}
