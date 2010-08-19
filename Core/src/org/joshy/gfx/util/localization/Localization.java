package org.joshy.gfx.util.localization;

import com.joshondesign.xml.Doc;
import com.joshondesign.xml.Elem;
import com.joshondesign.xml.XMLParser;

import java.net.URL;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Aug 19, 2010
 * Time: 2:15:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class Localization {
    private static String masterLocaleName;
    private static HashMap<String, String> translations;

    public static void init(URL translationStore, String localeName) throws Exception {
        masterLocaleName = localeName;
        translations = new HashMap<String,String>();
        Doc doc = XMLParser.parse(translationStore.openStream());
        for(Elem set : doc.xpath("//set")) {
            String prefix = set.attr("name");
            for(Elem key : set.xpath("key")) {
                String keyName = key.attr("name");
                for(Elem value : key.xpath("value")) {
                    String language = value.attr("language");
                    String translationKey = prefix+"."+ keyName+"."+language;
                    String translationValue = value.text();
                    if(language.length() <= 0) {
                        translationKey = prefix+"."+keyName;
                    }
                    translations.put(translationKey,translationValue);
                }
            }
        }
        
    }

    public static String getString(String key) {
        if(translations.containsKey(key+"."+masterLocaleName)) {
            return translations.get(key+"."+masterLocaleName);
        }
        String s = translations.get(key);
        return s;
    }
}
