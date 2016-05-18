package lpetlinski.simpleconnection;

import lpetlinski.simpleconnection.events.EventWithMessage;
import lpetlinski.simpleconnection.events.UdpMessage;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Base class for udp connection.
 */
abstract class BaseUdpConnection {

    private boolean started;
    private Object runLock = new Object();
    private DatagramSocket socket;
    private Thread subThread;

    private EventWithMessage<UdpMessage> receiver;

    /**
     * Default constructor.
     */
    BaseUdpConnection() {
        this.started = false;
    }

    /**
     * Stops the socket and sub thread (if exists).
     * @throws InterruptedException
     */
    protected void stop() throws InterruptedException {
        synchronized (this.runLock) {
            if (!this.started) {
                throw new IllegalStateException("Server not started");
            }
            this.started = false;
        }
        getLogger().debug("Stopping server.");
        this.socket.close();
        if (this.subThread != null) {
            this.subThread.join();
        }
    }

    /**
     * Starts reading from socket synchronously.
     */
    protected void startSynchronously() {
        while(started) {
            try {
                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);
                String data = new String(packet.getData()).trim();
                getLogger().debug("Received data: " + data);
                UdpMessage msg = getConfig().getProtocol().toMessage(data);
                if(msg != null) {
                    msg.setAdress(packet.getAddress().getHostAddress());
                    msg.setPort(packet.getPort());
                    invokeMessageReceived(msg);
                }
            } catch (IOException e) {
                getLogger().debug("Exception occurred while reading data", e);
            }
        }
    }

    /**
     * Starts reading from socket asynchronously.
     */
    protected void startAsynchronously() {
        this.subThread = new Thread(new InnerClient());
        this.subThread.start();
    }

    /**
     * Inner runnable class for thread.
     */
    private class InnerClient implements Runnable {

        @Override
        public void run() {
            BaseUdpConnection.this.startSynchronously();
        }
    }

    abstract protected Logger getLogger();
    abstract protected BaseUdpConfig getConfig();

    /**
     * True if is reading from socket.
     * @return True if reading from socket.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Sets whether started reading from server.
     * @param started True is started.
     */
    protected void setStarted(boolean started) {
        this.started = started;
    }

    /**
     * Returns underlying socket.
     * @return Socket.
     */
    protected DatagramSocket getSocket() {
        return socket;
    }

    /**
     * Returns run lock.
     * @return Run lock.
     */
    protected Object getRunLock() {
        return runLock;
    }

    /**
     * Sets socket.
     * @param socket Socket to set.
     */
    protected void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    /**
     * Sets handler for receiving data.
     * @param receiver Handler to be run when data is received.
     */
    public void onMessageReceived(EventWithMessage<UdpMessage> receiver) {
        this.receiver = receiver;
    }

    private void invokeMessageReceived(UdpMessage message) {
        if(receiver != null) {
            this.receiver.onEventOccurred(message);
        }
    }

    /**
     * Sends message with specified data.
     * @param message Message with address and port to send.
     * @throws IOException
     */
    public void send(UdpMessage message) throws IOException {
        String msgData = getConfig().getProtocol().toString(message);
        byte[] msgBytes = msgData.getBytes();

        DatagramPacket packet = new DatagramPacket(msgBytes, msgBytes.length, InetAddress.getByName(message.getAdress()), message.getPort());
        socket.send(packet);
    }
}
