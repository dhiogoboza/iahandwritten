package br.ufrn.ia.nnhandwritten.client;

/**
 * Created by dhiogoboza on 25/04/16.
 */
public class Config {

    private String username;
    private int serverPort = 9009;
    private String serverAddress;

    public Config() {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

}
