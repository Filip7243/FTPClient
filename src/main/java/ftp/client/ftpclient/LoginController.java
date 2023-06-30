package ftp.client.ftpclient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class LoginController {

    @FXML
    private TextField hostNameInput;

    @FXML
    private TextField usernameInput;

    @FXML
    private PasswordField passwordInput;

    @FXML
    private Button connectBtn;

    @FXML
    private Label errorLabel;


    public void initialize() {
        errorLabel.setVisible(false);
        connectBtn.setOnAction(this::onConnectBtnClick);
    }

    private void onConnectBtnClick(ActionEvent event) {
        String host = hostNameInput.getText();
        String username = usernameInput.getText();
        String password = passwordInput.getText();

        try {
            Socket socket = new Socket(host, 21);
            Client client = new Client(socket, username);
            client.login(username, password);
            client.listenForLoginResponse();

            // waiting for server response
            while (client.isLoggedIn() == null) {
                Thread.sleep(100);
            }

            // when response received
            if (client.isLoggedIn()) {
                LoggedInClient.setClient(client);

                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root, 600, 400));
                stage.show();
                ((Node) (event.getSource())).getScene().getWindow().hide();
            } else {
                showErrorLabel("Invalid Credentials!");
            }

        } catch (IOException e) {
            // TODO: jakiś regex do adresu IP
            String[] msg = e.getMessage().split(":");
            showErrorLabel(msg[0]);
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void showErrorLabel(String text) {
        errorLabel.setText(text);
        if (!errorLabel.isVisible()) {
            errorLabel.setVisible(true);
        }
    }

}
