package network.server;

import network.Transport;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private static InetSocketAddress serverAddress;
    private static boolean quit = false;
    private static DB db = new DB();
    private static ExecutorService pool = Executors.newCachedThreadPool();
    private static Transport transport = new Transport();

    public static InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    Server(InetSocketAddress serverAddress123) {
        serverAddress = serverAddress123;
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
            db.writeAllTables();
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
                if (msg.getCode() == CryptedMessageCode) {
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
                    case kastomQueryMessageCode:
                        handle((KastomQueryMessage) msg);
                        break;
                    case userZakaziQueryMessageCode:
                        handle((UserZakaziQueryMessage) msg);
                        break;
                    case newZakazQueryMessageCode:
                        handle((NewZakazQueryMessage) msg);
                        break;
                    default:
                        break;
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
    }
}