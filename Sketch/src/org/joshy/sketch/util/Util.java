package org.joshy.sketch.util;

import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.util.OSUtil;
import org.joshy.sketch.modes.DocContext;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 12/31/10
 * Time: 11:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class Util {
    public static void copyToFile(InputStream in, File out) throws IOException {
        FileOutputStream fout = new FileOutputStream(out);
        byte[] buff = new byte[1024];
        while(true) {
            int n = in.read(buff);
            if(n < 0) break;
            fout.write(buff,0,n);
        }
        fout.close();
        in.close();
    }

    public static double clamp(double min, double value, double max) {
        if(value < min) return min;
        if(value > max) return max;
        return value;
    }

    public static boolean isBetween(double min, double value, double max) {
        if(value < min) return false;
        if(value > max) return false;
        return true;
    }

    public static Point2D interpolatePoint(Point2D start, Point2D end, double position) {
        return new Point2D.Double(
                (end.getX()-start.getX())*position + start.getX(),
                (end.getY()-start.getY())*position + start.getY()
        );
    }

    public static Bounds toBounds(Rectangle2D bds) {
        return new Bounds(
                bds.getX(),
                bds.getY(),
                bds.getWidth(),
                bds.getHeight());
    }


    public static File requestDirectory(String title, DocContext context) {
        FileDialog fd = new FileDialog((Frame)context.getStage().getNativeWindow());
        if(OSUtil.isMac()) {
            //mac hack to do directory chooser
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
        } else {
            fd.setMode(FileDialog.SAVE);
        }
        fd.setTitle(title);
        fd.setVisible(true);
        if(OSUtil.isMac()) {
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
        }
        //cancel
        if(fd.getFile() == null) return null;

        String fileName = fd.getFile();
        /*
        if(!fileName.toLowerCase().endsWith(extension)) {
            fileName = fileName + extension;
        }*/
        File file = new File(fd.getDirectory(),fileName);
        if(file.exists() && !file.isDirectory()) {
            return null;
        }
        if(!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public static Shape toShape(Bounds bounds) {
        return new Rectangle2D.Double(
                bounds.getX(),
                bounds.getY(),
                bounds.getWidth(),
                bounds.getHeight()
        );
    }
}
