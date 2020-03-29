package com.network.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

import static com.network.message.Message.*;

@SuppressWarnings("UnusedAssignment")
class DB {

    private Connection getConnection() throws SQLException {
        String connStr = "jdbc:sqlite:KR.DB.db";  //"jdbc:sqlite::memory:"
        return DriverManager.getConnection(connStr);
    }

    //UPDATE clients set name = ?, password = ? where name = ?
    void create() throws SQLException {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("create table Clients ( name text, password text, date timestamp, isAdmin boolean )");
            stmt.executeUpdate("create table ActiveClients ( name text, IP text, portServerPart int, date timestamp )");
            stmt.executeUpdate("create table LOG ( name text, date timestamp, text text )");
            stmt.executeUpdate("create table Zakazi ( name text, date timestamp, weapon text, scope text, podstvolnik text, serial SERIAL )");//

            executeLinesFromFile();

            addWeaponType("ak12", "5,45", "автомат");
            addWeaponType("ak103", "5,56", "автомат");
            addWeaponType("akm", "7,62", "автомат");
            addWeaponType("g36", "5,56", "автомат");
            addWeaponType("m4a1", "5,56", "автомат");
            addWeaponType("scarh", "7,62", "автомат");
            addWeapon("ak12", "2014");
            addWeapon("ak103", "2009");
            addWeapon("akm", "1960");
            addWeapon("g36", "2005");
            addWeapon("m4a1", "1995");
            addWeapon("scarh", "1996");

            addScopeType("acog");
            addScopeType("eotech");
            addScopeType("kobra");
            addScope("acog", "2015");
            addScope("eotech", "2016");
            addScope("kobra", "2017");

            addGrenadeLauncherCaliber("gp25", "40");
            addGrenadeLauncherCaliber("m203", "40");
            addGrenadeLauncher("gp25", "1988");
            addGrenadeLauncher("m203", "1977");

            List<String> uniScopesWeapons = Arrays.asList("ak12", "ak103", "g36", "m4a1", "scarh");//"akm,"
            List<String> rusUniGrenadWeapons = Arrays.asList("ak12", "ak103");
            List<String> scopes = Arrays.asList("acog", "eotech", "kobra");
            for (String gun : uniScopesWeapons) {
                for (String sc : scopes)
                    addCompliance_of_the_weapons_and_scopes(gun, sc);
            }
            for (String gun : rusUniGrenadWeapons) {
                addWeapons_and_grenade_launcher_compliance(gun, "gp25");
            }
            addWeapons_and_grenade_launcher_compliance("m4a1", "m203");

            addClient("ADMIN", "111", Date.valueOf(LocalDate.now()), true);
            addClient("Лариса Ивановна", "111", Date.valueOf(LocalDate.now()), false);
            addClient("Штеренберг", "111", Date.valueOf(LocalDate.now()), false);
            addClient("Бумбершнюк", "111", Date.valueOf(LocalDate.now()), false);
            addClient("Бусыгин Константин Николаевич", "111", Date.valueOf(LocalDate.now()), true);
            addClient("q", "q", Date.valueOf(LocalDate.now()), false);
        }
    }

    public void addCompliance_of_the_weapons_and_scopes(String weapon, String scope) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("insert into Compliance_of_the_weapons_and_scopes values(?, ?, null)");
            stmt.setString(1, weapon);
            stmt.setString(2, scope);
            stmt.execute();
        }
    }

    public void addWeapons_and_grenade_launcher_compliance(String weapon, String grenade_launcher) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("insert into Weapons_and_grenade_launcher_compliance values(?, ?, null)");///&&&
            stmt.setString(1, weapon);
            stmt.setString(2, grenade_launcher);
            stmt.execute();
        }
    }

    public void settingUpdate(SettingMessage msg) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt1 = conn.prepareStatement("UPDATE clients set name = ?, password = ? where name = ?");
            stmt1.setString(1, msg.getNewName());
            stmt1.setString(2, msg.getPassword());
            stmt1.setString(3, msg.getName());
            stmt1.execute();
