package com.network.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

public class ZapuscatorServer extends Application {

    private static InetSocketAddress serverAddress = new InetSocketAddress("localhost" ,10000);

    public static void main(String[] args){
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        //Files.deleteIfExists((Paths.get("KR.DB.db")));                ///*****удолятор бд
        if (!Files.exists((Paths.get("KR.DB.db"))))create();
        Server server = new Server(serverAddress);
        new Thread(server).start();
        stage.setTitle("SERVER");
        stage.setTitle("SERVER");
        stage.setWidth(900);
        stage.setHeight(600);

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ServerScene.fxml"));
        AnchorPane root = loader.load();
        ServerController.setInstance(loader.getController());
        Scene scene = new Scene(root, 900, 500);
        stage.setOnCloseRequest(event-> {
            Server.shutdown();
            try {
                DB db = new DB();
                db.deleteAllActiveClients();
                db.closeAllActiveSession();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("Сервер пошол спать");
            Platform.exit();
        });
        ServerController.getInstance().log("ПРИ ВЫКЛЮЧЕНИИ ПРИЛОЖЕНИЯ  СЕРВЕР ОТКЛЮЧИТСЯ !");
        ServerController.getInstance().logEcho("здесь показываются ECHO-сообщения");
        stage.setScene(scene);
        stage.show();
    }

    private void create(){
        try {
            new DB().create();
        }
        catch (SQLException t){
            for (Throwable e:t)
                e.printStackTrace();
        }
    }
}