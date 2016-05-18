package lpetlinski.simpleconnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Udp server wrapper.
 */
public class UdpServer extends BaseUdpConnection {

    private static final Logger logger = LogManager.getLogger(UdpServer.class);

    private UdpServerConfig config;

    /**
     * Creates upd server with given configuration.
     * @param config Configuration of server.
     */
    public UdpServer(UdpServerConfig config) {
        this.config = config;
        if(config == null) {
            this.config = new UdpServerConfig();
        }
    }

    /**
     * Starts server.
     * @throws IOException
     */
    public void startServer() throws IOException {
        synchronized (this.getRunLock()) {
            if (this.isStarted()) {
                throw new IllegalStateException("Server already started");
            }
            this.setStarted(true);
        }
        if(config.isBroadcast()) {
            this.setSocket(new DatagramSocket(config.getPort(), InetAddress.getByName("0.0.0.0")));
            this.getSocket().setBroadcast(true);
        } else {
            this.setSocket(new DatagramSocket(config.getPort(), InetAddress.getByName(config.getAddress())));
            this.getSocket().setBroadcast(false);
        }
        if (this.config.isSynchronous()) {
            logger.debug("Creating synchronous server.");
            startSynchronously();
        } else {
            logger.debug("Creating asynchronous server");
            startAsynchronously();
        }
    }

    /**
     * Stops server.
     * @throws InterruptedException
     */
    public void stopServer() throws InterruptedException {
        stop();
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected BaseUdpConfig getConfig() {
        return config;
    }
}
