package org.joshy.sketch;

import com.boxysystems.jgoogleanalytics.FocusPoint;
import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;
import com.boxysystems.jgoogleanalytics.LoggingAdapter;
import com.joshondesign.xml.Doc;
import com.joshondesign.xml.Elem;
import com.joshondesign.xml.XMLParser;
import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.*;
import org.joshy.gfx.sidehatch.TranslationEditor;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.OSUtil;
import org.joshy.gfx.util.image.MasterImageCache;
import org.joshy.gfx.util.localization.Localization;
import org.joshy.gfx.util.u;
import org.joshy.gfx.util.xml.XMLRequest;
import org.joshy.sketch.actions.*;
import org.joshy.sketch.actions.flickr.FlickrUploadAction;
import org.joshy.sketch.actions.io.*;
import org.joshy.sketch.actions.pages.PageListPanel;
import org.joshy.sketch.actions.swatches.ColorSwatchManager;
import org.joshy.sketch.actions.swatches.PatternManager;
import org.joshy.sketch.actions.symbols.SymbolManager;
import org.joshy.sketch.canvas.DocumentCanvas;
import org.joshy.sketch.controls.*;
import org.joshy.sketch.model.CanvasDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.modes.DocModeHelper;
import org.joshy.sketch.modes.pixel.PixelModeHelper;
import org.joshy.sketch.modes.pixel.TiledPixelModeHelper;
import org.joshy.sketch.modes.preso.PresoModeHelper;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.modes.vector.VectorModeHelper;
import org.joshy.sketch.property.PropertyManager;
import org.joshy.sketch.script.ScriptTools;
import org.joshy.sketch.util.UpdateChecker;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.joshy.gfx.util.localization.Localization.getString;

public class Main implements Runnable {
    private static final File homedir = new File(OSUtil.getBaseStorageDir("Leonardo"));
    public static final File RECENT_FILES = new File(homedir,"recentfiles.xml");
    public static final File SETTINGS_FILE = new File(homedir,"settings.txt");
    public static final File SCRIPTS_DIR = new File(homedir,"scripts");

    public SymbolManager symbolManager = new SymbolManager(new File(homedir,"symbols"));
    public ColorSwatchManager colorManager = new ColorSwatchManager(new File(homedir,"swatches.xml"));
    public PatternManager patternManager = new PatternManager(new File(new File(homedir,"patterns"),"patterns.xml"));
    public PropertyManager propMan;
    private QuitAction quitAction = new QuitAction(this);
    private SAction aboutAction;
    private SAction prefsAction;
    public List<File> recentFiles;
    public static Properties settings;
    
    private List<DocModeHelper> modeHelpers = new ArrayList<DocModeHelper>();
    public DocModeHelper defaultModeHelper;
    private List<DocContext> contexts = new ArrayList<DocContext>();
    public static FocusPoint mainApp;
    public static JGoogleAnalyticsTracker tracker;
    public static boolean trackingEnabled = false;
    private Callback<ActionEvent> makeAWishAction;
    public static int CURRENT_BUILD_NUMBER = 2;
    public static Properties releaseProperties;
    public static final String TRACKING_PERMISSIONS = "org.joshy.gfx.sketch.tracking.allow";
    public static final String DEBUG_MENU = "org.joshy.gfx.sketch.debug.menuEnabled";
    public static final String DEFAULT_LOCALE = "org.joshy.gfx.sketch.defaultLocale";
    public static String UPDATE_URL = "";
    public static String DOWNLOAD_URL = "";
    private List<Menu> recentFilesMenus = new ArrayList<Menu>();
    public static MasterImageCache FlickrSearchCache = new MasterImageCache(true,10,"LeonardoFlickrSearchCache");
    private static Font[] fonts;
    private static HashMap<String, Font> fontMap;
    public static String AMINO_BINARY_URL = null;
    public static final String DEFAULT_FONT_NAME = "OpenSans";
    public static List<String> fontList;

