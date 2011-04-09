package org.joshy.sketch.actions.swatches;

import com.joshondesign.xml.Doc;
import com.joshondesign.xml.Elem;
import com.joshondesign.xml.XMLParser;
import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.draw.PatternPaint;
import org.joshy.gfx.node.control.ListModel;
import org.joshy.gfx.util.ArrayListModel;
import org.joshy.gfx.util.u;
import org.joshy.sketch.Main;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 4/7/11
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatternManager {
    private File file;
    private ArrayListModel<PatternPaint> patternModel = new ArrayListModel<PatternPaint>();

    public PatternManager(File file) {
        this.file = file;
        if(this.file.exists()) {
            loadPatterns(file);
        } else {
            try {
                initDummyData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initDummyData() throws IOException {
        //PatternPaint pt1 = PatternPaint.create(SRect.class.getResource("resources/button1.png"));
        PatternPaint pt1 = PatternPaint.create(Main.class.getResource(
                "resources/textures/brown_noise.png"),"brown_noise.png");
        //pt1 = pt1.deriveNewStart(new Point(40,40));
        PatternPaint pt2 = PatternPaint.create(Main.class.getResource(
                "resources/textures/checkerboard.png"),"checkerboard.png");
        PatternPaint pt3 = PatternPaint.create(Main.class.getResource(
                "resources/textures/flurdelis.png"),"flurdelis.png");
        PatternPaint pt4 = PatternPaint.create(Main.class.getResource(
                "resources/textures/tess01.png"),"tess01.png");
        PatternPaint pt5 = PatternPaint.create(Main.class.getResource(
                "resources/textures/wallpaper1.png"),"wallpaper1.png");
        //PatternPaint pt6 = PatternPaint.create(Main.class.getResource("resources/textures/webtreats-paper-pattern-6-grey.jpg"),"t6");

        patternModel = new ArrayListModel<PatternPaint>();
        patternModel.add(pt1);
        patternModel.add(pt2);
        patternModel.add(pt3);
        patternModel.add(pt4);
        patternModel.add(pt5);
        //patternModel.add(pt6);

    }

    public ListModel getModel() {
        return (ListModel)this.patternModel;
    }

    private void loadPatterns(File file) {
        u.p("loading patterns from xml file: " + file.getAbsolutePath());
        try {
            Doc doc = XMLParser.parse(file);
            for(Elem c : doc.xpath("/patterns/pattern")) {
                File f = new File(file.getParentFile(),c.attr("src"));
                u.p("loading f " + f.getAbsolutePath());
                patternModel.add(PatternPaint.create(f));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addPattern(PatternPaint pat) {
        patternModel.add(pat);
        try {
            save();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void save() throws FileNotFoundException, UnsupportedEncodingException {
        if(!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        XMLWriter xml = new XMLWriter(file).header();
        xml.start("patterns");
        for(PatternPaint c : patternModel) {
            String url = c.getRelativeURL();
            if(!url.toLowerCase().endsWith(".png")) {
                url = c.getRelativeURL()+".png";
            }
            xml.start("pattern","src",url).end();
            try {
                File patfile = new File(file.getParentFile(),url);
                if(patfile.exists()) {
                    u.p("skipping pattern: " + patfile.getAbsolutePath());
                }else {
                    u.p("saving pattern: " + patfile.getAbsolutePath());
                    ImageIO.write(c.getImage(),"png",patfile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        xml.end();
        xml.close();
        u.p("wrote out patterns to : " + file.getAbsolutePath());
    }
}
