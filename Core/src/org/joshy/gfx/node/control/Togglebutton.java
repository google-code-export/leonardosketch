package org.joshy.gfx.node.control;

import org.joshy.gfx.node.Node;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Feb 4, 2010
 * Time: 1:37:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class Togglebutton extends Button {
    public Togglebutton(String text) throws IOException {
        super(text);
        selectable = true;
    }

}
