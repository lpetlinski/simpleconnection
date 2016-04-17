package lpetlinski.simpleconnection;

import lpetlinski.simpleconnection.protocol.Protocol;

public class ClientConfig {
    private String address;
    private int port;
    private boolean synchronous;
    private Protocol protocol;

    public ClientConfig() {
        this.address = "127.0.0.1";
        this.port = 4444;
        this.synchronous = false;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSynchronous() {
        return synchronous;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
}
