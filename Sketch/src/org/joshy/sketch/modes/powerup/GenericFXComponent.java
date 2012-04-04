package org.joshy.sketch.modes.powerup;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.draw.GFX;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SResizeableNode;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 4/4/12
 * Time: 3:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenericFXComponent extends FXAbstractComponent implements FXComponent, SResizeableNode {
    private DrawDelegate dd;
    private Map<String,Object> props;
    private int initialWidth;
    private String xmlElementName;
    private int initialHeight;

    public static interface DrawDelegate {
        public void draw(GFX g, GenericFXComponent genericFXComponent);
    }

    public GenericFXComponent(DrawDelegate dd, Map<String, Object> props, int width, int height, String xmlClassName) {
        this.dd = dd;
        this.props = props;
        this.initialWidth = width;
        this.setWidth(width);
        this.initialHeight = height;
        this.setHeight(height);
        this.xmlElementName = xmlClassName;
    }
    public void draw(GFX g) {
        this.dd.draw(g, this);
    }

    @Override
    public SNode duplicate(SNode dupe) {
        if(dupe == null) {
            dupe = new GenericFXComponent(this.dd, props, initialWidth, initialHeight, xmlElementName);
        }
        return super.duplicate(dupe);
    }

    public String getXMLElementName() {
        return xmlElementName;
    }

    public void exportAttributes(XMLWriter out) {
        for(String key : props.keySet()) {
            out.attr(key,""+props.get(key));
        }
    }
}
