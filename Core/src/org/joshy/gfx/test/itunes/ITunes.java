package org.joshy.gfx.test.itunes;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.Growbar;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 28, 2010
 * Time: 6:53:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class ITunes implements Runnable {
    
    public static void main(String... args) throws Exception {
        Core.setUseJOGL(false);
        Core.init();
        Core.getShared().defer(new ITunes());
    }

    public void run() {
        try {
            Stage stage = Stage.createStage();
            stage.setMinimumWidth(700);
            stage.setMinimumHeight(400);


            Button prevButton = new Button("");
            prevButton.setVariant("imageOnly");
            prevButton.setNormalIcon(new File("assets/itunes/prevButton.png").toURI().toURL());
            prevButton.setPressedIcon(new File("assets/itunes/prevButton_pressed.png").toURI().toURL());
            
            Button playButton = new Button("");
            playButton.setVariant("imageOnly");
            playButton.setNormalIcon(new File("assets/itunes/play_button.png").toURI().toURL());
            playButton.setPressedIcon(new File("assets/itunes/pause_button.png").toURI().toURL());
            
            Button nextButton = new Button("");
            nextButton.setVariant("imageOnly");
            nextButton.setNormalIcon(new File("assets/itunes/nextButton.png").toURI().toURL());
            nextButton.setPressedIcon(new File("assets/itunes/nextButton_pressed.png").toURI().toURL());

            final Slider volume = new Slider(false);
            volume.setMin(1);
            volume.setValue(5);
            volume.setMax(20);


            final Panel statusPanel = new PlayerStatusPanel();
            statusPanel.setHeight(50);
            statusPanel.setFill(FlatColor.PURPLE);
            
            Button listToggle = new Button("L").setVariant("footerControl");
            Button gridToggle = new Button("G").setVariant("footerControl");
            Button coverflowToggle = new Button("C").setVariant("footerControl");

            final Textbox search = new Textbox();
            
            final Growbar header = new Growbar();
            header.add(
                    new Growbar.Strut(20,0),
                    prevButton,
                    playButton,
                    nextButton,
                    new Growbar.Strut(20,0),
                    volume,
                    new Growbar.Strut(20,0),
                    statusPanel,
                    new Growbar.Strut(20,0),
                    listToggle,
                    gridToggle,
                    coverflowToggle,
                    new Growbar.Strut(20,0),
                    search,
                    new Growbar.Strut(20,0)
            );
            header.makeGrowable(statusPanel);
            header.setWidth(600);
            header.setHeight(80);
            
            final Label status = new Label("10 items, 40:07 total time, 46.1 MB");
            final Panel footer = new Growbar();
            footer.add(
                    new Growbar.Strut(20,0),
                    new Button("+").setVariant("footerControl"),
                    new Button("s").setVariant("footerControl"),
                    new Button("r").setVariant("footerControl"),
                    new Button("^").setVariant("footerControl"),
                    new Growbar.Spacer(),
                    status,
                    new Growbar.Spacer(),
                    new Button("L").setVariant("footerControl"),
                    new Button("*").setVariant("footerControl"),
                    new Button("<").setVariant("footerControl"),
                    new Growbar.Strut(20,0)
            );

            footer.setTranslateY(400);
            footer.setHeight(40);
            footer.setWidth(600);

            final ListView sourceList = new ListView();
            sourceList.setModel(new SourceListModel());
            sourceList.setRenderer(new SourceListRenderer());
            
            final ScrollPane sourcePane = new ScrollPane();
            sourcePane.setWidth(200);
            sourcePane.setContent(sourceList);
            sourcePane.setHorizontalScrollVisible(false);

            final TableView playList = new TableView();
            playList.setModel(new PlaylistModel());
            playList.setDefaultColumnWidth(150);
            playList.setRenderer(new PlaylistRenderer());
            final ScrollPane playPane = new ScrollPane();
            playPane.setContent(playList);

            Panel top = new Panel() {
                @Override
                public void doLayout() {
                    //position children, then do their layouts
                    header.setWidth(this.getWidth());
                    footer.setWidth(this.getWidth());
                    footer.setTranslateY(this.getHeight() - footer.getHeight());
                    
                    sourcePane.setTranslateY(header.getHeight());
                    sourcePane.setHeight(this.getHeight() - footer.getHeight() - header.getHeight());
                    sourcePane.setTranslateX(0);

                    playPane.setTranslateX(sourcePane.getWidth());
                    playPane.setTranslateY(header.getHeight());
                    playPane.setHeight(this.getHeight() - footer.getHeight() - header.getHeight());
                    playPane.setWidth(this.getWidth() - sourcePane.getWidth());
                    super.doLayout();
                }
            };
            top.setFill(FlatColor.BLUE);

            top.add(header, sourcePane, playPane, footer);

            stage.setContent(top);
            stage.setWidth(800);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

}
