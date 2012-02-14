package org.joshy.sketch.model;

import org.joshy.sketch.util.RandomString;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 5, 2010
 * Time: 10:53:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class Page {
    private String id;
    private String name;
    
    private static int counter = 0;

    public Page() {
        this.id = new RandomString(32).nextString();
        this.name = "page " + counter;
        counter++;
    }

    public String getId() {
        return id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
