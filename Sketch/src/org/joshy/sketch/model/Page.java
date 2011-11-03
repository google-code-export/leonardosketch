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

    public Page() {
        this.id = new RandomString(32).nextString();
    }

    public String getId() {
        return id;
    }
}
