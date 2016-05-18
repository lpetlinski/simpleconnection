package lpetlinski.simpleconnection;

import lpetlinski.simpleconnection.protocol.Protocol;

/**
 * Tcp server configuration class.
 */
public class ServerConfig {
    private int port;
    private boolean synchronous;
    private Protocol protocol;

    /**
     * Creates default configuration. Which is:
     * Port 4444.
     * Asynchronous.
     */
    public ServerConfig() {
        this.port = 4444;
        this.synchronous = false;
    }

    /**
     * Returns server port on which is listening.
     * @return Server port on which is listening.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets server port to listen on.
     * @param port Server port to listen on.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns true if server is synchronous.
     * @return True if server is synchronous.
     */
    public boolean isSynchronous() {
        return synchronous;
    }

    /**
     * Sets whether server is synchronous.
     * @param synchronous True is server should be synchronous.
     */
    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    /**
     * Returns protocol used for reading/writing.
     * @return Protocol used for reading/writing.
     */
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * Sets protocol to be used in reading/writing.
     * @param protocol Protocol to be user for reading/writing.
     */
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
}
