package lpetlinski.simpleconnection;

import lpetlinski.simpleconnection.protocol.UdpProtocol;

/**
 * Base configuration class for udp connections.
 */
abstract class BaseUdpConfig {

    private boolean synchronous;
    private UdpProtocol protocol;

    /**
     * True if reading should be run synchronously.
     * @return True if reading should be run synchronously.
     */
    public boolean isSynchronous() {
        return synchronous;
    }

    /**
     * Sets whether reading should be run synchronously.
     * @param synchronous True if reading should be run synchronously.
     */
    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    /**
     * Returns protocol to be used for reading/writing messages.
     * @return Protocol to be used for reading/writing messages.
     */
    public UdpProtocol getProtocol() {
        return protocol;
    }

    /**
     * Sets protocol to be used for reading/writing messages.
     * @param protocol Protocol to be used for reading/writing messages.
     */
    public void setProtocol(UdpProtocol protocol) {
        this.protocol = protocol;
    }
}
