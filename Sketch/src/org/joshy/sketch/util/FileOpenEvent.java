package org.joshy.sketch.util;

import org.joshy.gfx.event.Event;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Nov 26, 2010
 * Time: 6:37:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileOpenEvent extends Event {
    private List<File> files;
    public static EventType FileOpen = new EventType("FileOpen");

    public FileOpenEvent(List<File> files) {
        super(FileOpen);
        this.files = files;
    }

    public List<File> getFiles() {
        return files;
    }
}
