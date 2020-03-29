package com.applicationGUI;

import com.network.message.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import network.Transport;



public class RegistrationController {
    @FXML private PasswordField passwordField;
    @FXML private TextField regestrationPassword;
    @FXML private TextField nameField;
    @FXML private TextField regestrationName;
    private static Transport transport=new Transport();
    private static RegistrationController instance;

    public static RegistrationController getInstance() {
        return instance;
    }

    public static void setInstance(RegistrationController instance) {
        RegistrationController.instance = instance;
    }

    @FXML private void OKEnter() {
       String password=passwordField.getText();
       String name=nameField.getText();
        new Thread( () -> {
            try {
                Message.GreetingReplyMessage check = (Message.GreetingReplyMessage) transport.sendAndRecieve_NOT_CRYPTED(
                        new GreetingMessage(name, Account.getClientServerPartPort(), password), ClientApp.getServerAddress());
                if (check.isVerified()) {
                     Account.setName(name);
                     Account.setPassword(password);
                     Account.setAdmin(check.isAdmin());
                    if (check.isAdmin()) {
                        Platform.runLater(() -> {
                            Registration.getStage().close();
                            GUIController.getInstance().setAdmin();
                            GUI.getStage().show();
                        });
                    }
                    else {
                        Platform.runLater(() -> {
                            Registration.getStage().close(); //hide();
                            GUIController.getInstance().setUser();
                            GUI.getStage().show();
                            GUIController.getInstance().reload();//////////////////////
                        });
                    }
                } else {
                    Platform.runLater(() -> {
                        passwordField.setText("");
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("ОЙ-ой-ОЙ");
                        alert.setHeaderText(null);
                        alert.setContentText("ПОЛЬЗОВАТЕЛЬ ОТСТУТСТВУЕТ");
                        alert.showAndWait();
                    });
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    @FXML private void OKReg() {
        String password=regestrationPassword.getText();
        String name=regestrationName.getText();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        new Thread( () -> {
            try {
                RegistrationReplyMessage check = (RegistrationReplyMessage) transport.sendAndRecieve_NOT_CRYPTED(
                        new RegistrationMessage(name, password), ClientApp.getServerAddress());
                if (check.isRegistrated()) {
                    Platform.runLater(() -> {
                        alert.setTitle("ПОЗРАВЛЯЕМ");
                        alert.setHeaderText(null);
                        alert.setContentText("ВЫ ЦАРЕГЕЦТРИРОВАНЫ");
                        alert.showAndWait();
                        nameField.setText(name);
                        passwordField.setText(password);
                        regestrationName.setText("");
                        regestrationPassword.setText("");
                    });
                }
                else {
                    Platform.runLater(() -> {
                        alert.setTitle("ОЙ-ой-ОЙ");
                        alert.setHeaderText(null);
                        alert.setContentText("скорее всего имя уже занято");
                        alert.showAndWait();
                        regestrationName.setText("");
                        regestrationPassword.setText("");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}