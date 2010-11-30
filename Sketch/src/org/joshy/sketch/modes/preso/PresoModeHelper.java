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
            title.setText(" - title -");
            title.setFillPaint(FlatColor.BLACK);
            title.setTranslateX(300);
            title.setTranslateY(150);
            title.setFontName("Arial");
            title.setFontSize(64);
            page.add(title);

            SText subtitle = new SText();
            subtitle.setText("- subtitle -");
            subtitle.setFillPaint(FlatColor.BLACK);
            subtitle.setTranslateX(300);
            subtitle.setTranslateY(300);
            subtitle.setFontName("Arial");
            subtitle.setFontSize(48);
            page.add(subtitle);
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
            header.setText(" - header -");
            header.setFillPaint(FlatColor.BLACK);
            header.setTranslateX(100);
            header.setTranslateY(50);
            header.setFontName("Arial");
            header.setFontSize(48);
            page.add(header);

            SText content = new SText();
            content.setText("- content -");
            content.setFillPaint(FlatColor.BLACK);
            content.setTranslateX(100);
            content.setTranslateY(150);
            content.setFontName("Arial");
            content.setFontSize(36);
            page.add(content);
        }
    }
}
