package com.network.client.applicationGUI;

import com.chess.BlackWidow;
import com.network.Transport;
import com.network.client.Account;
import com.network.client.ClientServerPart;
import com.network.client.GameUtils;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.network.client.applicationGUI.Music.*;
import static com.network.message.Message.*;

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
    @FXML private TextField enemyNameTextField;
    @FXML private ChoiceBox<String> weaponChoiceBox;
    @FXML private ImageView weaponImage;
    @FXML private DatePicker datePicker;
    @FXML private TextField userName;
    @FXML private TextField words;

    @FXML private TableView<AdminTableItem> queryTable;
    @FXML private TableColumn<AdminTableItem, String> columnDate;
    @FXML private TableColumn<AdminTableItem, String> columnAuthor;
    @FXML private TableColumn<AdminTableItem, String> columnMsg;

    @FXML private TextField newName;
    @FXML private TextField newPort;
    @FXML private TextField newPassword;
    @FXML private TextField textField;
    @FXML private TextArea textArea;
    @FXML private TextArea activeUsers;
    @FXML private TextArea activeUsersCopy;
    @FXML private Tab adminTab;
    @FXML private TabPane tabPane;

    private static GUIController instance;
    private static Transport transport = new Transport();
    static private MediaPlayer mediaPlayer;

    @FXML private void initialize() {
        columnDate.setCellValueFactory(x -> x.getValue().dataProperty());
        columnAuthor.setCellValueFactory(x -> x.getValue().userNameProperty());
        columnMsg.setCellValueFactory(x -> x.getValue().wordsProperty());

        activeUsersCopy.textProperty().bind(activeUsers.textProperty());
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

    public void setAdmin() {
        Platform.runLater(() -> {
            adminTab.setDisable(false);
            mediaPlayer = turnMusic(admin);
            mediaPlayer.setVolume(0.2);
        });
    }

    public void setUser() {
        Platform.runLater(() -> tabPane.getTabs().remove(adminTab));
        mediaPlayer = turnMusic(standard);
        standart_Style();
        mute();////
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
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("ОЙ-ой-ОЙ");
                    alert.setHeaderText(null);
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                });
                e.printStackTrace();
            }
        }).start();
        Platform.runLater(() -> textField.setText(""));
    }

    @FXML private void sendInvitation() {
        String enemyName = enemyNameTextField.getText();
        if(!enemyName.equals(Account.getName())) {
            new Thread(() -> {
                try {
                    GameInvitationAnswer answer = (GameInvitationAnswer) transport.sendAndRecieve_CRYPTED(
                            new GameInvitationMessage(Account.getName(), enemyName),
                            ClientApp.getServerAddress(), Account.getPassword(), true
                    );
                    if (!answer.getEnemyExist()) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("ОЙ-ой-ОЙ");
                            alert.setHeaderText(null);
                            alert.setContentText("ТАКОГО ПОЛЬЗОВАТЕЛЯ НЕ СУЩЕСТВУЕТ, ЛИБО ОН НЕ В СЕТИ");
                            alert.showAndWait();
                        });
                        return;
                    }
                    if (!answer.getAnswer()) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("ОЙ-ой-ОЙ");
                            alert.setHeaderText(null);
                            alert.setContentText("ПОЛЬЗОВАТЕЛЬ ОТКЛОНИЛ ПРЕДЛОЖЕНИЕ");
                            alert.showAndWait();
                        });
                        return;
                    }
                    if (answer.getAnswer()) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setOnCloseRequest(e->Platform.exit());//////////////
                            alert.setTitle("готовьтесь к игре");
                            alert.setHeaderText(null);
                            alert.setContentText("ПОЛЬЗОВАТЕЛЬ ПРИНЯЛ ПРЕДЛОЖЕНИЕ");
                            alert.showAndWait();
                            try {
                                Thread.sleep(3500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            alert.close();
                        });
                        GameUtils.setIsPlayerTurn(true);
                        BlackWidow.main(new String[]{});
                        return;
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("ОЙ-ой-ОЙ");
                        alert.setHeaderText(null);
                        alert.setContentText(String.valueOf(e));
                        alert.showAndWait();
                    });
                    e.printStackTrace();
                }
            }).start();
        }
        else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("НУ ДАЁТ");
                alert.setHeaderText(null);
                alert.setContentText("С САМИМ СОБОЙ ИГРАТЬ НЕЛЬЗЯ!!!");
                alert.showAndWait();
            });
        }
    }

    @FXML private void changeSettings() {
        String password2 = newPassword.getText().equals("") ? Account.getPassword() : newPassword.getText();
        String name2 = newName.getText().equals("") ? Account.getName() : newName.getText();
        int port2 = newPort.getText().equals("") ? Account.getClientServerPartPort() : Integer.parseInt(newPort.getText());
        new Thread(() -> {
            try {
                SettingReplyMessage check = (SettingReplyMessage) transport.sendAndRecieve_CRYPTED(new SettingMessage(Account.getName(),
                        name2, password2, new InetSocketAddress("localhost", port2)), ClientApp.getServerAddress(), Account.getPassword());
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
        System.out.println("СЧИТАЛ ЗАПРОС");
        try {
            Date date2 = (date1 != null) ? Date.valueOf(date1) : null;
            AdminQueryReplyMessage replyMessage = (AdminQueryReplyMessage) transport
                    .sendAndRecieve_CRYPTED(
                            new AdminQueryMessage(Account.getName(), name1, words1, date2), ClientApp.getServerAddress(), Account.getPassword());
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

    //final boolean[] answer = new boolean[1];
    public boolean showInvitation(GameInvitationMessage msg) {
        //TODO
        Lock lock = new ReentrantLock();
        lock.lock();
        Condition condition = lock.newCondition();
        AtomicBoolean answer = new AtomicBoolean(false);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("ПРИШЛАШЕНИЕ");
            alert.setHeaderText(null);
            alert.setContentText(msg.getEnemyName() + " приглашает вас сыграть партеечку");
            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.YES){
                answer.set(true);
            }
            condition.signal();
        });
        try {
            condition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lock.unlock();
        return answer.get();
    }
//    public class MyAlert extends Application implements Runnable{
//        Message msg;
//
//        public MyAlert(Message msg) {
//            this.msg = msg;
//        }
//
//        public void show(){
//            Stage stage = new Stage();
//            stage.setTitle("ПРИШЛАШЕНИЕ");
//            stage.setResizable(false);
//            stage.initModality(Modality.APPLICATION_MODAL);
//            stage.setOnCloseRequest(event->{
//                answer[0] = false;
//                Platform.exit();
//            });
//
//            Button yes = new Button("yes");
//            yes.setOnAction(e -> {
//                answer[0] = true;
//                Platform.exit();
//            });
//
//            Button no = new Button("no");
//            yes.setOnAction(e -> {
//                answer[0] = false;
//                Platform.exit();
//            });
//
//            Label label = new Label(msg.getName() + " приглашает вас сыграть партеечку");
//            VBox vBox = new VBox(6);
//            vBox.getChildren().add(label);
//            HBox hBox = new HBox(12);
//            hBox.getChildren().addAll(no, yes);
//            vBox.getChildren().add(hBox);
//            //vBox.setAlignment(Pos.CENTER);
//            Scene scene = new Scene(vBox);
//            stage.setScene(scene);
//            stage.showAndWait();
//        }
//
//        @Override
//        public void run() {
//
//        }
//
//        @Override
//        public void start(Stage primaryStage) throws Exception {
//            show();
//        }
//    }

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