package com.network.client.applicationGUI;

import com.network.Transport;
import com.network.client.Account;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import static com.network.message.Message.*;


public class RegistrationController {
    private static Transport transport = new Transport();
    private static RegistrationController instance;
    @FXML private PasswordField passwordField;
    @FXML private TextField registrationPassword;
    @FXML private TextField nameField;
    @FXML private TextField registrationName;

    public static RegistrationController getInstance() {
        return instance;
    }

    public static void setInstance(RegistrationController instance) {
        RegistrationController.instance = instance;
    }

    @FXML
    private void OKEnter() {
        String password = passwordField.getText();
        String name = nameField.getText();
        new Thread(() -> {
            try {
                GreetingReplyMessage check = (GreetingReplyMessage) transport.sendAndReceive_NOT_CRYPTED(
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
                    } else {
                        Platform.runLater(() -> {
                            Registration.getStage().close(); //hide();
                            GUIController.getInstance().setUser();
                            GUI.getStage().show();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void OKReg() {
        String password = registrationPassword.getText();
        String name = registrationName.getText();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        new Thread(() -> {
            try {
                RegistrationReplyMessage check = (RegistrationReplyMessage) transport.sendAndReceive_NOT_CRYPTED(
                        new RegistrationMessage(name, password), ClientApp.getServerAddress());
                if (check.isRegistrated()) {
                    Platform.runLater(() -> {
                        alert.setTitle("ПОЗРАВЛЯЕМ");
                        alert.setHeaderText(null);
                        alert.setContentText("ВЫ ЗАРЕГИСТРИРОВАНЫ");
                        alert.showAndWait();
                        nameField.setText(name);
                        passwordField.setText(password);
                        registrationName.setText("");
                        registrationPassword.setText("");
                    });
                } else {
                    Platform.runLater(() -> {
                        alert.setTitle("ОЙ-ой-ОЙ");
                        alert.setHeaderText(null);
                        alert.setContentText("скорее всего имя уже занято");
                        alert.showAndWait();
                        registrationName.setText("");
                        registrationPassword.setText("");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}