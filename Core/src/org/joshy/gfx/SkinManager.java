package org.joshy.gfx;

import org.joshy.gfx.css.SuperSkin;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 18, 2010
 * Time: 1:58:25 PM
 * To change this template use File | Settings | File Templates.
 */

public class SkinManager {
    private static SkinManager _shared;
    private SuperSkin cssSkin;

    private SkinManager() {
    }

    public static SkinManager getShared() {
        if(_shared == null) {
            _shared = new SkinManager();
        }
        return _shared;
    }

    public void setCSSSkin(SuperSkin cssSkin) {
        this.cssSkin = cssSkin;
    }

    public SuperSkin getCSSSkin() {
        return cssSkin;
    }
}
