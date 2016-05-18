package lpetlinski.simpleconnection;

import lpetlinski.simpleconnection.events.Event;
import lpetlinski.simpleconnection.events.EventWithMessage;
import lpetlinski.simpleconnection.events.Message;
import lpetlinski.simpleconnection.events.StringMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Tcp client wrapper on socket.
 */
public class Client {

    private static final Logger logger = LogManager.getLogger(Client.class);

    private Thread subThread;
    private PrintWriter writer;
    private RequestHandler reader;
    private Socket clientSocket;
    private ClientConfig config;
    private boolean started;
    private final Object runLock = new Object();

    private EventWithMessage<Message> receiver;
    private Event connectionClosed;
    private Event connectionOpened;

    /**
     * Creates new instance.
     * @param config Configuration of this client.
     */
    public Client(ClientConfig config) {
        this.config = config;
        this.started = false;
        if(config == null) {
            this.config = new ClientConfig();
        }
    }

    /**
     * Starts the client.
     * @throws IOException
     */
    public void startClient() throws IOException {
        synchronized (runLock) {
            if(this.started) {
                throw new IllegalStateException("Client already started");
            }
            this.started = true;
        }
        if(this.config.isSynchronous()) {
            logger.debug("Creating client synchronously");
            startSynchronously();
        } else {
            logger.debug("Creating client asynchronously.");
            this.subThread = new Thread(new InnerClient());
            this.subThread.start();
        }
    }

    private void startSynchronously() throws IOException {
        this.clientSocket = new Socket(this.config.getAddress(), this.config.getPort());
        this.writer = new PrintWriter(this.clientSocket.getOutputStream());
        this.invokeOnConnectionOpened();
        createReader();
    }

    private void createReader() throws IOException {
        this.reader = new RequestHandler(this.clientSocket.getInputStream());
        this.reader.onReadFromServer(new EventWithMessage<StringMessage>() {
            @Override
            public void onEventOccurred(StringMessage msg) {
                logger.debug("Got message from server: " + msg.getData());
                Message message = Client.this.config.getProtocol().toMessage(msg.getData());
                while (message != null) {
                    Client.this.invokeOnReceive(message);
                    message = Client.this.config.getProtocol().toMessage("");
                }
            }
        });
        this.reader.onReaderClosed(new Event() {
            @Override
            public void onEventOccurred() {
                logger.debug("Reader closed.");
                Client.this.invokeOnConnectionClosed();
                if (Client.this.isStarted()) {
                    Client.this.stopClient();
                }
            }
        });
        this.reader.startReading();
    }

    /**
     * Stops the client socket and sub thread if running asynchronously.
     */
    public void stopClient() {
        synchronized (runLock) {
            if(!this.started) {
                throw new IllegalStateException("Client already stopped");
            }
            this.started = false;
        }
        logger.debug("Stopping client.");
        try {
            if(!this.clientSocket.isClosed()) {
                this.clientSocket.close();
            }
            if(this.subThread != null && this.subThread.isAlive()) {
                this.subThread.join();
            }
        } catch (Exception e) {
            logger.trace("Exception occurred while stopping client.", e);
        }
    }

    private class InnerClient implements Runnable {

        @Override
        public void run() {
            try {
                clientSocket = new Socket(config.getAddress(), config.getPort());
                invokeOnConnectionOpened();
                writer = new PrintWriter(clientSocket.getOutputStream());
                createReader();
            } catch (IOException e) {
                logger.warn("Exception occurred while connecting", e);
            }
        }
    }

    /**
     * Sends the message.
     * @param message
     */
    public void send(Message message) {
        if(this.writer != null) {
            this.writer.print(config.getProtocol().toString(message));
            this.writer.flush();
        }
    }

    private void invokeOnReceive(Message msg) {
        if(this.receiver != null) {
            this.receiver.onEventOccurred(msg);
        }
    }

    /**
     * Setter for handler when received message.
     * @param receiver Handler to be called when message is received.
     */
    public void onReceive(EventWithMessage<Message> receiver) {
        this.receiver = receiver;
    }

    private void invokeOnConnectionClosed() {
        if(this.connectionClosed != null) {
            this.connectionClosed.onEventOccurred();
        }
    }

    /**
     * Setter for handler when connection is closed.
     * @param event Handler to be called when connection is closed.
     */
    public void onConnectionClosed(Event event) {
        this.connectionClosed = event;
    }

    private void invokeOnConnectionOpened() {
        if(this.connectionOpened != null) {
            this.connectionOpened.onEventOccurred();
        }
    }

    /**
     * Setter for handler when connection is opened.
     * @param event Handler to be called when connection is opened.
     */
    public void onConnectionOpened(Event event) {
        this.connectionOpened = event;
    }

    public boolean isStarted() {
        synchronized (runLock) {
            return this.started;
        }
    }
}
