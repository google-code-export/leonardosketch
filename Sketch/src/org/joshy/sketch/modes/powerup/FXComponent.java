package org.joshy.sketch.modes.powerup;

import com.joshondesign.xml.XMLWriter;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 4/4/12
 * Time: 2:19 PM
 * To change this template use File | Settings | File Templates.
 */
public interface FXComponent {
    public String getXMLElementName();

    public void exportAttributes(XMLWriter out);
}
