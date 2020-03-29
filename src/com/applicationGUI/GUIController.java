package com.applicationGUI;

import com.network.Transport;
import com.network.client.Account;
import com.network.client.ClientServerPart;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaPlayer;

import java.net.InetSocketAddress;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;

import static com.applicationGUI.Music.*;
import static com.network.message.Message.*;

class ZakazTableItem {
    @FXML private SimpleStringProperty weapon;
    @FXML private SimpleStringProperty data;
    @FXML private SimpleStringProperty scope;
    @FXML private SimpleStringProperty podstvolnik;
    public ZakazTableItem(String weapon, String data, String scope, String podstvolnik) {
        this.weapon = new SimpleStringProperty(weapon);
        this.data = new SimpleStringProperty(data);
        this.scope = new SimpleStringProperty(scope);
        this.podstvolnik = new SimpleStringProperty(podstvolnik);
    }
    SimpleStringProperty weaponProperty(){return weapon;}
    SimpleStringProperty dataProperty(){return data;}
    SimpleStringProperty scopeProperty(){return scope;}
    SimpleStringProperty podstvolnikProperty(){return podstvolnik;}
}

class AdminTableItem {
    @FXML private SimpleStringProperty userName;
    @FXML private SimpleStringProperty data;
    @FXML private SimpleStringProperty words;
    AdminTableItem(String userName, String data, String words) {
        this.userName = new SimpleStringProperty(userName);
        this.data = new SimpleStringProperty(data);
        this.words = new SimpleStringProperty(words);
    }
    SimpleStringProperty userNameProperty(){return userName;}
    SimpleStringProperty dataProperty(){return data;}
    SimpleStringProperty wordsProperty(){return words;}
}

public class GUIController {
    @FXML private ChoiceBox<String> weaponChoiceBox;
    @FXML private ChoiceBox<String> scopeChoiceBox;
    @FXML private ChoiceBox<String> podstvolnikChoiceBox;
    @FXML private ImageView podstvolnikImage;
    @FXML private ImageView weaponImage;
    @FXML private ImageView scopeImage;
    @FXML private DatePicker datePicker;
    @FXML private TextField userName;
    @FXML private TextField words;

    @FXML private TableView<AdminTableItem> queryTable;
    @FXML private TableColumn<AdminTableItem, String> columnDate;
    @FXML private TableColumn<AdminTableItem, String> columnAuthor;
    @FXML private TableColumn<AdminTableItem, String> columnMsg;

    @FXML private TableView<ZakazTableItem> zakaziTable;
    @FXML private TableColumn<ZakazTableItem, String> zakazDataColumn;
    @FXML private TableColumn<ZakazTableItem, String> zakazWeaponColumn;
    @FXML private TableColumn<ZakazTableItem, String> zakazScopeColumn;
    @FXML private TableColumn<ZakazTableItem, String> zakazPodstvolnikColumn;

    @FXML private TextField newName;
    @FXML private TextField newPort;
    @FXML private TextField newPassword;
    @FXML private TextField textField;
    @FXML private TextArea textArea;
    @FXML private TextArea activeUsers;
    @FXML private Tab adminTab;
    @FXML private Tab zakazi;
    @FXML private TabPane tabPane;

    private static GUIController instance;
    private static Transport transport = new Transport();
    static private MediaPlayer mediaPlayer;

    @FXML private void initialize() {

        columnDate.setCellValueFactory(x -> x.getValue().dataProperty());
        columnAuthor.setCellValueFactory(x -> x.getValue().userNameProperty());
        columnMsg.setCellValueFactory(x -> x.getValue().wordsProperty());

        zakazDataColumn.setCellValueFactory(x -> x.getValue().dataProperty());
        zakazPodstvolnikColumn.setCellValueFactory(x -> x.getValue().podstvolnikProperty());
        zakazScopeColumn.setCellValueFactory(x -> x.getValue().scopeProperty());
        zakazWeaponColumn.setCellValueFactory(x -> x.getValue().weaponProperty());
    }

    @FXML private void AL_SHAB_Style() {
        mediaPlayer.stop();
        mediaPlayer = turnMusic(nashid);
        GUI.getStage().getScene().getStylesheets().clear();
        GUI.getStage().getScene().getStylesheets().add("AL_SHAB_Style.css");
    }

    @FXML private void standart_Style() {
        mediaPlayer.stop();
        mediaPlayer = turnMusic(standard);
        GUI.getStage().getScene().getStylesheets().clear();
        GUI.getStage().getScene().getStylesheets().add("standart_Style.css");
    }

