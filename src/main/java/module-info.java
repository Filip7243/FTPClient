module ftp.client.ftpclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens ftp.client.ftpclient to javafx.fxml;
    exports ftp.client.ftpclient;
}