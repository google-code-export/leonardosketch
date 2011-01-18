package org.joshy.sketch.pixel.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/17/11
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class PixelDocument {
    private List<PixelLayer> layers;

    public PixelDocument() {
        layers = new ArrayList<PixelLayer>();
    }
}
