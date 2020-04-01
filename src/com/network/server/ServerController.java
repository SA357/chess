package com.network.server;


import com.network.Transport;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.InetSocketAddress;
import java.sql.SQLException;

import static com.network.message.Message.NewServerAddressMessage;

public class ServerController {

    @FXML private TextArea console;
    @FXML private TextArea echo;
    @FXML private TextField newServerPort;
    @FXML private TextField newServerAddress;
    private Transport transport = new Transport();
    private DB db = new DB();
    private static ServerController instance;//например для того чтобы вывести соообщение в чатек

    public static ServerController getInstance() {
        return instance;
    }

    public static void setInstance(ServerController instance) {
        ServerController.instance = instance;
    }

    public void log(String line) {
        Platform.runLater(() -> console.appendText(line + "\n"));
    }

    public void logEcho(String line) {
        Platform.runLater(() -> echo.appendText(line + "\n"));
    }

    public void newServerAddress() {
        String address = newServerAddress.getText();
        String port = newServerPort.getText();
        Platform.runLater(() -> {
                    try {
                        InetSocketAddress a = new InetSocketAddress(address, Integer.parseInt(port));
                        NewServerAddressMessage message = new NewServerAddressMessage(a);
                        Server.shutdown();
                        Server server = new Server(a);
                        Thread.sleep(100);
                        Server.reload();
                        new Thread(server).start();
                        for (InetSocketAddress addr : Server.getDb().getAllActiveClientsServerParts()) {
                            transport.sendMessage_CRYPTED(message, addr, db.getPassword(db.getName(addr)));
                        }
                        log("Сервер поменял адрес на " + a);
                    } catch (SQLException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    newServerAddress.setText("");
                    newServerPort.setText("");
                }
        );
    }
}
