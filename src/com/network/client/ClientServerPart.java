package com.network.client;

import com.chess.BlackWidow;
import com.chess.gui.Table;
import com.network.client.applicationGUI.ClientApp;
import com.network.client.applicationGUI.GUIController;
import com.network.Transport;
import com.network.message.Message;
import com.network.message.Message.*;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.network.message.MessageNames.*;


public class ClientServerPart implements Runnable {

    static private Transport transport = new Transport();
    static private boolean quit = false;
    private int clientServerPartPort;

    public ClientServerPart(int clientServerPartPort) {
        this.clientServerPartPort = clientServerPartPort;
    }

    public static void shutdown() {
        quit = true;
    }

    public static void reload() {
        quit = false;
    }

    public void run() {
        ExecutorService pool = Executors.newCachedThreadPool();
        try (ServerSocket ss = new ServerSocket()) {
            ss.setSoTimeout(1000);
            ss.bind(new InetSocketAddress("localhost", clientServerPartPort));
            while (!quit) {
                try {
                    Socket request = ss.accept();
                    pool.submit(new ClientServerPart.Request(request));/// можно и new Request(request)
                } catch (SocketTimeoutException e) {
                    // ничего не делаем
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }

    static class Request implements Runnable {

        private Socket socket;

        Request(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (Socket sock = socket) {
                Message msg = (Message) new ObjectInputStream(new BufferedInputStream(socket.getInputStream())).readObject();
                if (msg.getCode()== cryptedMessageCode){
                    msg = CryptedMessage.decrypt( (CryptedMessage) msg, Account.getPassword() );
                }
                switch (msg.getCode()) {
                    case echoMessageCode:
                        transport.sendMessage_CRYPTED(new EchoMessage(), socket, Account.getPassword());
                        break;
                    case textMessageCode:
                        TextMessage textmsg = (TextMessage) msg;
                        GUIController.getInstance().showMessage(textmsg.getText());
                        break;
                    case addedClientMessageCode:
                        AddedClientMessage addedClientMessage = (AddedClientMessage) msg;
                        GUIController.getInstance().addActiveClient(addedClientMessage.getName());
                        break;
                    case deleteClientMessageCode:
                        DeleteClientMessage deleteClientMessage = (DeleteClientMessage) msg;
                        GUIController.getInstance().deleteActiveClient(deleteClientMessage.getName());
                        break;
                    case newServerAddressMessageCode:
                        NewServerAddressMessage newServerPortMessage = (NewServerAddressMessage) msg;
                        ClientApp.setServerAddress(newServerPortMessage.getInetSocketAddress());
                        break;
                    case gameInvitationMessageCode:
                        GameInvitationMessage gameInvitationMessage = (GameInvitationMessage) msg;
                        //если уже играет, то можно вернуть, что уже играет //
                        boolean answer = GUIController.getInstance().showInvitation(gameInvitationMessage);
                        transport.sendMessage_CRYPTED(new GameInvitationAnswer(Account.getName(), answer), socket, Account.getPassword());
                        if (answer){
                            Account.setEnemyName(msg.getName());
                            new Thread(()->BlackWidow.main(new String[]{})).start();
                        }
                        break;
                    case moveMessageCode:
                        MoveMessage moveMessage = (MoveMessage) msg;
                        Table.get().updateGameBoard((moveMessage.getBoard()));
                        Table.get().setMoveLog((moveMessage.getMoveLog()));
                        Table.get().redo();
                        GameUtils.setIsPlayerTurn(true);
                        break;
                    default: break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}