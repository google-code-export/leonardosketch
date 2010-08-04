package org.joshy.sketch.modes.pixel;

import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.control.Slider;
import org.joshy.gfx.node.control.SwatchColorPicker;
import org.joshy.gfx.node.layout.HAlign;
import org.joshy.gfx.node.layout.HBox;
import org.joshy.sketch.controls.HistogramColorPicker;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 7, 2010
 * Time: 8:42:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class PixelToolbar extends HBox {
    public SwatchColorPicker pixelColorPicker;
    public HistogramColorPicker histogramColorPicker;
    public Slider brushWidthSlider;
    public Slider brushHardnessSlider;
    public Slider brushOpacitySlider;

    public PixelToolbar(PixelDocContext context) throws IOException {
        this.setHAlign(HAlign.TOP);
        pixelColorPicker = new SwatchColorPicker();
        this.add(pixelColorPicker);
        histogramColorPicker = new HistogramColorPicker(context);
        this.add(histogramColorPicker);
        this.add(new Label("W:"));
        brushWidthSlider = new Slider(false).setValue(10).setMin(1).setMax(100);
        this.add(brushWidthSlider);
        this.add(new Label("S:"));
        brushHardnessSlider = new Slider(false).setValue(1.0).setMin(0).setMax(1.0);
        this.add(brushHardnessSlider);
        this.add(new Label("O:"));
        brushOpacitySlider = new Slider(false).setValue(1.0).setMin(0).setMax(1.0);
        this.add(brushOpacitySlider);
    }
}
