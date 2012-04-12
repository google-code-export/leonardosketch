/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assetmanager;

import javafx.application.Application;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 *
 * @author josh
 */
public class AssetManager extends Application {
    public static void main(String[] args) {
        Application.launch(AssetManager.class, args);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("AssetManager.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add("assetmanager/custom.css");

        stage.setScene(scene);
        stage.show();
    }

    public void start(JFXPanel panel) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("AssetManager.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add("assetmanager/custom.css");
        panel.setScene(scene);
        //stage.show();
    }
}
