package ftp.client.ftpclient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    @FXML
    private TableView<UploadedFile> filesTable;

    @FXML
    private TableColumn<UploadedFile, String> nameCol;

    @FXML
    private TableColumn<UploadedFile, String> extCol;

    @FXML
    private TableColumn<UploadedFile, Double> sizeCol;

    @FXML
    private TableColumn<UploadedFile, String> dateCol;

    @FXML
    private Button uploadBtn;

    @FXML
    private Button downloadBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Label welcomeLabel;

    private static final List<String> availableExtensions = List.of(".jpg", ".png", ".jpeg", ".txt", ".pdf", ".iso");

    private Client client = LoggedInClient.getClient();

    private ObservableList<UploadedFile> obs = FXCollections.observableArrayList();

    public void initialize() {
        welcomeLabel.setText("WELCOME " + client.getUsername().toUpperCase() + " TO YOUR ACCOUNT!");

        uploadBtn.setOnAction(this::onUploadClick);
        downloadBtn.setOnAction(this::onDownloadClick);
        deleteBtn.setOnAction(this::onDeleteClick);


        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        extCol.setCellValueFactory(new PropertyValueFactory<>("extension"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        obs.setAll(client.getClientFiles());
        filesTable.setItems(obs);
    }

    private void onUploadClick(ActionEvent event) {
        Scene scene = ((Node) (event.getSource())).getScene();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chose Files To Upload");
        File fileToUpload = fileChooser.showOpenDialog(scene.getWindow());

        if (fileToUpload != null) {
            uploadFile(fileToUpload);
        } else {
            System.out.println("Something went wrong!");
        }

    }

    private void uploadFile(File fileToUpload) {
        String fileName = fileToUpload.getName();  // this is the name with extension
        String extension = fileName.substring(fileName.lastIndexOf("."));

        if (availableExtensions.contains(extension)) {
            try {
                client.sendUploadCommand();
                FileInputStream fileInputStream = new FileInputStream(fileToUpload.getAbsolutePath());
                DataOutputStream dataOutputStream = client.getDataOutputStream();

                byte[] fileNameBytes = fileName.getBytes();
                byte[] fileContent = new byte[(int) fileToUpload.length()];

                fileInputStream.read(fileContent);  // now we have our file in this stream
                fileInputStream.close();

                dataOutputStream.writeInt(fileNameBytes.length);  // we are telling server size of sending data
                dataOutputStream.write(fileNameBytes);

                dataOutputStream.writeInt(fileContent.length);
                dataOutputStream.write(fileContent);

                ArrayList<UploadedFile> clientFiles = client.getClientFiles();
                clientFiles.add(new UploadedFile(clientFiles.size(), fileName, extension,
                        (double) fileToUpload.length() / 1024,
                        Files.readAttributes(Path.of(fileToUpload.getPath()), BasicFileAttributes.class).lastModifiedTime().toString()));

                obs.setAll(clientFiles);
                filesTable.setItems(obs);

                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Upload success");
                a.setContentText("File uploaded!");
                a.show();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Can't upload file");
            a.setContentText("You can't upload this file!");
            a.show();
        }
    }

    private void onDownloadClick(ActionEvent event) {
        UploadedFile selectedItem = filesTable.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            client.sendDownloadCommand(selectedItem.getName());
            System.out.println(selectedItem.getName());
        }
    }

    private void onDeleteClick(ActionEvent event) {
        UploadedFile selectedItem = filesTable.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            client.sendDeleteCommand(selectedItem.getName());
            obs.remove(selectedItem);
            filesTable.refresh();
            System.out.println("Selectd item" + selectedItem.getName());
        }
    }
}
