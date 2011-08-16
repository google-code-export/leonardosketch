package org.joshy.sketch.modes.pixel;

import org.joshy.sketch.Main;
import org.joshy.sketch.actions.NewAction;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.pixel.model.PixelDoc;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 8/15/11
 * Time: 9:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class TiledPixelModeHelper extends PixelModeHelper {
    public TiledPixelModeHelper(Main main) {
        super(main);
    }

    @Override
    public CharSequence getModeName() {
        return "Tiled Bitmap";
    }

    @Override
    public SAction getNewDocAction(Main main) {
        return new NewAction(main) {
            @Override
            protected void newDocDialog() {
                PixelDoc doc = new PixelDoc();
                doc.setRepeatSize(16);
                doc.setRepeat(true);
                try {
                    main.setupNewDoc(new TiledPixelModeHelper(main),doc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
