package org.joshy.sketch;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;
import com.boxysystems.jgoogleanalytics.FocusPoint;
import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;
import com.boxysystems.jgoogleanalytics.LoggingAdapter;
import com.joshondesign.xml.Doc;
import com.joshondesign.xml.Elem;
import com.joshondesign.xml.XMLParser;
import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.WindowEvent;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.node.layout.StackPanel;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.OSUtil;
import org.joshy.gfx.util.localization.Localization;
import org.joshy.gfx.util.u;
import org.joshy.gfx.util.xml.XMLRequest;
import org.joshy.sketch.actions.*;
import org.joshy.sketch.actions.flickr.ViewSidebar;
import org.joshy.sketch.actions.io.*;
import org.joshy.sketch.actions.pages.PageListPanel;
import org.joshy.sketch.actions.symbols.SymbolManager;
import org.joshy.sketch.canvas.DocumentCanvas;
import org.joshy.sketch.controls.Menu;
import org.joshy.sketch.controls.Ruler;
import org.joshy.sketch.controls.StandardDialog;
import org.joshy.sketch.model.CanvasDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.modes.DocModeHelper;
import org.joshy.sketch.modes.preso.PresoModeHelper;
import org.joshy.sketch.modes.preso.ViewSlideshowAction;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.modes.vector.VectorModeHelper;
import org.joshy.sketch.property.PropertyManager;
import org.joshy.sketch.script.ScriptTools;
import org.joshy.sketch.util.UpdateChecker;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.joshy.gfx.util.localization.Localization.getString;

public class Main implements Runnable {
    private static final File BASE_STORAGE_DIR = new File(OSUtil.getBaseStorageDir());
    private static final File homedir = new File(BASE_STORAGE_DIR,"Leonardo");
    public static final File RECENT_FILES = new File(homedir,"recentfiles.xml");
    public static final File SETTINGS_FILE = new File(homedir,"settings.txt");
    public static final File SCRIPTS_DIR = new File(homedir,"scripts");
    public static Font HANDDRAWN_FONT;
    public static Font SERIF_FONT;
    public static Font SANSSERIF_FONT;

    public SymbolManager symbolManager = new SymbolManager(new File(homedir,"symbols"));
    public PropertyManager propMan;
    private QuitAction quitAction;
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
    private static final String TRACKING_PERMISSIONS = "org.joshy.gfx.sketch.tracking.allow";

    public static void main(String ... args) throws Exception {
        System.setSecurityManager(null);
        String locale = System.getProperty("user.language") + "_" + System.getProperty("user.country");
        u.p("Using locale = " + locale);

        setupSettings();

        //Localization.init(Main.class.getResource("translation.xml"),"en_US");
        Localization.init(Main.class.getResource("translation.xml"),locale);

        Core.setUseJOGL(false);
        Core.init();
        Core.getShared().defer(new Main());
    }

