package org.joshy.gfx.test.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.SelectionEvent;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.*;
import org.joshy.gfx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Aug 28, 2010
 * Time: 6:55:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class LayoutTests implements Runnable {
    private SplitPane split;
    private static final String SIMPLE_HBOX = "Simple HBox";
    private static final String SIMPLE_HBOX_BASELINE = "Simple HBox with baseline";
    private static final String HBOX_WITH_SPACERS = "Hbox with spacers";
    private static final String VBOX_WITH_TOOLBAR_AND_STATUSBAR = "VBox with toolbar and statusbar";
    private static final String VBOX_SIMPLE = "VBox simple";
    private static final String VBOX_RIGHT_SIMPLE = "VBox simple, right aligned";
    private static final String VBOX_STRETCH_SIMPLE = "VBox simple, stretch";
    private static final String COMPLEX_CENTER_ALIGNED_DIALOG = "Complex center aligned dialog";

    public static void main(String ... args) throws Exception {
        Core.init();
        Core.getShared().defer(new LayoutTests());

    }

    public void run() {
        Stage stage = Stage.createStage();
        stage.setWidth(600);
        stage.setHeight(400);

        split = new SplitPane(false);
        stage.setContent(split);
        split.setPosition(200);

        List<String> tests = new ArrayList<String>();
        tests.add(SIMPLE_HBOX);
        tests.add(SIMPLE_HBOX_BASELINE);
        tests.add(HBOX_WITH_SPACERS);
        tests.add(VBOX_SIMPLE);
        tests.add(VBOX_RIGHT_SIMPLE);
        tests.add(VBOX_STRETCH_SIMPLE);
        tests.add(VBOX_WITH_TOOLBAR_AND_STATUSBAR);
        tests.add(COMPLEX_CENTER_ALIGNED_DIALOG);

        final ListView<String> testView =new ListView<String>().setModel(ListView.createModel(tests)); 
        split.setFirst(testView);
        split.setSecond(new Panel());
        EventBus.getSystem().addListener(SelectionEvent.Changed, new Callback<SelectionEvent>() {
            public void call(SelectionEvent event) {
                if(event.getView() == testView) {
                    split.setSecond(getTest(testView.getModel().get(testView.getSelectedIndex())));
                }
            }
        });
    }

    private Control getTest(String testName) {
        //u.p("doing test " + testName);

        if(SIMPLE_HBOX.equals(testName)) {
            return new HFlexBox().add(new Label("Label"),new Button("Button"), createPopup("Popup List"));
        }

        if(SIMPLE_HBOX_BASELINE.equals(testName)) {
            return new HFlexBox().setBoxAlign(FlexBox.Align.Baseline).add(new Label("Label"),new Button("Button"), createPopup("Popup List"));
        }

        if(HBOX_WITH_SPACERS.equals(testName)) {
            return new HFlexBox()
                    .add(new Button("before"))
                    .add(new Spacer(),1)
                    .add(new Button("between"))
                    .add(new Spacer(),1)
                    .add(new Button("after"))
                    .add(new Button("stretchy button"));
        }

        if(VBOX_SIMPLE.equals(testName)) {
            return new VFlexBox()
                    .add(new Button("B1"))
                    .add(new Button("B2"))
                    .add(new Button("super long button"));
        }
        if(VBOX_RIGHT_SIMPLE.equals(testName)) {
            return new VFlexBox()
                    .setBoxAlign(FlexBox.Align.Right)
                    .add(new Button("B1"))
                    .add(new Button("B2"))
                    .add(new Button("super long button"));
        }
        if(VBOX_STRETCH_SIMPLE.equals(testName)) {
            return new VFlexBox()
                    .setBoxAlign(FlexBox.Align.Stretch)
                    .add(new Button("B1"))
                    .add(new Button("B2"))
                    .add(new Button("super long button"));
        }

        if(VBOX_WITH_TOOLBAR_AND_STATUSBAR.equals(testName)) {
            FlexBox toolbar = new HFlexBox();
            toolbar.add(new Button("B1"),new Button("B2"), new Button("B3"));
            toolbar.add(new Spacer(),1);
            toolbar.add(new Button("B4"),new Button("B5"),new Button("B6"));

            FlexBox statusbar = new HFlexBox()
                    .add(new Label("left edge"))
                    .add(new Spacer(),1)
                    .add(new Label("right edge"));

            return new VFlexBox()
                    .add(toolbar)
                    .add(new Panel().setFill(FlatColor.RED),1)
                    .add(statusbar);
        }
        if(COMPLEX_CENTER_ALIGNED_DIALOG.equals(testName)) {
            return new GridBox()
                .setPadding(5)
                .column(120, GridBox.Align.Right)
                .column(200, GridBox.Align.Left)
                    .addControl(new Label("Appearance:"))
                    .addControl(createPopup("Blue"))
                .nextRow()
                    .skip()
                    .addControl(new Label("For the overall look of buttons, menus, and windows"))
                .nextRow()
                    .addControl(new Label("Highlight Color"))
                    .addControl(createPopup("Blue"))
                .nextRow()
                    .skip().addControl(new Label("For selected text"))
                .nextRow()
                    .addControl(new Label("Place scroll arrows:"))
                    .addControl(new Radiobutton("Together"))
                .nextRow()
                    .skip()
                    .addControl(new Radiobutton("At top and bottom"))
                .nextRow()
                    .addControl(new Label("Click in the scroll bar to:"))
                    .addControl(new Radiobutton("Jump to the next page"))
                .nextRow()
                    .skip()
                    .addControl(new Radiobutton("Jump to the spot that's clicked"))
                .nextRow()
                    .skip()
                    .addControl(new Checkbox("Use smooth scrolling"))
                .nextRow()
                    .skip()
                    .addControl(new Checkbox("Double-click a window's title bar to minimize"))
                .nextRow()
                    .addControl(new Label("Number of recent items:"))
                    .addControl(new HFlexBox().add(createPopup("10"),new Label("Applications")).setHeight(20))
                .nextRow()
                    .skip()
                    .addControl(new HFlexBox().add(createPopup("10"),new Label("Documents")).setHeight(20))
                .nextRow()
                    .skip()
                    .addControl(new HFlexBox().add(createPopup("10"),new Label("Servers")).setHeight(20))
                .nextRow()
                    .skip()
                    .addControl(new Checkbox("Use LCD font smoothing when available"))
                .nextRow()
                    .addControl(new HFlexBox().add(
                        new Label("Turn off font smoothing for font sizes")
                        ,createPopup("4")
                        ,new Label("and smaller")).setHeight(20));
        }

        return new Panel();
    }

    private static PopupMenuButton createPopup(final String s) {
        PopupMenuButton<String> pm = new PopupMenuButton<String>();
        pm.setModel(new ListModel<String>() {

            public String get(int i) {
                return s;
            }

            public int size() {
                return 3;
            }
        });
        return pm;
    }
}
