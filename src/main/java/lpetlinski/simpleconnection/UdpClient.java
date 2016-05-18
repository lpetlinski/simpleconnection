package lpetlinski.simpleconnection;

import lpetlinski.simpleconnection.events.UdpMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

/**
 * Wrapper for udp client.
 */
public class UdpClient extends BaseUdpConnection {

    private static final Logger logger = LogManager.getLogger(UdpClient.class);

    private UdpClientConfig config;

    /**
     * Creates udp client.
     * @param config Configuration of client.
     */
    public UdpClient(UdpClientConfig config) {
        this.config = config;
        if(config == null) {
            this.config = new UdpClientConfig();
        }
    }

    /**
     * Starts client based on configuration.
     * @throws IOException
     */
    public void startClient() throws IOException {
        synchronized (getRunLock()) {
            if(this.isStarted()) {
                throw new IllegalStateException("Client already started");
            }
            this.setStarted(true);
        }
        this.setSocket(new DatagramSocket());
        if(this.config.isSynchronous()) {
            logger.debug("Creating client synchronously");
            startSynchronously();
        } else {
            logger.debug("Creating client asynchronously.");
            startAsynchronously();
        }
    }

    /**
     * Stops client listening.
     * @throws InterruptedException
     */
    public void stopClient() throws InterruptedException {
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

    /**
     * Enumerates all interfaces and sends data to all network interfaces.
     * @param message Message to send. Note that address is not used here.
     * @throws SocketException
     */
    public void sendBroadcast(UdpMessage message) throws SocketException {
        try {
            this.getSocket().setBroadcast(true);
            String msgData = getConfig().getProtocol().toString(message);
            byte[] msgBytes = msgData.getBytes();

            try {
                DatagramPacket sendPacket = new DatagramPacket(msgBytes, msgBytes.length, InetAddress.getByName("255.255.255.255"), message.getPort());
                logger.trace("Trying to send package to 255.255.255.255:" + message.getPort());
                this.getSocket().send(sendPacket);
                logger.trace("Sent package to 255.255.255.255");
            } catch (Exception e) {
            }
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; // Don't want to broadcast to the loopback interface
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(msgBytes, msgBytes.length, broadcast, message.getPort());
                        logger.trace("Trying to send package to " + broadcast + ":" + message.getPort());
                        this.getSocket().send(sendPacket);
                    } catch (Exception e) {
                    }
                }
            }
        } finally {
            this.getSocket().setBroadcast(false);
        }
    }
}
