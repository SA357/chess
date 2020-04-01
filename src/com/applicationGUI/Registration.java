package com.applicationGUI;

import com.network.client.ClientServerPart;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;


public class Registration {

    private static Stage stage;

    public static Stage getStage() {
        return stage;
    }

    public void init() throws IOException {
        stage = new Stage();
        stage.setTitle("ВХОД");
        stage.setWidth(850);
        stage.setHeight(650);
        InputStream iconStream = getClass().getClassLoader().getResourceAsStream("icon.png");
        assert iconStream != null;
        Image image = new Image(iconStream);
        stage.getIcons().add(image);
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("RegestrationScene.fxml"));
        TabPane root = loader.load();
        RegistrationController.setInstance(loader.getController());
        Scene scene = new Scene(root, 300, 200);
        stage.setOnCloseRequest(event-> {
            ClientServerPart.shutdown();
            Platform.exit();
        });
        //scene.getStylesheets().add("main.css");
        stage.setScene(scene);
    }
}