    public static void main(String ... args) throws Exception {
        System.setSecurityManager(null);
        String locale = System.getProperty("user.language") + "-" + System.getProperty("user.country");

        setupSettings();

        //Localization.init(Main.class.getResource("translation.xml"),"de-DE");
        if(settings.containsKey(DEFAULT_LOCALE)) {
            String dl = settings.getProperty(DEFAULT_LOCALE);
            if(!dl.equals("DEFAULT")) {
                locale = dl;
            }
        }
        u.p("Using locale = " + locale);
        Localization.init(Main.class.getResource("translation.xml"),locale);

        Core.setUseJOGL(false);
        Core.init();
        Core.getShared().loadCSS(Main.class.getResourceAsStream("style.css"),Main.class.getResource("style.css"));
        Core.getShared().defer(new Main());
    }

    private static void setupTracking() {
        if(settings.containsKey(TRACKING_PERMISSIONS)) {
            trackingEnabled = "true".equals(settings.getProperty(TRACKING_PERMISSIONS));
        } else {
            final Stage stage = Stage.createStage();
            stage.setTitle(getString("trackingDialog.title").toString());
            Callback<ActionEvent> noResponse = new Callback<ActionEvent>() {
                public void call(ActionEvent actionEvent) throws Exception {
                    stage.hide();
                    settings.setProperty(TRACKING_PERMISSIONS,"false");
                }
            };
            Callback<ActionEvent> yesResponse = new Callback<ActionEvent>() {
                public void call(ActionEvent actionEvent) throws Exception {
                    stage.hide();
                    settings.setProperty(TRACKING_PERMISSIONS,"true");
                }
            };
            Callback<ActionEvent> whatResponse = new Callback<ActionEvent>() {
                public void call(ActionEvent actionEvent) throws Exception {
                    OSUtil.openBrowser("http://code.google.com/p/leonardosketch/wiki/Tracking");
                }
            };
            stage.setContent(new VFlexBox()
                    .add(new Label(getString("trackingDialog.question")))//"Can Leonardo track how often you run it?"))
                    .add(new HFlexBox()
                        .add(new Button(getString("misc.yes")).onClicked(yesResponse))
                        .add(new Button(getString("misc.no")).onClicked(noResponse))
                        .add(new Button(getString("misc.whatsthis")).onClicked(whatResponse))
                    )
                    );
            stage.setWidth(400);
            stage.setHeight(200);
            //stage.centerOnScreen();
            Core.getShared().defer(new Runnable(){
                public void run() {
                    stage.raiseToTop();
                }
            });

        }
        if(trackingEnabled) {
            tracker = new JGoogleAnalyticsTracker("Leonardo","UA-17798312-2");
            mainApp = new FocusPoint("MainApp");
            tracker.setLoggingAdapter(new LoggingAdapter(){
                public void logError(String s) {
                    u.p("logging error: " + s);
                }

                public void logMessage(String s) {
                    u.p("logging message: " + s);
                }
            });
        }

        trackEvent("launch");
    }

    private static void setupSettings() throws IOException {
        releaseProperties = new Properties();
        URL releaseURL = Main.class.getResource("release.properties");
        releaseProperties.load(releaseURL.openStream());
        CURRENT_BUILD_NUMBER = Integer.parseInt(releaseProperties.getProperty("org.joshy.sketch.build.number"));
        UPDATE_URL = releaseProperties.getProperty("org.joshy.sketch.updateurl");
        DOWNLOAD_URL = releaseProperties.getProperty("org.joshy.sketch.downloadurl");
        AMINO_BINARY_URL = System.getProperty("com.joshondesign.amino.binaryurl","http://goamino.org/download/daily/amino-1.0b2.js");

        settings = new Properties();
        if(SETTINGS_FILE.exists()) {
            settings.load(new FileReader(SETTINGS_FILE));
        }
        if(System.getProperties().containsKey("org.joshy.gfx.sketch.tracking.allow")) {
            settings.setProperty("org.joshy.gfx.sketch.tracking.allow",System.getProperty("org.joshy.gfx.sketch.tracking.allow"));
        }

    }

