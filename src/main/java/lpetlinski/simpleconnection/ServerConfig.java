package lpetlinski.simpleconnection;

import lpetlinski.simpleconnection.protocol.Protocol;

public class ServerConfig {
    private int port;
    private boolean synchronous;
    private Protocol protocol;

    public ServerConfig() {
        this.port = 4444;
        this.synchronous = false;
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
