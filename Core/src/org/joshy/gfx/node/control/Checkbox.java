package org.joshy.gfx.node.control;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 20, 2010
 * Time: 11:04:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class Checkbox extends Button {

    public Checkbox() {
        selectable = true;
    }

    public Checkbox(String s) {
        this();
        setText(s);
    }
}
