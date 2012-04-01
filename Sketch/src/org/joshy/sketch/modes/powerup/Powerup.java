package org.joshy.sketch.modes.powerup;

import org.joshy.sketch.modes.DocContext;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 3/31/12
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Powerup {
    public abstract CharSequence getMenuName();

    public abstract void enable(DocContext context);
}
