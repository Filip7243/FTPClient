package ftp.client.ftpclient;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client {

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private volatile Boolean isLoggedIn = null;
    private String username;
    private ArrayList<UploadedFile> clientFiles = new ArrayList<>();

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.username = username;

            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            disconnect(socket, dataInputStream, dataOutputStream, bufferedReader, bufferedWriter);
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
            disconnect(socket, dataInputStream, dataOutputStream, bufferedReader, bufferedWriter);
        }
    }

    public void listenForLoginResponse() {
        new Thread(() -> {
            String loginResponse;

            while (isLoggedIn == null) {
                try {
                    loginResponse = bufferedReader.readLine();

                    if (loginResponse == null) {
                        isLoggedIn = false;
                        throw new IOException("Invalid Credentials!");
                    }

                    // TODO: liste plikow pobrac z serwera jeszcze

                    if (loginResponse.equals("230")) {
                        isLoggedIn = true;
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    disconnect(socket, dataInputStream, dataOutputStream, bufferedReader, bufferedWriter);
                    break;
                }
            }
        }).start();
    }

    private void disconnect(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream,
                            BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (socket != null) socket.close();
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

    public Socket getSocket() {
        return socket;
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
}

