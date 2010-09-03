package org.joshy.gfx.css;

import org.joshy.gfx.node.control.Control;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 31, 2010
 * Time: 1:05:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class CSSMatcher {
    public String element;
    public String pseudo;
    public String id;
    public String cssClass;

    public CSSMatcher() {
    }

    public CSSMatcher(String element) {
        this.element = element;
    }

    public CSSMatcher(Control c) {
        this.element = c.getClass().getSimpleName();
        this.id = c.getId();
    }

    @Override
    public String toString() {
        return "CSSMatcher{" +
                "element='" + element + '\'' +
                ", pseudo='" + pseudo + '\'' +
                ", id='" + id + '\'' +
                ", cssClass='" + cssClass + '\'' +
                '}';
    }
}
