package ftp.client.ftpclient;

public final class LoggedInClient {

    private static Client client;

    public static void setClient(Client client) {
        LoggedInClient.client = client;
    }

    public static Client getClient() {
        return client;
    }
}
