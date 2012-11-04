package org.joshy.sketch.modes.preso;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.controls.Menu;
import org.joshy.sketch.model.CanvasDocument;
import org.joshy.sketch.model.SText;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;
import static org.joshy.sketch.modes.preso.SwitchTheme.Reveal.HEADER;
import static org.joshy.sketch.modes.preso.SwitchTheme.Reveal.SUBHEADER;
import static org.joshy.sketch.modes.preso.SwitchTheme.Reveal.TEXT_CLASS;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.modes.vector.VectorModeHelper;

import static org.joshy.gfx.util.localization.Localization.getString;

/**
 * A context for Presentation documents
 */
public class PresoModeHelper extends VectorModeHelper {

    public PresoModeHelper(Main main) {
        super(main);
    }


    @Override
    public boolean isPageListVisible() {
        return true;
    }

    @Override
    public Menu buildPageMenu(VectorDocContext context) {
        Menu menu = new Menu();
        menu.setTitle("Page");
        menu.addItem("Add Title Page", new AddTitlePage(context));
        menu.addItem("Add Content Page", new AddContentPage(context));
        menu.addItem("Delete Selected Page", new DeletePageAction());
        menu.separator();
        menu.addItem(getString("menus.viewPresentation"), "", new ViewSlideshowAction(context));
        menu.separator();
        menu.addItem("Switch to Standard Theme", new SwitchTheme.Standard(null, context));
        menu.addItem("Switch to Cowboy Theme", new SwitchTheme.Cowboy(null, context));
        menu.addItem("Switch to Future Theme", new SwitchTheme.Future(null,context));
        menu.addItem("Switch to Classy Theme", new SwitchTheme.Classy(null,context));
        return menu;
    }

    static class AddTitlePage extends SAction {
        private VectorDocContext context;

        AddTitlePage(VectorDocContext context){
            this.context = context;
        }

        @Override
        public void execute() {
            SketchDocument doc = context.getDocument();
            SketchDocument.SketchPage page = doc.addPage();
            insertContents(page);
            context.pageList.listview.setSelectedIndex(doc.getCurrentPageIndex());
        }

        public void insertContents(SketchDocument.SketchPage page) {
            SwitchTheme.PresoThemeAction theme = (SwitchTheme.PresoThemeAction) page.getDocument().getProperties().get("theme");
            SText title = new SText();
            title.setText("Presentation Title");
            title.setFillPaint(FlatColor.BLACK);
            title.setStringProperty(TEXT_CLASS, HEADER);
//            title.setStringProperty("text-class","title");
            title.setTranslateY(140);
            theme.styleText(title);
            center(title, page);
            page.add(title);

            SText subtitle = new SText();
            subtitle.setText("by the author");
            subtitle.setStringProperty(TEXT_CLASS, SUBHEADER);
//            subtitle.setStringProperty("text-class", "subtitle");
            subtitle.setTranslateY(280);
            theme.styleText(subtitle);
            center(subtitle, page);
            page.add(subtitle);
        }

        private void center(SText title, SketchDocument.SketchPage page) {
            title.setHalign(SText.HAlign.Center);
            title.setTranslateX((page.getDocument().getWidth() - title.getWidth()) / 2);
        }
    }

    private class DeletePageAction extends SAction {
        @Override
        public void execute() {
            
        }
    }
    
    @Override
    public CharSequence getModeName() {
        return getString("misc.presentation");
    }

    @Override
    public SAction getNewDocAction(Main main) {
        return new NewPresentationDocAction(main);
    }

    @Override
    public CanvasDocument createNewDoc() {
        SketchDocument doc = new SketchDocument();
        doc.setPagesVisible(true);
        SketchDocument.SketchPage page = doc.getCurrentPage();
        new PresoModeHelper.AddTitlePage(null).insertContents(page);
        return doc;
    }

    static class AddContentPage extends SAction {
        private VectorDocContext context;

        public AddContentPage(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public void execute() {
            SketchDocument doc = context.getDocument();
            SketchDocument.SketchPage page = doc.addPage();
            insertContents(page);
            context.pageList.listview.setSelectedIndex(doc.getCurrentPageIndex());
        }
        public void insertContents(SketchDocument.SketchPage page) {
            SwitchTheme.PresoThemeAction theme = (SwitchTheme.PresoThemeAction) page.getDocument().getProperties().get("theme");
            SText header = new SText();
            header.setText("A Page Header");
            header.setStringProperty(TEXT_CLASS, SUBHEADER);
            header.setTranslateX(50);
            header.setTranslateY(40);
            header.setBulleted(false);
            theme.styleText(header);
            page.add(header);

            SText content = new SText();
            content.setText("my first point\nmy second point\nmy third point\nmy fourth point");
            content.setStringProperty(TEXT_CLASS, SwitchTheme.Reveal.LIST);
            content.setTranslateX(70);
            content.setTranslateY(140);
            content.setBulleted(true);
            theme.styleText(content);
            page.add(content);
        }
    }

    @Override
    public void addCustomExportMenus(Menu exportMenu, DocContext context) {
        super.addCustomExportMenus(exportMenu, context);
        exportMenu.separator();
        exportMenu.addItem("Export to HTML Presentation", new ExportHTMLPresentationAction((VectorDocContext)context));
    }
}
