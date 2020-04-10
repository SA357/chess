package com.network.server;

import com.network.Transport;
import com.network.message.Message;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.network.message.Message.*;
import static com.network.message.MessageNames.*;

public class Server implements Runnable {

    private static InetSocketAddress serverAddress;
    private static boolean quit = false;
    private static DB db = new DB();
    private static ExecutorService pool = Executors.newCachedThreadPool();
    private static Transport transport = new Transport();

    Server(InetSocketAddress serverAddress123) {
        serverAddress = serverAddress123;
    }

    public static InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    public static DB getDb() {
        return db;
    }

    public static void reload() {
        quit = false;
    }

    static void shutdown() {
        quit = true;
        try {
            //db.writeAllTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
        EchoServerSender.shutdown();
    }

    public void run() {
        EchoServerSender.setDb(db);
        new Thread(new EchoServerSender()).start();
        try (ServerSocket ss = new ServerSocket()) {
            ss.setSoTimeout(1000);
            ss.bind(serverAddress);
            while (!quit) {
                try {
                    Socket clientSocket = ss.accept();
                    clientSocket.setSoTimeout(1000);
                    pool.submit(new Handler(clientSocket));
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


    static class Handler implements Runnable {

        private Socket socket;

        Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (Socket sock = socket) {
                InetSocketAddress clientAppAddress = new InetSocketAddress(socket.getInetAddress(), socket.getPort()); //(InetSocketAddress) socket.getRemoteSocketAddress() или так
                Message msg = (Message) new ObjectInputStream(new BufferedInputStream(socket.getInputStream())).readObject();
                if (msg.getCode() == cryptedMessageCode) {
                    msg = CryptedMessage.decrypt((CryptedMessage) msg, db.getPassword(msg.getName()));
                }
                switch (msg.getCode()) {
                    case greetingMessageCode:
                        handle((GreetingMessage) msg, clientAppAddress);
                        break;
                    case textMessageCode:
                        handle((TextMessage) msg);
                        break;
                    case registrationMessageCode:
                        handle((RegistrationMessage) msg);
                        break;
                    case settingMessageCode:
                        handle((SettingMessage) msg);
                        break;
                    case deleteMeMessageCode:
                        handle((DeleteMeMessage) msg);
                        break;
                    case adminQueryMessageCode:
                        handle((AdminQueryMessage) msg);
                        break;
                    case gameInvitationMessageCode:
                        handle((GameInvitationMessage) msg);
                        break;
                    case moveMessageCode:
                        handle((MoveMessage) msg);
                        break;
                    default: break;
                }
            } catch (EOFException e) {
                System.out.println("Клиент ушёл \n" + e.getMessage());
                ServerController.getInstance().log("Клиент ушёл \n" + e.getMessage());
            } catch (SQLException t) {
                for (Throwable e : t) {
                    e.printStackTrace();
                    ServerController.getInstance().log(e.getMessage());
                }
            } catch (Exception e) {
                ServerController.getInstance().log(e.getMessage());
                e.printStackTrace();
            }
        }

        private void handle(RegistrationMessage msg) throws Exception {
            if (db.checkClientNameExistence(msg.getName())) {
                transport.sendMessage_NOT_CRYPTED(new RegistrationReplyMessage(false), socket);
            } else {
                db.addClient(msg.getName(), msg.getPassword(), Date.valueOf(LocalDate.now()), false);
                System.out.println(msg.getName() + " зарегестрировался");
                ServerController.getInstance().log(msg.getName() + " зарегестрировался");
                transport.sendMessage_NOT_CRYPTED(new RegistrationReplyMessage(true), socket);
            }
        }

        private void handle(GreetingMessage msg, InetSocketAddress clientAddress) throws Exception { //clientAddress нужен для получения inetAddressа
            System.out.println(msg.getName() + " пытается зайти");
            ServerController.getInstance().log(msg.getName() + " пытается зайти");
            boolean isVerified = db.isVerified(msg.getName(), msg.getPassword());
            boolean isAdmin = db.isAdmin(msg.getName(), msg.getPassword());
            transport.sendMessage_NOT_CRYPTED(new GreetingReplyMessage(isAdmin, isVerified), socket);
            if (isVerified) {
                System.out.println(msg.getName() + " зашёл");
                ServerController.getInstance().log(msg.getName() + " зашёл");

                db.addActiveClient(msg.getName(), clientAddress.getAddress().toString().substring(1), msg.getClientServerPartPort(), Date.valueOf(LocalDate.now()));
                InetSocketAddress currentClientServerPartAddress = new InetSocketAddress(clientAddress.getAddress().toString().substring(1), msg.getClientServerPartPort());
                //добавляем уже активных пользователей в нашу колонку справа
                for (InetSocketAddress ActiveClientsServerPart : db.getAllActiveClientsServerParts()) {
                    if (!currentClientServerPartAddress.equals(ActiveClientsServerPart)) {
                        transport.sendMessage_CRYPTED(new AddedClientMessage(db.getName(ActiveClientsServerPart)),
                                currentClientServerPartAddress, db.getPassword(db.getName(currentClientServerPartAddress)));
                    }
                }
                //оповещаем других активных пользователей , о новом активном пользователе (обновляем ихнюю колонку справа)
                for (InetSocketAddress addr : db.getAllActiveClientsServerParts()) {
                    transport.sendMessage_CRYPTED(new AddedClientMessage(msg.getName()), addr, db.getPassword(db.getName(addr)));
                }
            }
        }

        private void handle(TextMessage msg) throws Exception {
            if (db.checkClientNameExistence(msg.getName()) || msg.getName().equals("СЕРВЕР")) {   //проверяем есть ли в базе
                db.addLog(msg.getName(), Date.valueOf(LocalDate.now()), msg.getText());
                System.out.println("Сервер получил сообщение от " + msg.getName());
                ServerController.getInstance().log("Сервер получил сообщение от " + msg.getName());
                for (InetSocketAddress addr : db.getAllActiveClientsServerParts()) {
                    transport.sendMessage_CRYPTED(new TextMessage(msg.getName() + ": " + msg.getText(), null), addr, db.getPassword(db.getName(addr)));
                }
            }
        }

        private void handle(SettingMessage msg) throws Exception {
            if (db.checkClientNameExistence(msg.getName())) {   //проверяем есть ли в базе
                DeleteMeMessage deleteMeMessage = new DeleteMeMessage(db.getInetSocketAddress(msg.getName()), msg.getName());
                db.settingUpdate(msg);
                System.out.println(msg.getName() + " изменил имя на >>>" + msg.getNewName());
                ServerController.getInstance().log(msg.getName() + " изменил имя на >>>" + msg.getNewName());
                db.addActiveClient(msg.getNewName(), msg.getInetSocketAddress().getAddress().toString().split("/")[1],
                        msg.getInetSocketAddress().getPort(), Date.valueOf(LocalDate.now()));
                transport.sendMessage_CRYPTED(new SettingReplyMessage(true), socket, db.getPassword(msg.getName()));
                handle(deleteMeMessage);//
                for (InetSocketAddress addr : db.getAllActiveClientsServerParts()) {
                    transport.sendMessage_CRYPTED(new AddedClientMessage(msg.getNewName()), addr, db.getPassword(db.getName(addr)));
                }
                //
            }
        }

        private void handle(DeleteMeMessage msg) throws Exception {
            db.deleteActiveClient(msg.getName());
            List<InetSocketAddress> list = new ArrayList<>();
            try {
                list = Server.getDb().getAllActiveClientsServerParts();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            for (InetSocketAddress addr : list) {
                transport.sendMessage_CRYPTED(new DeleteClientMessage(msg.getName()), addr, db.getPassword(db.getName(addr)));
            }
            System.out.println(msg.getName() + " ушёл, и сюрвер его удалил " + msg.getInetSocketAddress());
            ServerController.getInstance().log(msg.getName() + " ушёл, и сюрвер его удалил " + msg.getInetSocketAddress());
            handle(new TextMessage(msg.getName() + " ушёл", "СЕРВЕР"));
        }

        private void handle(AdminQueryMessage msg) throws Exception {
            if (db.checkClientNameExistence(msg.getName())) {
                System.out.println("Исполняется запрос " + msg.getName());
                transport.sendMessage_CRYPTED(db.executeAdminQuery(msg), socket, db.getPassword(msg.getName()));
            }
        }

        private void handle(GameInvitationMessage msg) throws Exception {
            ServerController.getInstance().log(msg.getName() + " хочет еграц");
            if (db.checkClientNameExistence(msg.getName())) {
                if(db.checkClientActivness(msg.getEnemyName())) {
                    GameInvitationAnswer gameInvitationAnswer;
                    if(
                        (gameInvitationAnswer =
                                (GameInvitationAnswer) transport.sendAndRecieve_CRYPTED(
                                    msg, db.getInetSocketAddress(msg.getEnemyName()), db.getPassword(msg.getEnemyName()),true
                                )
                        ).getAnswer()
                    ) { //просим у enemy подтверждение
                        db.addActiveSession(msg.getName(), msg.getEnemyName(), Date.valueOf(LocalDate.now()));
                    }
                    transport.sendMessage_CRYPTED(gameInvitationAnswer, socket, db.getPassword(msg.getName())); //шлём игроку хочет ли enemy еграц
                }
                else {
                    transport.sendMessage_CRYPTED(new GameInvitationAnswer(), socket, db.getPassword(msg.getName())); //шлём игроку, что такого игрока не существует
                }
            }
        }

        private void handle(MoveMessage msg) throws Exception {
            String enemyName;
            if (
                    db.checkClientNameExistence(msg.getName()) && db.checkClientActivness( enemyName = db.getEnemyName(msg.getName()) )
            ) {
                System.out.println(msg.getName() + " сделал ход");
                transport.sendMessage_CRYPTED(msg, db.getInetSocketAddress(enemyName), db.getPassword(enemyName));
            }
        }
    }
}