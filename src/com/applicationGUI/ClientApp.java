package com.applicationGUI;

import javafx.application.Application;
import javafx.stage.Stage;
import com.network.client.Account;
import com.network.client.ClientServerPart;

import java.net.InetSocketAddress;
import java.util.Random;

public class ClientApp extends Application {

    private static InetSocketAddress serverAddress = new InetSocketAddress("localhost", 10000);

    public static InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    public static void setServerAddress(InetSocketAddress serverAddress) {
        ClientApp.serverAddress = serverAddress;
    }

    public static void main(String[] args) throws Exception {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Random random = new Random();
        Account.setClientServerPartPort(random.nextInt(10000) + 6000);
        Thread clientServerPart = new Thread(new ClientServerPart(Account.getClientServerPartPort()));
        clientServerPart.start();
        Registration registration = new Registration();
        registration.init();
        GUI gui = new GUI();
        gui.init();
        Registration.getStage().show();
    }
}
