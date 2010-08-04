package org.joshy.gfx.test.addressbook;

import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.layout.GridLineLayout;
import org.joshy.gfx.node.layout.HBox;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.node.layout.VBox;
import org.joshy.gfx.util.u;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 26, 2010
 * Time: 4:06:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class EntryPanel extends GridLineLayout {
    private Person person;
    private Label firstNameField;
    private Label lastNameField;
    private Label homePhone;
    private Label mobilePhone;

    public EntryPanel() {

        firstNameField = new Label("Josh");
        lastNameField = new Label("Marinacci");
        homePhone = new Label("Home");
        mobilePhone = new Label("Home");
        HBox name = new HBox();
        name.setSpacing(5);
        name.add(firstNameField);
        name.add(lastNameField);

        //configure the columns
        setColumn(0, 0, GridLineLayout.ColumnAnchor.LEFT);
        setColumn(1, 100, GridLineLayout.ColumnAnchor.LEFT);
        setRow(0, GridLineLayout.RowAnchor.TOP);
        setRow(1, GridLineLayout.RowAnchor.TOP);
        setRow(2, GridLineLayout.RowAnchor.TOP);

        add(0, 0, new Label("name:"), GridLineLayout.Resize.RIGHT);
        add(1, 0, name, GridLineLayout.Resize.NO, GridLineLayout.VResize.NONE);
        add(0, 1, new Label("home:"), GridLineLayout.Resize.RIGHT);
        add(1, 1, homePhone, GridLineLayout.Resize.NO);
        add(0, 2, new Label("mobile:"), GridLineLayout.Resize.RIGHT);
        add(1, 2, mobilePhone, GridLineLayout.Resize.NO);
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
        if(this.person == null) {
            firstNameField.setText("");
            lastNameField.setText("");
            homePhone.setText("");
            mobilePhone.setText("");
        } else {
            firstNameField.setText(person.first);
            lastNameField.setText(person.last);
            homePhone.setText(person.homeNumber);
            mobilePhone.setText(person.mobileNumber);
        }
    }


}
