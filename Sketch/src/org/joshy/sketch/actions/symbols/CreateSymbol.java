package org.joshy.sketch.actions.symbols;

import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.model.SNode;

import static org.joshy.gfx.util.localization.Localization.getString;

public class CreateSymbol extends SAction {
    private VectorDocContext context;

    public CreateSymbol(VectorDocContext context) {
        this.context = context;
    }


    @Override
    public CharSequence getDisplayName() {
        return getString("menus.createSymbol");
    }

    @Override
    public void execute() {
        if(context.getSelection().isEmpty()) return;
        if(context.getSelection().size() > 1) return;

        SNode shape = context.getSelection().items().iterator().next();
        SNode dupe = shape.duplicate(null);
        dupe.setTranslateX(0);
        dupe.setTranslateY(0);

        context.getSymbolManager().add(dupe);
        //context.getSymbolManager().save();
    }
}
