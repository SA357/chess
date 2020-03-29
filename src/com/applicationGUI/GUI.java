package com.applicationGUI;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

;

public class GUI {

    private static Stage stage;

    public static Stage getStage() {
        return stage;
    }

    public void init() throws IOException {
        stage = new Stage();
        stage.setTitle("PRO_LABS");     // установка заголовка
        stage.setWidth(900);            // установка ширины
        stage.setHeight(700);           // установка длины
        InputStream iconStream = getClass().getClassLoader().getResourceAsStream("icon.jpg");
        assert iconStream != null;
        Image image = new Image(iconStream);
        stage.getIcons().add(image);
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("GUIScene.fxml"));
        TabPane root = loader.load();
        applicationGUI.GUIController.setInstance(loader.getController());
        Scene scene = new Scene(root, 800, 550);
        stage.setOnCloseRequest(event-> {
            try {
                new Transport().sendMessage_CRYPTED(new DeleteMeMessage(
                        new InetSocketAddress("localhost", Account.getClientServerPartPort()), Account.getName()
                        ),
                        ClientApp.getServerAddress(),Account.getPassword());
            } catch (Exception e) {
                e.printStackTrace();
            }
            ClientServerPart.shutdown();
            Platform.exit();
        });
        stage.setScene(scene);
        stage.setResizable(false);
    }
}