package org.joshy.sketch;

import java.io.File;
import org.joshy.gfx.util.OSUtil;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 5/3/12
 * Time: 12:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class Settings {
    public static final File homedir = new File(OSUtil.getBaseStorageDir(System.getProperty("org.joshy.sketch.settings.basedirname", "Leonardo")));
    public static final File RECENT_FILES = new File(homedir,"recentfiles.xml");
    public static final File SETTINGS_FILE = new File(homedir,"settings.txt");
    public static final File SCRIPTS_DIR = new File(homedir,"scripts");
    public static final String TRACKING_PERMISSIONS = "org.joshy.gfx.sketch.tracking.allow";
    public static final String DEBUG_MENU = "org.joshy.gfx.sketch.debug.menuEnabled";
    public static final String DEFAULT_LOCALE = "org.joshy.gfx.sketch.defaultLocale";
    public static String UPDATE_URL = "";
    public static String DOWNLOAD_URL = "";
    public static String AMINO_BINARY_URL = null;
    public static final String DEFAULT_FONT_NAME = "OpenSans";
}
