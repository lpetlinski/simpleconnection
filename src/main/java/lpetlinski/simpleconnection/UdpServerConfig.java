package lpetlinski.simpleconnection;

/**
 * Udp server configuration.
 */
public class UdpServerConfig extends BaseUdpConfig {

    private String address;
    private int port;
    private boolean broadcast;

    /**
     * Returns the address on which server is listening on.
     * @return Address on which server is listening on.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address on which server should listen on.
     * @param address Address to be set on which server should listen on.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns port on which server is listening on.
     * @return Port on which server is listening on.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port on which server should listen on.
     * @param port Port on which server should listen on.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns whether server is only listening for broadcast messages.
     * @return True if server is only listening for broadcast messages.
     */
    public boolean isBroadcast() {
        return broadcast;
    }

    /**
     * Sets whether server should only listen for broadcast messages.
     * @param broadcast True if server should listen for broadcast messages.
     */
    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }
}
