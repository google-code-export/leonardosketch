package assetmanager;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.sketch.actions.swatches.Palette;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: josh
 * Date: 4/13/12
 * Time: 1:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class RenderUtil {
    public static Node fontToImage(Asset asset) {
        BufferedImage img = new BufferedImage(100,17,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(java.awt.Color.WHITE);
        g2.fillRect(0, 0, img.getWidth(), img.getHeight());
        g2.setPaint(java.awt.Color.BLACK);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, asset.getFile()).deriveFont(15f);
            g2.setFont(font);
            g2.drawString("ABCD efgh 1234",3,img.getHeight()-3);
        } catch (FontFormatException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        g2.dispose();
        javafx.scene.image.Image fximg = javafx.scene.image.Image.impl_fromExternalImage(img);
        return new ImageView(fximg);
    }


    public static ImageView patternToImage(Asset asset) {
        File file = asset.getFile();
        try {
            ImageView iv = new ImageView(new javafx.scene.image.Image(new FileInputStream(file)));
            iv.setViewport(new Rectangle2D(0, 0, 100, 15));
            return iv;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static javafx.scene.image.Image toImage(Palette pal) {
        BufferedImage img = new BufferedImage(100,15,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(java.awt.Color.RED);
        g2.fillRect(0, 0, 10, 10);
        int x = 0;
        for(FlatColor color : pal.getColors()) {
            g2.setPaint(new java.awt.Color((float)color.getRed(),(float)color.getGreen(),(float)color.getBlue()));
            g2.fillRect(x*4,0,4,10);
            x++;
        }
        g2.dispose();
        javafx.scene.image.Image fximg = javafx.scene.image.Image.impl_fromExternalImage(img);
        return fximg;
    }


}
