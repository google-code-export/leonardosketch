package org.joshy.sketch.actions;

import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.sketch.model.Page;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.modes.vector.VectorDocContext;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 11/3/11
 * Time: 1:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class MakeLink extends SAction {
    private VectorDocContext context;

    public MakeLink(VectorDocContext context) {
        super();
        this.context = context;
    }

    @Override
    public CharSequence getDisplayName() {
        return "Make Link";
    }

    @Override
    public void execute() throws Exception {
        final Stage stage = Stage.createStage();
        final Iterable<? extends SNode> items = context.getSelection().items();
        VFlexBox box = new VFlexBox();
        for(Page page : context.getDocument().getPages()) {
            final String id = page.getId();
            Button bt = new Button("page " + id);
            bt.onClicked(new Callback<ActionEvent>() {
                public void call(ActionEvent event) throws Exception {
                    stage.hide();
                    for(SNode node : items) {
                        node.setLinkTarget(id);
                    }
                }
            });
            box.add(bt);
        }
        stage.setContent(box);
    }
}
