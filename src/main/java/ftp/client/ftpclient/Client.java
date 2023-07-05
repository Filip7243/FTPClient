package ftp.client.ftpclient;

import javafx.scene.control.Alert;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client {

    private Socket controlSocket;
    private Socket dataSocket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private volatile Boolean isLoggedIn = null;
    private String username;
    private ArrayList<UploadedFile> clientFiles = new ArrayList<>();

    public Client(Socket controlSocket, Socket dataSocket, String username) {
        try {
            this.controlSocket = controlSocket;
            this.dataSocket = dataSocket;
            this.username = username;

            this.dataInputStream = new DataInputStream(dataSocket.getInputStream());
            this.dataOutputStream = new DataOutputStream(dataSocket.getOutputStream());

            this.bufferedReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(controlSocket.getOutputStream()));
        } catch (IOException e) {
            disconnect(controlSocket, dataSocket, dataInputStream, dataOutputStream, bufferedReader, bufferedWriter);
        }
    }

    // USER command
    public void login(String username, String password) {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.write(password);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            disconnect(controlSocket, dataSocket, dataInputStream, dataOutputStream, bufferedReader, bufferedWriter);
        }
    }

    public void sendListCommand() {
        try {
            bufferedWriter.write("LIST");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendUploadCommand() {
        try {
            bufferedWriter.write("STOR");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendDownloadCommand(String fileName) {
        try {
            bufferedWriter.write("RETR");
            bufferedWriter.newLine();
            bufferedWriter.write(fileName);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendDeleteCommand(String fileName) {
        try {
            bufferedWriter.write("DELE");
            bufferedWriter.newLine();
            bufferedWriter.write(fileName);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void listenForResponse() {
        new Thread(() -> {
            String responseCode;

            while (controlSocket.isConnected()) {
                try {
                    responseCode = bufferedReader.readLine();
                    System.out.println("RESPONSE CODE: " + responseCode);

                    if (responseCode.contains("530")) {
                        isLoggedIn = false;
                        throw new IOException(responseCode);
                    }

                    if (responseCode.contains("230")) {  // successfully authorization
                        isLoggedIn = true;
                    }

                    if (isLoggedIn != null && isLoggedIn) {
                        if (responseCode.contains("226")) {  // success response from server
                            if (responseCode.contains("File On Server")) {  // file uploading
                                System.out.println("File Uploaded!");
                            }

                            if (responseCode.contains("File Deleted")) {
                                Alert a = new Alert(Alert.AlertType.INFORMATION);
                                a.setTitle("Delete success");
                                a.setContentText("File deleted!");
                                a.show();
                            }

                            if (responseCode.contains("Transfer Completed")) {  // file download
                                int fileNameLength = dataInputStream.readInt();

                                if (fileNameLength > 0) {
                                    byte[] fileNameBytes = new byte[fileNameLength];
                                    dataInputStream.readFully(fileNameBytes, 0, fileNameLength);
                                    String fileName = new String(fileNameBytes);
                                    String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

                                    int fileContentLength = dataInputStream.readInt();

                                    if (fileContentLength > 0) {
                                        byte[] fileContentBytes = new byte[fileContentLength];
                                        dataInputStream.readFully(fileContentBytes, 0, fileContentLength);

                                        UploadedFile uploadedFile = new UploadedFile(clientFiles.size(), fileName, extension, (double) fileContentLength / 1024, "");
                                        clientFiles.add(uploadedFile);

                                        File fileToSave = new File(fileName);
                                        FileOutputStream fileOutputStream = new FileOutputStream(fileToSave);
                                        fileOutputStream.write(fileContentBytes);
                                        fileOutputStream.close();
                                    }
                                }
                            }

                            if (responseCode.contains("Files Sent")) {
                                int numberOfFiles = dataInputStream.readInt();

                                for (int i = 0; i < numberOfFiles; i++) {
                                    int fileNameLength = dataInputStream.readInt();

                                    if (fileNameLength > 0) {
                                        byte[] fileNameBytes = new byte[fileNameLength];
                                        dataInputStream.readFully(fileNameBytes, 0, fileNameLength);  // we read whole file
                                        String fileName = new String(fileNameBytes);
                                        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

                                        int fileContentLength = dataInputStream.readInt();

                                        if (fileContentLength > 0) {
                                            byte[] fileContentBytes = new byte[fileContentLength];
                                            dataInputStream.readFully(fileContentBytes, 0, fileContentLength);


                                            UploadedFile uploadedFile = new UploadedFile(i, fileName, extension, (double) fileContentLength / 1024, "");
                                            clientFiles.add(uploadedFile);
                                        }
                                    }
                                }
                            }
                        }

                    }
                } catch (IOException e) {
                    System.out.println("ERR: " + e.getMessage());
                    break;
                }
            }
        }).start();
    }

    private void disconnect(Socket controlSocket, Socket dataSocket, DataInputStream dataInputStream, DataOutputStream dataOutputStream,
                            BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (controlSocket != null) controlSocket.close();
            if (dataSocket != null) dataSocket.close();
            if (dataInputStream != null) this.dataInputStream.close();
            if (dataOutputStream != null) this.dataOutputStream.close();
            if (bufferedReader != null) this.bufferedReader.close();
            if (bufferedWriter != null) this.bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean isLoggedIn() {
        return isLoggedIn;
    }

    public String getUsername() {
        return username;
    }

    public Socket getControlSocket() {
        return controlSocket;
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public ArrayList<UploadedFile> getClientFiles() {
        return clientFiles;
    }

    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    public BufferedWriter getBufferedWriter() {
        return bufferedWriter;
    }


}