    public static void trackEvent(String event) {
        if(trackingEnabled) {
            tracker.trackAsynchronously(new FocusPoint(event,mainApp));
        }
    }

    public static Map<String,Font> getFontMap() {
        return fontMap;
    }

    public void run() {
        try {
            setupTracking();
            UpdateChecker.setup(this);
            setupGlobals();
            //setupNewDoc(defaultModeHelper,null);
            showDocChooser();
            setupMac();
            //Core.setDebugCSS(new File("test.css"));
        } catch (Exception ex) {
            u.p(ex);
        }
    }

    public DocContext setupNewDoc(DocModeHelper modeHelper, CanvasDocument doc) throws Exception {
        final DocContext context = modeHelper.createDocContext(this);
        contexts.add(context);

        context.setupActions();
        context.setupPalettes();
        context.stackPanel = new StackPanel();


        ScrollPane scrollPane = new ScrollPane(
            context.getCanvas()
                .setWidth(300)
                .setHeight(300)
        );
        
        final Ruler hruler = new Ruler(false,scrollPane,context);
        final Ruler vruler = new Ruler(true,scrollPane,context);

        context.stackPanel.add(
                new Panel() {
                    @Override
                    public void doLayout() {
                        super.doLayout();
                        for(Control c : controlChildren()) {
                            if(c == hruler) {
                                c.setWidth(getWidth()-30);
                                c.setHeight(30);
                                c.setTranslateX(30);
                                c.setTranslateY(0);
                            }
                            if(c == vruler) {
                                c.setWidth(30);
                                c.setHeight(getHeight()-30);
                                c.setTranslateX(0);
                                c.setTranslateY(30);
                            }
                            if(c instanceof ScrollPane) {
                                c.setWidth(getWidth()-30);
                                c.setHeight(getHeight()-30);
                                c.setTranslateX(30);
                                c.setTranslateY(30);
                            }
                            if(c == context.getNotificationIndicator()) {
                                c.setTranslateX(40);
                                c.setTranslateY(getHeight()-c.getHeight()-25);
                            }
                            c.doLayout();
                        }

                    }
                }
                .add(hruler,vruler)
                .add(scrollPane)
                .add(context.getNotificationIndicator())
                );

        if(context instanceof VectorDocContext) {
            context.pageList = new PageListPanel((VectorDocContext)context);
        }
        context.setupTools();
        context.setupSidebar();
        

        setupStage(context, modeHelper);
        //create a new doc if one wasn't passed in
        if(doc == null) {
            doc = modeHelper.createNewDoc();
        }
        context.setDocument(doc);
        hruler.setDocument(doc);
        vruler.setDocument(doc);
        context.getStage().setTitle(context.getDocument().getTitle());

        if(modeHelper.isPageListVisible()) {
            context.mainPanel.add(context.pageList);
        }
        if(context.pageMenu != null) {
            context.menubar.remove(context.pageMenu);
        }
        context.pageMenu = modeHelper.buildPageMenu(context);
        if(context.pageMenu != null) {
            context.menubar.add(context.pageMenu);
        }

        rebuildWindowMenu();
        //focus on the canvas
        Core.getShared().getFocusManager().setFocusedNode(context.getCanvas());

        return context;
    }

    public void rebuildWindowMenu() {
        List<ShowWindow> windowOpeners = new ArrayList<ShowWindow>();
        for(DocContext c : contexts) {
            windowOpeners.add(new ShowWindow(c));
        }
        for(DocContext context : contexts) {
            if(context.windowJMenu != null) {
                context.menubar.remove(context.windowJMenu);
            }
            Menu windowMenu = new Menu().setTitle(getString("menus.window"));
            for(ShowWindow a : windowOpeners) {
                windowMenu.addItem(getString("menus.window")+": " + a.context.getDocument().getTitle(), a);
            }
            context.windowJMenu = windowMenu;//windowMenu.createJMenu();
            context.menubar.add(context.windowJMenu);
        }                                               
    }