    private static void setupTracking() {
        if(settings.containsKey(TRACKING_PERMISSIONS)) {
            trackingEnabled = "true".equals(settings.getProperty(TRACKING_PERMISSIONS));
        } else {
            u.p("we've never asked about tracking!");
            final Stage stage = Stage.createStage();
            stage.setTitle("Usage Tracking?");
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
                    .add(new Label("Can Leonardo track how often you run it?"))
                    .add(new HFlexBox()
                        .add(new Button("Yes").onClicked(yesResponse))
                        .add(new Button("No").onClicked(noResponse))
                        .add(new Button("What's This?").onClicked(whatResponse))
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
        u.p("url = " + releaseURL);
        releaseProperties.load(releaseURL.openStream());
        CURRENT_BUILD_NUMBER = Integer.parseInt(releaseProperties.getProperty("org.joshy.sketch.build.number"));

        settings = new Properties();
        if(SETTINGS_FILE.exists()) {
            settings.load(new FileReader(SETTINGS_FILE));
        }

    }

    public static void trackEvent(String event) {
        if(trackingEnabled) {
            tracker.trackAsynchronously(new FocusPoint(event,mainApp));
        }
    }

    public void run() {
        try {
            setupTracking();
            UpdateChecker.setup(this);
            setupGlobals();
            setupNewDoc(defaultModeHelper,null);
            setupMac();
            Core.setDebugCSS(new File("test.css"));
        } catch (Exception ex) {
            u.p(ex);
        }
    }

    public DocContext setupNewDoc(DocModeHelper modeHelper, CanvasDocument doc) throws Exception {
        DocContext context = modeHelper.createDocContext(this);
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
                            c.doLayout();
                        }

                    }
                }
                .setFill(FlatColor.GREEN)
                .add(hruler,vruler)
                .add(scrollPane)
                );

        context.stackPanel.add(context.getUndoOverlay());
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
                windowMenu.addItem("Window: " + a.context.getDocument().getTitle(), a);
            }
            context.windowJMenu = windowMenu.createJMenu();
            context.menubar.add(context.windowJMenu);
        }                                               
    }

    private void setupStage(final DocContext context, DocModeHelper modeHelper) {
        final TextControl wishBox = new Textbox().setText("I wish Leonardo would...");
        final Label wishStatus = new Label("");
        wishStatus.setPrefWidth(100);
        makeAWishAction = new Callback<ActionEvent>(){
            public void call(ActionEvent actionEvent) {
                try {
                    new XMLRequest()
                            .setMethod(XMLRequest.METHOD.POST)
                            .setURL("http://joshy.org:8081/AminoWebServices/MakeAWish")
                            .setParameter("message",wishBox.getText())
                            .onComplete(new Callback<Doc>(){
                                public void call(Doc doc) {
                                    u.p("request completed");
                                    wishStatus.setText("Wish received!");
                                }
                            }).start();
                    wishBox.setText("I wish Leonardo would...");
                    wishStatus.setText("Making wish...");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        final HFlexBox statusBar = new HFlexBox();
        statusBar.setBoxAlign(HFlexBox.Align.Baseline)
                .add(wishBox,1)
                .add(new Button("Make a wish!").onClicked(makeAWishAction))
                .add(wishStatus)
                ;
        statusBar.setPrefWidth(400);


        context.mainPanel = new Panel() {
            @Override
            public void doLayout() {
                for(Control c : controlChildren()) {
                    double w= 4*50+20+30;
                    if(c == context.stackPanel) {
                        c.setTranslateY(0);
                        c.setTranslateX(30);
                        c.setWidth(getWidth()-w-30);
                        c.setHeight(getHeight()-40);
                    }
                    if(c == context.getToolbar()) {
                        c.setTranslateX(0);
                        c.setTranslateY(0);
                        c.setHeight(getHeight());
                    }
                    if(c == context.getSidebar()) {
                        c.setWidth(w);
                        c.setHeight(getHeight());
                        c.setTranslateX(getWidth()-w);
                        c.setTranslateY(0);
                    }
                    if(c == context.pageList) {
                        c.setTranslateX(20);
                        c.setTranslateY(getHeight()-100);
                        c.setHeight(100);
                        c.setWidth(getWidth()-20-w);
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
        if(context.getSidebar() != null) {
            context.mainPanel.add(context.getSidebar());
        }
        context.mainPanel.setFill(FlatColor.WHITE);

        context.getStage().setContent(context.mainPanel);
        context.setupPopupLayer();
        context.getStage().setWidth(900);
        context.getStage().setHeight(600);

        final JFrame frame = (JFrame) context.getStage().getNativeWindow();
        context.menubar = new JMenuBar();
        buildCommonMenubar(context);
        frame.setJMenuBar(context.menubar);
        EventBus.getSystem().addListener(context.getStage(), WindowEvent.Closing,new Callback<WindowEvent>() {
            public void call(WindowEvent event) {
                if(context.getDocument().isDirty()) {
                    event.veto();
                    u.p("doc is still dirty!!!");
                    StandardDialog.Result result = StandardDialog.showYesNoCancel(
                            "This document hasn't been saved yet. Save?",
                            "Save","Don't Save","Cancel");
                    if(result== StandardDialog.Result.Yes) {
                        new SaveAction(context,false).execute();
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
        contexts.remove(context);
        rebuildWindowMenu();
        if(contexts.isEmpty()) {
            showDocChooser();
        }
    }

    private void showDocChooser() {
        final Stage stage = Stage.createStage();
        Panel panel = new VFlexBox();
        for(final DocModeHelper mode : modeHelpers) {
            panel.add(new Button("New " + mode.getModeName()).onClicked(new Callback<ActionEvent>(){
                public void call(ActionEvent event) throws Exception {
                    SAction action = mode.getNewDocAction(Main.this);
                    action.execute();
                    stage.hide();
                }
            }));
        }
        stage.setContent(panel);

    }

    private void setupGlobals() throws IOException {
        HANDDRAWN_FONT = Font.fromURL(Main.class.getResource("resources/belligerent.ttf")).size(30).resolve();
        SERIF_FONT = Font.fromURL(Main.class.getResource("resources/OFLGoudyStMTT.ttf")).size(30).resolve();
        SANSSERIF_FONT = Font.fromURL(Main.class.getResource("resources/Junction 02.ttf")).size(30).resolve();

        modeHelpers.add(new VectorModeHelper(this));
        //modeHelpers.add(new PixelModeHelper(this));
        modeHelpers.add(new PresoModeHelper(this));
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
    }

    private void buildCommonMenubar(DocContext context) {
        DocumentCanvas canvas = context.getCanvas();
        //recent files menu
        Menu recentFilesMenu = new Menu().setTitle(getString("menus.recentfiles"));
        List<File> f2 = new ArrayList<File>(recentFiles);
        Collections.reverse(f2);
        for(File f : f2) {
            recentFilesMenu.addItem(f.getName(), new OpenAction(context,f));
        }
        //file menu
        Menu fileMenu = new Menu().setTitle(getString("menus.file"));
        Menu newMenu = new Menu().setTitle(getString("menus.new"));
        for(DocModeHelper helper : modeHelpers) {
            newMenu.addItem(helper.getModeName() + " document", helper.getNewDocAction(this));
        }
        fileMenu.addMenu(newMenu);
        fileMenu
                .addItem(getString("menus.open"), "O", new OpenAction(context))
                .addMenu(recentFilesMenu)
                .addItem(getString("menus.save"), "S",    new SaveAction(context, false))
                .addItem(getString("menus.saveas"), "shift S", new SaveAction(context, true))
                .addItem(getString("menus.close"), "W",   new CloseAction(canvas))
                .addMenu(new Menu().setTitle("Export")
                    .addItem(getString("menus.topng"),    new SavePNGAction(context))
                    .addItem(getString("menus.tosvg"),    new SaveSVGAction(context))
                    .addItem(getString("menus.tohtml"),   new SaveHTMLAction(context))
                    .addItem(getString("menus.tocanvas"), new SaveHTMLCanvasAction(context))
                    .addItem(getString("menus.topdf"),    new SavePDFAction(context))
                );
        quitAction = new QuitAction(this);
        aboutAction = new AboutAction(this);
        prefsAction = new PreferencesAction();

        if(!OSUtil.isMac()) {
            fileMenu.addItem(getString("menus.exit"),    "Q",       quitAction);
            fileMenu.addItem("Settings",    null,       prefsAction);
            fileMenu.addItem("About",    null,       aboutAction);
        }
        JMenuBar menubar = context.menubar;
        menubar.add(fileMenu.createJMenu());
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
        editMenu.addItem(getString("menus.setBackgroundColor"), new DocumentActions.SetBackground(context));
        editMenu.addItem("Enable Analytics Tracking", new ToggleAction(){
            @Override
            public boolean getToggleState() {
                return trackingEnabled;
            }

            @Override
            public void setToggleState(boolean toggleState) {
                trackingEnabled = toggleState;
            }
        });
        menubar.add(editMenu.createJMenu());
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
                    .separator()
                    .addItem(getString("menus.viewFlickrSidebar"), new ViewSidebar(vdc))
                    .addItem(getString("menus.viewSymbolSidebar"), new SymbolManager.ShowSymbolPanel(vdc))
                    .separator()
                    .addItem(getString("menus.viewPresentation"),"", new ViewSlideshowAction(vdc));
        }

        //view menu
        menubar.add(viewMenu.createJMenu());

        Menu shareMenu = new Menu().setTitle(getString("menus.share"))
                .addItem(getString("menus.sendTwitter"), new TwitPicAction(context))
                .addItem(getString("menus.configTwitter"), new TwitPicAction.ChangeSettingsAction(context, true));
        if(OSUtil.isMac()) {
            shareMenu.addItem(getString("menus.sendEmailPNG"), new SendMacMail(context));
        }
        menubar.add(shareMenu.createJMenu());

        Menu scriptMenu = new Menu().setTitle(getString("menus.scripts"));
        if(SCRIPTS_DIR.exists()) {
            for(File file : SCRIPTS_DIR.listFiles()) {
                if(file.exists() && file.getName().toLowerCase().endsWith(".js")) {
                    scriptMenu.addItem(file.getName(),new ScriptTools.RunScriptAction(file,context));
                }
            }
        }
        menubar.add(scriptMenu.createJMenu());
    }

    private List<File> loadRecentDocs(File file) {
        List<File> files = new ArrayList<File>();
        if(file.exists()) {
            try {
                Doc doc = XMLParser.parse(file);
                for(Elem e : doc.xpath("//file")) {
                    files.add(new File(e.attr("filepath")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return files;
    }

    private void setupMac() {
        Application application = Application.getApplication();
        application.setEnabledAboutMenu(true);
        application.setEnabledPreferencesMenu(true);
        application.addApplicationListener(new ApplicationListener(){

            public void handleAbout(ApplicationEvent applicationEvent) {
                try {
                    aboutAction.execute();
                    applicationEvent.setHandled(true);
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            public void handleOpenApplication(ApplicationEvent applicationEvent) {
            }

            public void handleOpenFile(ApplicationEvent applicationEvent) {
            }

            public void handlePreferences(ApplicationEvent applicationEvent) {
                try {
                    prefsAction.execute();
                    applicationEvent.setHandled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void handlePrintFile(ApplicationEvent applicationEvent) {
            }

            public void handleQuit(ApplicationEvent applicationEvent) {
                quitAction.execute();
                applicationEvent.setHandled(true);
            }

            public void handleReOpenApplication(ApplicationEvent applicationEvent) {
            }
        });
    }

    public static URL getIcon(String s) {
        return Main.class.getResource("resources/"+s);
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

