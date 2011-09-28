package org.joshy.sketch.modes.preso;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.controls.Menu;
import org.joshy.sketch.model.CanvasDocument;
import org.joshy.sketch.model.SText;
import org.joshy.sketch.model.SketchDocument;
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
        menu.addItem(getString("menus.viewPresentation"),"", new ViewSlideshowAction(context));
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
            SText title = new SText();
            title.setText("Presentation Title");
            title.setFillPaint(FlatColor.BLACK);
            title.setFontName("OpenSans");
            title.setFontSize(70);

            title.setAutoSize(false);
            title.setWidth(600);
            center(title,page);

            title.setTranslateY(190);

            page.add(title);

            SText subtitle = new SText();
            subtitle.setText("by the author");
            subtitle.setFillPaint(FlatColor.BLACK);
            subtitle.setFontName("OpenSans");
            subtitle.setFontSize(36);
            subtitle.setAutoSize(false);
            subtitle.setWidth(350);
            center(subtitle,page);

            subtitle.setTranslateY(280);
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
        SketchDocument.SketchPage page = doc.getCurrentPage();
        new PresoModeHelper.AddTitlePage(null).insertContents(page);
        return doc;
    }

    private static class AddContentPage extends SAction {
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
            SText header = new SText();
            header.setText("A Page Header");
            header.setFillPaint(FlatColor.BLACK);
            header.setTranslateX(50);
            header.setTranslateY(40);
            header.setFontName("OpenSans");
            header.setFontSize(48);
            page.add(header);

            SText content = new SText();
            content.setText("my first point\nmy second point\nmy third point\nmy fourth point");
            content.setFillPaint(FlatColor.BLACK);
            content.setTranslateX(70);
            content.setTranslateY(140);
            content.setFontName("OpenSans");
            content.setFontSize(30);
            page.add(content);
        }
    }
}