    @FXML private void DNR_Style() {
        mediaPlayer.stop();
        mediaPlayer = turnMusic(DNRMusic);
        GUI.getStage().getScene().getStylesheets().clear();
        GUI.getStage().getScene().getStylesheets().add("DNR_Style.css");
    }

    @FXML private void mute() {
        mediaPlayer.stop();
    }

    @FXML void reload() {
        try {
            reloadZakazi();
            reloadKastom();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void newZakaz() throws Exception {

    }

    @FXML private void reloadZakazi() throws Exception {

    }

    @FXML private void reloadKastom() {

    }

    public void setAdmin() {
        Platform.runLater(() -> {
            adminTab.setDisable(false);
            zakazi.setDisable(true);
            mediaPlayer = turnMusic(admin);
            mediaPlayer.setVolume(0.2);
        });
    }

    public void setUser() {
        Platform.runLater(() -> tabPane.getTabs().remove(adminTab));
        mediaPlayer = turnMusic(standard);
        standart_Style();
    }

    public static GUIController getInstance() {
        return instance;
    }

    public static void setInstance(GUIController instance) {
        GUIController.instance = instance;
    }

    @FXML private void sendMessage() {
        String text = textField.getText();
        new Thread(() -> {
            try {
                transport.sendMessage_CRYPTED(new TextMessage(text, Account.getName()),
                        ClientApp.getServerAddress(), Account.getPassword());
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("ОЙ-ой-ОЙ");
                    alert.setHeaderText(null);
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                    e.printStackTrace();
                });
            }
        }).start();
        Platform.runLater(() -> textField.setText(""));
    }

    @FXML private void changeSettings() {
        String password2 = newPassword.getText().equals("") ? Account.getPassword() : newPassword.getText();
        String name2 = newName.getText().equals("") ? Account.getName() : newName.getText();
        int port2 = newPort.getText().equals("") ? Account.getClientServerPartPort() : Integer.parseInt(newPort.getText());
        new Thread(() -> {
            try {
                SettingReplyMessage check = (SettingReplyMessage) transport.sendAndRecieve_CRYPTED(new SettingMessage(Account.getName(),
                        name2, password2, new InetSocketAddress("localhost", port2)), Account.getPassword(), ClientApp.getServerAddress());
                if (check.isChanged()) {
                    Account.setName(name2);
                    Account.setClientServerPartPort(port2);
                    Account.setPassword(password2);
                    ClientServerPart.shutdown();
                    ClientServerPart clientServerPart = new ClientServerPart(port2);
                    Thread.sleep(100);
                    ClientServerPart.reload();
                    new Thread(clientServerPart).start();
                } else {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("ОЙ-ой-ОЙ");
                        alert.setHeaderText(null);
                        alert.setContentText("Не получелося, воть так воть(((");
                        alert.showAndWait();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                newPassword.setText("");
                newName.setText("");
                newPort.setText("");
            });
        }).start();
    }

    @FXML private void adminQuery() {
        LocalDate date1 = datePicker.getValue();
        String name1 = userName.getText();
        String words1 = words.getText();
        System.out.println("СЧЕТАЛ ЗАПРОЦС");
        try {
            Date date2 = (date1 != null) ? Date.valueOf(date1) : null;
            AdminQueryReplyMessage replyMessage = (AdminQueryReplyMessage) transport
                    .sendAndRecieve_CRYPTED(
                            new AdminQueryMessage(Account.getName(), name1, words1, date2), Account.getPassword(), ClientApp.getServerAddress());
//            System.out.println(replyMessage.getList());
            Platform.runLater(() -> {
                ObservableList<AdminTableItem> list = FXCollections.observableArrayList();
                for (AdminQueryReplyMessage.Entry entry : replyMessage.getList()) {
                    list.add(new AdminTableItem(entry.getNameOfUser(), entry.getDate(), entry.getWords()));
                    queryTable.setItems(list);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMessage(String line) {
        Platform.runLater(() -> textArea.appendText(line + "\n"));
    }

    public void addActiveClient(String name) {
        Platform.runLater(() -> activeUsers.appendText(name + "\n"));
    }

    public void deleteActiveClient(String name) {
        String line = activeUsers.getText();
        Platform.runLater(() -> {
            activeUsers.setText("");
            Arrays.stream(line.split("[\n]")).filter(x -> !x.equals(name)).forEach(x -> activeUsers.appendText(x + "\n"));
        });
    }
}