//            PreparedStatement stmt2 = conn.prepareStatement("insert into activeClients values(?, ?, ?, ?)");
//            stmt2.setString(1, msg.getNewName());
//            stmt2.setString(2, msg.getInetSocketAddress().getAddress().toString().split("/")[1]);//////вот ето да
//            stmt2.setInt(3, msg.getInetSocketAddress().getPort());
//            stmt2.setDate(4, Date.valueOf(LocalDate.now()));
//            stmt2.execute();
            ServerController.getInstance().log("изменил имя на >>>" + msg.getNewName());
            addLog(msg.getName(), Date.valueOf(LocalDate.now()), "изменил имя на >>>" + msg.getNewName());
        }
    }

    public void addWeapon(String name, String date) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("insert into List_of_weapons values(?, ?, ?)");
            stmt.setString(1, name);
            stmt.setString(2, date);
            stmt.setString(3, String.valueOf(new Random().nextInt()));
            stmt.execute();
        }
    }

    public void addWeaponType(String name, String caliber, String type) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("insert into List_of_weapons_types values(?, ?, ?)");
            stmt.setString(1, name);
            stmt.setString(2, caliber);
            stmt.setString(3, type);
            stmt.execute();
        }
    }

    public void addScopeType(String name) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("insert into List_of_types_of_scopes values(?)");
            stmt.setString(1, name);
            stmt.execute();
        }
    }

    public void addScope(String name, String date) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("insert into List_of_scopes values(?, ?, ?)");
            stmt.setString(1, name);
            stmt.setString(2, date);
            stmt.setString(3, String.valueOf(new Random().nextInt()));
            stmt.execute();
        }
    }

    public void addGrenadeLauncherCaliber(String name, String caliber) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("insert into List_of_calibers_of_grenade_launchers values(?, ?)");
            stmt.setString(1, name);
            stmt.setString(2, caliber);
            stmt.execute();
        }
    }

    public void addGrenadeLauncher(String name, String date) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("insert into List_of_copies_of_grenade_launchers values(?, ?, ?)");
            stmt.setString(1, name);
            stmt.setString(2, date);
            stmt.setString(3, String.valueOf(new Random().nextInt()));
            stmt.execute();
        }
    }

    public void addClient(String name, String password, Date date, boolean isAdmin) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("insert into Clients values(?, ?, ?, ?)");
            stmt.setString(1, name);
            stmt.setString(2, password);
            stmt.setDate(3, date);
            stmt.setBoolean(4, isAdmin);
            stmt.execute();
        }
    }

    public void addActiveClient(String name, String IP, int clientServerPartPort, Date date) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("insert into activeClients values(?, ?, ?, ?)");
            stmt.setString(1, name);
            stmt.setString(2, IP);
            stmt.setInt(3, clientServerPartPort);
            stmt.setDate(4, date);
            stmt.execute();
        }
    }

    public void addLog(String name, Date date, String text) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("insert into LOG values(?, ?, ?)");
            stmt.setString(1, name);
            stmt.setDate(2, date);
            stmt.setString(3, text);
            stmt.execute();
        }
    }

    public void newZakaz(NewZakazQueryMessage msg) throws SQLException {
        System.out.println("обрабатывается заказ " + msg.getName());
        ServerController.getInstance().log("обрабатывается заказ " + msg.getName());
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            //
            PreparedStatement weaponStmt = conn.prepareStatement("select Serial_number from List_of_weapons  where Name_of_the_weapon = ? limit 1");
            weaponStmt.setString(1, msg.getWeapon());
            ResultSet weaponRS = weaponStmt.executeQuery();

            try {
                weaponRS.getString(1);//если ничюво нету выдаст ошибку
            } catch (SQLException e) {
                System.out.println(msg.getWeapon() + " хотят заказать, а он отсутствует на складе");
                ServerController.getInstance().log(msg.getWeapon() + " хотят заказать, а он отсутствует на складе");
                throw e;
            }

            PreparedStatement weaponDelStmt = conn.prepareStatement("delete from List_of_weapons where Serial_number = ? ");
            weaponDelStmt.setString(1, weaponRS.getString(1));
            if (weaponDelStmt.executeUpdate() == 0) {
                conn.rollback();
                newZakaz(msg);
                throw new SQLException("кто-то забрал " + msg.getWeapon());
            }
            //
            if (!"".equals(msg.getScope()) && msg.getScope() != null) {
                PreparedStatement scopeStmt = conn.prepareStatement("select Serial_number from List_of_scopes where Name_of_the_scope = ? limit 1");
                scopeStmt.setString(1, msg.getScope());
                ResultSet scopeRS = scopeStmt.executeQuery();

                try {
                    scopeRS.getString(1);//если ничюво нету выдаст ошибку
                } catch (SQLException e) {
                    System.out.println(msg.getScope() + " хотят заказать, а он отсутствует на складе");
                    ServerController.getInstance().log(msg.getScope() + " хотят заказать, а он отсутствует на складе");
                    throw e;
                }

                PreparedStatement scopeDelStmt = conn.prepareStatement("delete from List_of_scopes  where Serial_number = ? ");
                scopeDelStmt.setString(1, scopeRS.getString(1));
                if (scopeDelStmt.executeUpdate() == 0) {
                    conn.rollback();
                    newZakaz(msg);
                    throw new SQLException("кто-то забрал " + scopeRS.getString(1));
                }
            }
            //
            if (!"".equals(msg.getPodstvolnik()) && msg.getPodstvolnik() != null) {
                PreparedStatement podstvStmt = conn.prepareStatement("select Serial_number from List_of_copies_of_grenade_launchers where Name_of_the_grenade_launcher = ? limit 1");
                podstvStmt.setString(1, msg.getPodstvolnik());
                ResultSet podstvRS = podstvStmt.executeQuery();

                try {
                    podstvRS.getString(1);//если товар отсутствует - выдаст ошибку
                } catch (SQLException e) {
                    System.out.println(msg.getPodstvolnik() + " хотят заказать, а он отсутствует на складе");
                    ServerController.getInstance().log(msg.getPodstvolnik() + " хотят заказать, а он отсутствует на складе");
                    throw e;
                }

                PreparedStatement podstvDelStmt = conn.prepareStatement("delete from List_of_copies_of_grenade_launchers where Serial_number = ? ");
                podstvDelStmt.setString(1, podstvRS.getString(1));
                if (podstvDelStmt.executeUpdate() == 0) {
                    conn.rollback();
                    newZakaz(msg);
                    throw new SQLException("кто-то забрал " + podstvRS.getString(1));
                }
            }
            //
            PreparedStatement zakaziStmt = conn.prepareStatement("insert into Zakazi values(?, ?, ?, ?, ?, null)");
            zakaziStmt.setString(1, msg.getName());
            zakaziStmt.setDate(2, msg.getDate());
            zakaziStmt.setString(3, msg.getWeapon());
            zakaziStmt.setString(4, msg.getScope());
            zakaziStmt.setString(5, msg.getPodstvolnik());
            zakaziStmt.execute();
            //
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    public UserZakaziQueryReplyMessage getZakazi(UserZakaziQueryMessage msg) throws SQLException {
        UserZakaziQueryReplyMessage replyMessage = new UserZakaziQueryReplyMessage();
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT weapon, date, scope, podstvolnik FROM Zakazi WHERE name = ?");
            stmt.setString(1, msg.getName());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                replyMessage.getSet()
                        .add(
                            new UserZakaziQueryReplyMessage.Zakaz(rs.getDate(2).toLocalDate().toString(),
                            rs.getString(1), rs.getString(3), rs.getString(4))
                        );
            }
        }
        return replyMessage;
    }

    public AdminQueryReplyMessage executeAdminQuery(AdminQueryMessage msg) throws SQLException { //работает только для одной строки LOG
        try (Connection conn = getConnection()) {
//            String needwords = "text like '%" + String.join("%' and text like %'", msg.getWords().split(",")) + "%'";
//            System.out.println(needwords);
            String query = "select name, date, text from LOG where 1";
            if (!msg.getNameOfUser().equals("")) {
                query += " and name = ?";
            }
            if (!msg.getWords().equals("")) {
                query += " and text like ?";
            }
            if (msg.getDate() != null) {
                query += " and date >= ?";
            }
            System.out.println(query);
            PreparedStatement stmt = conn.prepareStatement(query);
            int cnt = 1;
            if (!msg.getNameOfUser().equals("")) {
                stmt.setString(cnt++, msg.getNameOfUser());
            }
            if (!msg.getWords().equals("")) {
                stmt.setString(cnt++, "%" + msg.getWords() + "%");
            }
            if (msg.getDate() != null) {
                stmt.setDate(cnt++, msg.getDate());
            }
            ResultSet rs = stmt.executeQuery();
            AdminQueryReplyMessage replyMessage = new AdminQueryReplyMessage();
            while (rs.next())
                replyMessage.getList().add(
                        new AdminQueryReplyMessage.Entry(
                                rs.getString(1), rs.getString(3), rs.getDate(2).toLocalDate().toString()
                        )
                );
            return replyMessage;
//            if(!msg.getNameOfUser().equals("")) {
//                PreparedStatement stmt = conn.prepareStatement("select name, date, text from LOG where name = ? and date >= ? and "+needwords);
////                PreparedStatement stmt = conn.prepareStatement("select name, date, text from LOG where " + needwords);
//                stmt.setString(1, msg.getNameOfUser());
//                stmt.setDate(2, msg.getDate());
//                stmt.execute();
//                ResultSet rs = stmt.getResultSet();
//                return new QueryReaplyMessage(rs.getString(1), rs.getString(3), rs.getDate(2));
//            }
//            else {
//                PreparedStatement stmt = conn.prepareStatement("select name, date, text from LOG where date >= ? and "+needwords);
//                stmt.setDate(1, msg.getDate());
//                stmt.execute();
        }
    }

    boolean isAdmin(String name, String password) throws SQLException {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT EXISTS(SELECT password FROM Clients WHERE password = '"
                    + password + "' and name = '" + name + "' and isAdmin =" + true + ")");
            //rs.next(); //????
            return rs.getBoolean(1);
        }
    }

    boolean isVerified(String name, String password) throws SQLException {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT EXISTS(SELECT password FROM Clients WHERE password = '" + password + "' and name = '" + name + "')");
            return rs.getBoolean(1);
        }
    }

    public String getPassword(String name) throws SQLException {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT password FROM Clients WHERE name = '" + name+"'");
            return rs.getString(1);   //
        }
    }

    String getName(InetSocketAddress inetAddress) throws SQLException {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM activeClients WHERE IP = '" + inetAddress.getAddress().toString().substring(1)
                    + "' and portServerPart = "+ inetAddress.getPort());
            return rs.getString(1);   //1 or
        }
    }

    InetSocketAddress getInetSocketAddress(String name) throws SQLException {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT IP, portServerPart FROM activeClients WHERE name = '" + name + "'");
            return new InetSocketAddress(rs.getString(1), rs.getInt(2));   //
        }
    }

    boolean checkClientNameExistence(String name) throws SQLException {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT EXISTS(SELECT name FROM Clients WHERE name = '" + name + "')");
            return rs.getBoolean(1);
        }
    }

    List<InetSocketAddress> getAllActiveClientsServerParts() throws SQLException {
        List<InetSocketAddress> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT IP, portServerPart from activeClients");
            while (rs.next()) {
                list.add(new InetSocketAddress(rs.getString(1), rs.getInt(2)));
            }
        }
        return list;
    }

    void deleteAllActiveClients() throws SQLException {
        for (InetSocketAddress a : getAllActiveClientsServerParts()) {
            deleteActiveClient(a);
        }
    }

    void deleteActiveClient(InetSocketAddress inetAddress) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.createStatement().execute("DELETE FROM activeClients WHERE IP = '" + inetAddress.getAddress().toString().substring(1)
                    + "' and portServerPart =" + inetAddress.getPort());
        }
    }

    void deleteActiveClient(String name) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.createStatement().execute("DELETE FROM activeClients WHERE name = '" + name + "'");
        }
    }

    void executeLinesFromFile() throws SQLException { // сканнер накапливает строку до ( ; )    и пропускать коменты ( - )
        try (Connection conn = getConnection()) {
            Scanner scan = new Scanner(Paths.get("sett.txt"));//
            StringBuilder stringBuilder = new StringBuilder();
            while (scan.hasNextLine()) {
                String line;
                do {
                    line = scan.nextLine();                                                      //обязателен ли трим //// здеся он всё портит, не в коем цлучое низя
                    if (!line.contains("--")) stringBuilder.append(line);
                }
                while (!line.contains(";"));
                line = stringBuilder.toString();
                stringBuilder = new StringBuilder();
                System.out.println(">" + line);
                if (line.endsWith(";")) {
                    line = line.substring(0, line.length() - 1);
                }

                Statement stmt = conn.createStatement();
                if (stmt.execute(line)) {
                    ResultSet rs = stmt.getResultSet();
                    try {
                        showResultSet(rs, System.out);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                stmt.closeOnCompletion();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showResultSet(ResultSet rs, OutputStream os) throws SQLException {
        PrintWriter pw = new PrintWriter(os, true);
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {                               /// в резалтСетах нумерация начинаеца с 1
            if (i > 1) {
                pw.print(", ");
            }
            pw.print(metaData.getColumnName(i));
        }
        pw.println();
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1) {
                    pw.print(", ");
                }
                pw.print(rs.getString(i));
            }
            pw.println();
        }
        pw.println();
    }

    void writeAllTables() throws Exception {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            for (String table : tableNames()) {
                ResultSet rs = stmt.executeQuery("select * from " + table);
                try (FileOutputStream fo = new FileOutputStream("All_Tables.txt", true)) {
                    PrintWriter pw = new PrintWriter(fo, true);//
                    pw.println("таблица: " + table);
                    showResultSet(rs, fo);
                }
            }
        }
    }

    private Set<String> tableNames() throws Exception {
        Set<String> tableNames = new HashSet<>();
        try (Connection conn = getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(null, null, "", null);
            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME"));//(3) cтолбец (узнали из документациии)
            }
        }
        return tableNames;
    }
}