    private void setupStage(final DocContext context, DocModeHelper modeHelper) {
        final Textbox wishBox = new Textbox().setHintText(getString("misc.wish.box"));
        final Label wishStatus = new Label("");
        wishStatus.setPrefWidth(130);
        makeAWishAction = new Callback<ActionEvent>(){
            public void call(ActionEvent actionEvent) {
                if(wishBox.getText().trim().length() < 2) {
                    StandardDialog.showError(getString("misc.wish.dialog").toString());
                } else {
                    try {
                        new XMLRequest()
                                .setMethod(XMLRequest.METHOD.POST)
                                .setURL("http://joshy.org:8081/AminoWebServices/MakeAWish")
                                .setParameter("message",wishBox.getText())
                                .onComplete(new Callback<Doc>(){
                                    public void call(Doc doc) {
                                        wishStatus.setText("Wish received!");
                                    }
                                }).start();
                        wishBox.setText("");
                        wishStatus.setText("Making wish...");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        wishBox.onAction(makeAWishAction);
        final HFlexBox statusBar = new HFlexBox();
        statusBar.setBoxAlign(HFlexBox.Align.Baseline)
                .add(wishBox,1)
                .add(new Button(getString("misc.wish.button")).onClicked(makeAWishAction))
                .add(wishStatus)
                ;
        statusBar.setPrefWidth(450);


        context.mainPanel = new Panel() {
            @Override
            public void doLayout() {
                for(Control c : controlChildren()) {
                    double sidebarWidth= 75*3 + 20 + 10 + 30;
                    if(!context.sidebarContainer.isOpen()) {
                        sidebarWidth = 30;
                    }
                    if(c == context.stackPanel) {
                        c.setTranslateY(0);
                        c.setHeight(getHeight()-40);
                        c.setTranslateX(30);
                        c.setWidth(getWidth()-sidebarWidth-30);
                    }
                    if(c == context.getToolbar()) {
                        c.setTranslateX(0);
                        c.setTranslateY(0);
                        c.setHeight(getHeight());
                    }
                    if(c == context.sidebarContainer) {
                        c.setWidth(sidebarWidth);
                        c.setTranslateX(getWidth()-sidebarWidth);
                        c.setHeight(getHeight());
                        c.setTranslateY(0);
                    }
                    if(c == context.pageList) {
                        c.setTranslateX(20);
                        c.setTranslateY(getHeight()-100);
                        c.setHeight(100);
                        c.setWidth(getWidth()-20-sidebarWidth);
                    }
                    if(c == statusBar) {
                        c.setTranslateX(20);
                        c.setTranslateY(getHeight()-40);
                    }
                    c.doLayout();
                }
                setDrawingDirty();
            }
            @Override
            protected void drawSelf(GFX g) {
                //no op
            }
        };
        context.mainPanel.add(statusBar);

        if(modeHelper.isPageListVisible()) {
            context.mainPanel.add(context.pageList);
        }
        context.mainPanel.add(context.stackPanel);
        context.mainPanel.add(context.getToolbar());
        context.sidebarContainer = new DisclosurePanel()
                .setTitle(new Label(getString("menus.viewSidebar"))
                        .setFont(Font.name(DEFAULT_FONT_NAME).size(18).resolve())
                        .setColor(new FlatColor(0x404040))
                )
                .setPosition(DisclosurePanel.Position.Right)
                .setOpen(false);
        context.mainPanel.add(context.sidebarContainer);
        if(context.getSidebar() != null) {
            TabPanel sb = context.getSidebar();
            sb.setPrefHeight(200);
            //sb.setPrefWidth(200);
            context.sidebarContainer.setContent(sb);
        } else {
            context.sidebarContainer.setContent(new Label("nothing"));
            context.sidebarContainer.setOpen(false);
        }
        context.mainPanel.setFill(FlatColor.WHITE);

        context.getStage().setContent(context.mainPanel);
        context.setupPopupLayer();
        context.getStage().setWidth(950);
        context.getStage().setHeight(700);

        final JFrame frame = (JFrame) context.getStage().getNativeWindow();
        context.menubar = new Menubar(frame);
        buildCommonMenubar(context,modeHelper);
        EventBus.getSystem().addListener(CanvasDocument.DocumentEvent.Closing,new Callback<CanvasDocument.DocumentEvent>() {
            public void call(CanvasDocument.DocumentEvent documentEvent) throws Exception {
                if(documentEvent.getDocument() == context.getDocument()) {
                    if(context.getDocument().isDirty()) {
                        u.p("doc is still dirty!!!");
                        StandardDialog.Result result = StandardDialog.showYesNoCancel(
                                getString("dialog.docNotSaved").toString(),
                                getString("dialog.save").toString(),
                                getString("dialog.dontsave").toString(),
                                getString("dialog.cancel").toString());
                        if(result== StandardDialog.Result.Yes) {
                            new SaveAction(context,false,true).execute();
                        }
                        if(result==StandardDialog.Result.No) {
                            context.getStage().hide();
                            closeWindow(context);
                        }
                        if(result==StandardDialog.Result.Cancel) {
                            //do nothing
                        }
                    } else {
                        closeWindow(context);
                    }
                }

            }
        });
        EventBus.getSystem().addListener(context.getStage(), WindowEvent.Closing,new Callback<WindowEvent>() {
            public void call(WindowEvent event) {
                if(context.getDocument().isDirty()) {
                    event.veto();
                    u.p("doc is still dirty!!!");
                    StandardDialog.Result result = StandardDialog.showYesNoCancel(
                            getString("dialog.docNotSaved").toString(),
                            getString("dialog.save").toString(),
                            getString("dialog.dontsave").toString(),
                            getString("dialog.cancel").toString());
                    if(result== StandardDialog.Result.Yes) {
                        new SaveAction(context,false,true).execute();
                    }
                    if(result==StandardDialog.Result.No) {
                        context.getStage().hide();
                        closeWindow(context);
                    }
                    if(result==StandardDialog.Result.Cancel) {
                        //do nothing
                    }

                } else {
                    closeWindow(context);
                }
            }
        });
    }

    private void closeWindow(DocContext context) {
        context.getStage().hide();
        contexts.remove(context);
        rebuildWindowMenu();
        if(contexts.isEmpty()) {
            showDocChooser();
        }
    }

    private void showDocChooser() {
        final Stage stage = Stage.createStage();
        stage.setTitle("New Leo Document");
        stage.setContent(new NewDocumentChooser(this, stage));
        stage.setWidth(550);
        stage.setHeight(400);
        stage.centerOnScreen();
    }

    private void setupGlobals() throws IOException {
        fonts = new Font[]{
                Font.fromURL(getFont("belligerent.ttf")).size(30).resolve(),
                Font.fromURL(getFont("Chunk.ttf")).resolve(),
                Font.fromURL(getFont("OFLGoudyStMTT.ttf")).size(30).resolve(),
                Font.fromURL(getFont("orbitron-medium.ttf")).resolve(),
                Font.fromURL(getFont("raleway_thin.ttf")).resolve(),
                Font.fromURL(getFont("Sniglet_Regular.ttf")).resolve(),
                Font.fromURL(getFont("league_gothic.ttf")).resolve(),
        };
        fontMap = new HashMap<String,Font>();
        for(Font font : fonts) {
            fontMap.put(font.getName(),font);
        }
        fontMap.put(Font.DEFAULT.getName(),Font.DEFAULT);

        fontList = new ArrayList<String>();
        fontList.addAll(fontMap.keySet());

        modeHelpers.add(new VectorModeHelper(this));
        modeHelpers.add(new PresoModeHelper(this));
        modeHelpers.add(new PixelModeHelper(this));
        modeHelpers.add(new TiledPixelModeHelper(this));
        defaultModeHelper = modeHelpers.get(0);

        propMan = new PropertyManager();
        if(!SCRIPTS_DIR.exists()) {
            SCRIPTS_DIR.mkdirs();
            try {
                u.streamToFile(ScriptTools.class.getResourceAsStream("demo_script.js"),new File(SCRIPTS_DIR,"demo_script.js"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        recentFiles = loadRecentDocs(RECENT_FILES);
        
        EventBus.getSystem().addListener(FileOpenEvent.FileOpen, new Callback<FileOpenEvent>() {
            public void call(FileOpenEvent fileOpenEvent) throws Exception {
                OpenAction act = new OpenAction(Main.this);
                act.execute(fileOpenEvent.getFiles());
            }
        });


    }

    private URL getFont(String s) {
        return Main.class.getResource("resources/fonts/"+s);
    }

    private void buildCommonMenubar(DocContext context, DocModeHelper modeHelper) {
        DocumentCanvas canvas = context.getCanvas();
        //recent files menu
        Menu recentFilesMenu = new Menu().setTitle(getString("menus.recentfiles"));
        recentFilesMenus.add(recentFilesMenu);
        regenRecentFilesMenus();

        //file menu
        Menu fileMenu = new Menu().setTitle(getString("menus.file"));
        Menu newMenu = new Menu().setTitle(getString("menus.new"));
        for(DocModeHelper helper : modeHelpers) {
            newMenu.addItem(helper.getModeName() + " document", helper.getNewDocAction(this));
        }
        fileMenu.addMenu(newMenu);
        fileMenu
                .addItem(getString("menus.open"), "O", new OpenAction(this))
                .addMenu(recentFilesMenu)
                .addItem(getString("menus.save"), "S",    new SaveAction(context, false,true))
                .addItem(getString("menus.saveas"), "shift S", new SaveAction(context, true,true))
                .addItem(getString("menus.close"), "W",   new CloseAction(context))
                ;

        if("true".equals(System.getProperty("org.joshy.sketch.actions.enableImport"))) {
            fileMenu.addItem(getString("menus.import"), new ImportAction(context));
        }

        Menu exportMenu = new Menu().setTitle(getString("menus.export"))
                .addItem(getString("menus.topng"), new SavePNGAction(context))
                .addItem(getString("menus.tosvg"), new SaveSVGAction(context))
                .addItem(getString("menus.tohtml"), new SaveHTMLAction(context))
                .addItem(getString("menus.tocanvas"), new SaveAminoCanvasAction(context))
                .addItem(getString("menus.topdf"), new SavePDFAction(context))
                .addItem(getString("menus.tojava2d"), new SaveJava2DAction(context))
                .addItem("Re-export", "shift E", new ReExportAction(context));
        modeHelper.addCustomExportMenus(exportMenu,context);
        fileMenu.addMenu(exportMenu);

        quitAction = new QuitAction(this);
        aboutAction = new AboutAction(this);
        prefsAction = new PreferencesAction(this);

        if(!OSUtil.isMac()) {
            fileMenu.addItem(getString("menus.settings"),  null,       prefsAction);
            fileMenu.addItem(getString("menus.about"),     null,       aboutAction);
            fileMenu.addItem(getString("menus.exit"),      "Q",        quitAction);
        }
        Menubar menubar = context.menubar;
        menubar.add(fileMenu);
        Menu editMenu = new Menu().setTitle(getString("menus.edit"))
                .addItem(getString("menus.cut"), "X", new Clipboard.CutAction(context))
                .addItem(getString("menus.copy"), "C", new Clipboard.CopyAction(context))
                .addItem(getString("menus.paste"), "V", new Clipboard.PasteAction(context));
        if(context instanceof VectorDocContext) {
            editMenu.addItem(getString("menus.delete"), ((VectorDocContext)context).deleteSelectedNodeAction);
        }
        editMenu.addItem(getString("menus.undo"), "Z", new UndoManager.UndoAction(context))
                .addItem(getString("menus.redo"), "shift Z", new UndoManager.RedoAction(context));
        if(context instanceof VectorDocContext) {
                editMenu.addItem(getString("menus.clearSelection"), "D", new NodeActions.ClearSelection((VectorDocContext) context));
        }
        /*editMenu.addItem("Enable Analytics Tracking", new ToggleAction(){
            @Override
            public boolean getToggleState() {
                return trackingEnabled;
            }

            @Override
            public void setToggleState(boolean toggleState) {
                trackingEnabled = toggleState;
            }
        });
        */
        menubar.add(editMenu);
        context.createAfterEditMenu(menubar);

        Menu viewMenu = new Menu().setTitle(getString("menus.view"))
                .addItem(getString("menus.zoomIn"),     "EQUALS", new ViewActions.ZoomInAction(context))
                .addItem(getString("menus.zoomOut"),    "MINUS",  new ViewActions.ZoomOutAction(context))
                .addItem(getString("menus.zoomActual"), "0",      new ViewActions.ZoomResetAction(context))
                .separator()
                .addItem(getString("menus.fullScreen"), "F",      new ViewActions.ToggleFullScreen(context))
                .addItem(getString("menus.fullScreenWithMenubar"), new ViewActions.ToggleFullScreenMenubar(context))
                .separator()
                .addItem(getString("menus.newView"), new ViewActions.NewView(context));
        
        if(context instanceof VectorDocContext) {
            VectorDocContext vdc = (VectorDocContext) context;
            viewMenu
                    .addItem(getString("menus.showDocumentBounds"),  new ViewActions.ShowDocumentBounds(vdc))
                    .addItem(getString("menus.showGrid"),             new ViewActions.ShowGridAction(vdc))
                    .addItem(getString("menus.snapGrid"),          new ViewActions.SnapGridAction(vdc))
                    .addItem(getString("menus.snapDocEdges"),          new ViewActions.SnapDocBoundsAction(vdc))
                    .addItem(getString("menus.snapNodeEdges"),          new ViewActions.SnapNodeBoundsAction(vdc))
                    .separator();
            viewMenu.addItem(getString("menus.viewSidebar"), new ViewActions.ViewSidebarAction(vdc));
            for(SAction a : vdc.getSidebarPanelViewActions()) {
                viewMenu.addItem(a.getDisplayName(), a);
            }
        }

        //view menu
        menubar.add(viewMenu);

        Menu shareMenu = new Menu().setTitle(getString("menus.share"))
                .addItem(getString("menus.sendTwitter"), new TwitPicAction(context))
                .addItem(getString("menus.configTwitter"), new TwitPicAction.ChangeSettingsAction(context, true))
                .addItem(getString("menus.sendFlickr"), new FlickrUploadAction(context))
                .addItem(getString("menus.changeFlickrSettings"), new FlickrUploadAction.ChangeFlickrSettingsAction(context,true))
        ;

        if(OSUtil.isMac()) {
            shareMenu.addItem(getString("menus.sendEmailPNG"), new SendMacMail(context));
        }
        menubar.add(shareMenu);

        Menu scriptMenu = new Menu().setTitle(getString("menus.scripts"));
        if(SCRIPTS_DIR.exists()) {
            for(File file : SCRIPTS_DIR.listFiles()) {
                if(file.exists() && file.getName().toLowerCase().endsWith(".js")) {
                    scriptMenu.addItem(file.getName(),new ScriptTools.RunScriptAction(file,context));
                }
            }
        }
        menubar.add(scriptMenu);

        if(settings.containsKey(DEBUG_MENU)) {
            if("true".equals(settings.getProperty(DEBUG_MENU))) {
                Menu debugMenu = new Menu().setTitle(getString("menus.debug"));
                /*debugMenu.addItem("Show Console", new SAction(){
                    @Override
                    public void execute() throws Exception {

                    }
                })*/
                debugMenu.addItem(getString("menus.editTranslations"), new SAction() {
                    @Override
                    public void execute() throws Exception {
                        Stage s = Stage.createStage();
                        s.setContent(new TranslationEditor());
                        s.setWidth(800);
                        s.setHeight(400);
                        s.centerOnScreen();
                    }
                });
                menubar.add(debugMenu);
            }
        }

    }

    private List<File> loadRecentDocs(File file) {
        List<File> files = new ArrayList<File>();
        if(file.exists()) {
            try {
                Doc doc = XMLParser.parse(file);
                for(Elem e : doc.xpath("//file")) {
                    File f = new File(e.attr("filepath"));
                    //files.add(new File(e.attr("filepath")));
                    if(f.exists()) {
                        files.add(f);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return files;
    }


    private void saveRecentFiles(List<File> recentFiles, File file) {
        try {
            XMLWriter out = new XMLWriter(file);
            out.start("files");
            for(File f : recentFiles) {
                out.start("file","filepath",f.getAbsolutePath());
                out.end();
            }
            out.end();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void addRecentFile(File file) {
        recentFiles.add(file);
        recalcAndSaveRecentFiles();
    }

    private void recalcAndSaveRecentFiles() {

        Map<String,File> map = new HashMap<String,File>();
        List<String> unique = new ArrayList<String>();
        for(File f : recentFiles) {
            if(!unique.contains(f.getAbsolutePath())) {
                unique.add(f.getAbsolutePath());
            }
            map.put(f.getAbsolutePath(), f);
        }
        recentFiles.clear();
        for(String name : unique) {
            recentFiles.add(map.get(name));
        }
        saveRecentFiles(recentFiles, Main.RECENT_FILES);
        regenRecentFilesMenus();
    }

    private void regenRecentFilesMenus() {
        for(Menu menu : recentFilesMenus) {
            menu.removeAll();
            List<File> f2 = new ArrayList<File>(recentFiles);
            Collections.reverse(f2);
            for(File f : f2) {
                menu.addItem(f.getName(), new OpenAction(this,f));
            }
        }
    }

    private void setupMac() {
        if(!OSUtil.isMac()) return;
        EventBus.getSystem().addListener(SystemMenuEvent.All, new Callback<SystemMenuEvent>(){
            public void call(SystemMenuEvent systemMenuEvent) throws Exception {
                if(systemMenuEvent.getType() == SystemMenuEvent.About) {
                    aboutAction.execute();
                }
                if(systemMenuEvent.getType() == SystemMenuEvent.Preferences) {
                    prefsAction.execute();
                }
                if(systemMenuEvent.getType() == SystemMenuEvent.Quit) {
                    quitAction.execute();
                }
            }
        });
    }

    public static URL getIcon(String s) {
        return Main.class.getResource("resources/"+s);
    }

    public static void saveSettings() throws IOException {
        u.p("saving settings to : " + SETTINGS_FILE.getAbsolutePath());
        settings.store(new FileWriter(SETTINGS_FILE),"Leonardo settings");
    }

    public List<DocModeHelper> getModeHelpers() {
        return modeHelpers;
    }

    public List<DocContext> getContexts() {
        return contexts;
    }

    private class ShowWindow extends SAction {
        private DocContext context;

        public ShowWindow(DocContext context) {
            super();
            this.context = context;
        }

        @Override
        public void execute() {
            context.getStage().raiseToTop();
        }
    }

}

