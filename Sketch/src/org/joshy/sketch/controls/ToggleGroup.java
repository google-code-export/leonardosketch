package org.joshy.sketch.controls;

import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.control.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Apr 7, 2010
 * Time: 4:30:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToggleGroup {
    private List<Button> buttons;
    private Button selectedButton;

    public ToggleGroup() {
        buttons = new ArrayList<Button>();
    }

    public ToggleGroup add(Button button) {
        buttons.add(button);
        EventBus.getSystem().addListener(button,ActionEvent.Action, new Callback<ActionEvent>() {
            public void call(ActionEvent event) {
                setSelectedButton((Button)event.getSource());
            }
        });
        return this;
    }

    public Button getSelectedButton() {
        return selectedButton;
    }

    public void setSelectedButton(Button button) {
        if(buttons.contains(button)) {
            for(Button b : buttons) {
                if(b == button) {
                    selectedButton = b;
                    b.setSelected(true);
                } else {
                    b.setSelected(false);
                }
            }
        }
        EventBus.getSystem().publish(new ActionEvent(ActionEvent.Action,this));
    }
}
