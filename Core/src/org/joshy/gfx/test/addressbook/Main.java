package org.joshy.gfx.test.addressbook;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.GridLineLayout;
import org.joshy.gfx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 25, 2010
 * Time: 4:34:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main implements Runnable {
    private List<Person> people;
    private FilteredListModel<Person> model;

    public static void main(String ... args) throws Exception, InterruptedException {
        Core.setUseJOGL(false);
        Core.init();
        Core.getShared().defer(new Main());
    }

    public void run() {
        try {
            initData();

            //create components and their models
            ListView results = new ListView();
            model = new FilteredListModel<Person>(people);
            results.setModel(model);
            Control searchBox = new Textbox();
            final EntryPanel entryPanel = new EntryPanel();
            //entryPanel.setWidth(100);
            //entryPanel.setHeight(100);

            final Button addButton = new Button("+");
            final Button editButton = new Button("Edit");

            //set up the layout
            //create a grid line layout
            GridLineLayout panel = new GridLineLayout();
            //configure the columns
            panel.setColumn(0, 0, GridLineLayout.ColumnAnchor.LEFT);
            panel.setColumn(1, 200, GridLineLayout.ColumnAnchor.LEFT);
            panel.setColumn(2, 0, GridLineLayout.ColumnAnchor.RIGHT);
            panel.setRow(0, GridLineLayout.RowAnchor.TOP);
            panel.setRow(1, GridLineLayout.RowAnchor.TOP);
            panel.setRow(2, GridLineLayout.RowAnchor.BOTTOM);

            panel.add(0, 0, searchBox, GridLineLayout.Resize.NO, GridLineLayout.VResize.NONE);
            panel.add(0, 1, results, GridLineLayout.Resize.FILL, GridLineLayout.VResize.FILL);
            panel.add(1, 1, entryPanel, GridLineLayout.Resize.FILL, GridLineLayout.VResize.FILL);
            panel.add(0, 2, addButton, GridLineLayout.Resize.NO);
            panel.add(1, 2, editButton, GridLineLayout.Resize.NO);

            // make the gui pretty with a custom renderer
            results.setRenderer(new ListView.ItemRenderer<Person>(){
                public void draw(GFX gfx, ListView listView, Person item, int index, double x, double y, double width, double height) {
                    if(index % 2 == 0) {
                        gfx.setPaint(new FlatColor(0.8,0.8,0.8,1.0));
                    } else {
                        gfx.setPaint(FlatColor.WHITE);
                    }
                    if(index == listView.getSelectedIndex()) {
                        gfx.setPaint(FlatColor.BLUE);
                    }
                    gfx.fillRect(x,y,width,height);
                    gfx.setPaint(FlatColor.BLACK);
                    gfx.drawText(item.first + " " + item.last, Font.DEFAULT, x+2, y+15);
                }
            });
            

            // add event handlers
            // update the filter when the search box changes
            EventBus.getSystem().addListener(searchBox, ChangedEvent.StringChanged,new Callback<ChangedEvent>() {
                public void call(ChangedEvent event) {
                    model.setFilterText(event.getValue().toString());
                }
            });

            // update the entry panel when the list selection changes
            EventBus.getSystem().addListener(results, SelectionEvent.Changed, new Callback<SelectionEvent>(){
                public void call(SelectionEvent event) {
                    if(event.getView().getSelectedIndex() == ListView.NO_SELECTION) {
                        entryPanel.setPerson(null);
                    } else {
                        entryPanel.setPerson(model.get(event.getView().getSelectedIndex()));
                    }
                }
            });

            //add a new dummy person when you hit the addButton
            EventBus.getSystem().addListener(ActionEvent.Action, new Callback<ActionEvent>() {
                public void call(ActionEvent event) {
                    if(event.getSource() == addButton) {
                        model.add(new Person("Billy","Bob","444-555-6666","777-888-999"));
                    }
                    if(event.getSource() == editButton) {
                    }
                }
            });
            
            // drop into a window
            Stage stage = Stage.createStage();
            stage.setContent(panel);

            // done
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        people = new ArrayList<Person>();
        people.add(new Person("Josh","Marinacci","678-458-5810","777-888-999"));
        people.add(new Person("Jennifer","Marinacci","678-458-5810","777-888-999"));
        people.add(new Person("Rachel","Hill","678-458-5810","777-888-999"));
        people.add(new Person("Kevin","Hill","678-458-5810","777-888-999"));
    }

    private class FilteredListModel<People> implements ListModel<Person> {
        private List<Person> data;
        private List<Person> view;

        public FilteredListModel(List<Person> list) {
            this.data = list;
            this.view = new ArrayList<Person>(this.data);
        }

        public Person get(int i) {
            return view.get(i);
        }

        public int size() {
            return view.size();
        }

        public void setFilterText(String text) {
            String filter = text.toLowerCase();
            this.view.clear();
            for(Person p : this.data) {
                if(p.first.toLowerCase().contains(filter)) {
                    view.add(p);
                    continue;
                }
                if(p.last.toLowerCase().contains(filter)) {
                    view.add(p);
                    continue;
                }
            }

            EventBus.getSystem().publish(new ListView.ListEvent(ListView.ListEvent.Updated, this));
        }

        public void add(Person person) {
            data.add(person);
            this.view.clear();
            view.addAll(data);
            EventBus.getSystem().publish(new ListView.ListEvent(ListView.ListEvent.Updated, this));            
        }
    }
}
