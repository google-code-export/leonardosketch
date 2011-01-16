package org.joshy.sketch.util;

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
}
