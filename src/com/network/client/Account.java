package network.client;

public class Account {

    static private String name;
    static private String password;
    static private Boolean isAdmin;
    static private int clientServerPartPort;

    public static int getClientServerPartPort() {
        return clientServerPartPort;
    }

    public static void setClientServerPartPort(int clientServerPartPort) {
        Account.clientServerPartPort = clientServerPartPort;
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        Account.name = name;
    }

    public static boolean isAdmin() {
        return isAdmin;
    }

    public static void setAdmin(Boolean isAdmin) {
        Account.isAdmin = isAdmin;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        Account.password=password;
    }
}
