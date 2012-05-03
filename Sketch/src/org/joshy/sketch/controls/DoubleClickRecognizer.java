package org.joshy.sketch.controls;

import java.util.Date;
import org.joshy.gfx.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 5/3/12
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class DoubleClickRecognizer {
    private long lastClickTime;

    public void apply(MouseEvent event) {
        if(event.getType() == MouseEvent.MousePressed) {
            lastClickTime = new Date().getTime();
        }
    }

    public boolean isDoubleClick() {
        if(new Date().getTime() - lastClickTime < 250) {
            return true;
        }
        return false;
    }
}
