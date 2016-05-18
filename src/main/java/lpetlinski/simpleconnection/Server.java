package lpetlinski.simpleconnection;

import lpetlinski.simpleconnection.events.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Tcp server wrapper.
 */
public class Server {

    private static final Logger logger = LogManager.getLogger(Server.class);

    private Thread subThread;
    private ServerSocket serverSocket;
    private PrintWriter writer;
    private RequestHandler reader;
    private Socket clientSocket;
    private boolean started;
    private final Object runLock = new Object();
    private ServerConfig config;

    private EventWithMessage<Message> receiver;
    private Event clientDisconnected;
    private Event clientConnected;

    /**
     * Constructor.
     * @param config Configuration of this server.
     */
    public Server(ServerConfig config) {
        this.config = config;
        if(config == null) {
            this.config = new ServerConfig();
        }
        this.started = false;
    }

    /**
     * Starts server.
     * @throws IOException
     */
    public void startServer() throws IOException {
        synchronized (this.runLock) {
            if (this.started) {
                throw new IllegalStateException("Server already started");
            }
            this.started = true;
        }
        this.serverSocket = new ServerSocket(this.config.getPort());
        if (this.config.isSynchronous()) {
            logger.debug("Creating synchronous server.");
            runServerSynchronously();
        } else {
            logger.debug("Creating asynchronous server");
            this.subThread = new Thread(new InnerServer());
            this.subThread.start();
        }
    }

    private void runServerSynchronously() {
        while (this.started) {
            try {
                this.writer = null;
                this.reader = null;
                this.clientSocket = serverSocket.accept();
                logger.debug("Client connected.");
                this.writer = new PrintWriter(this.clientSocket.getOutputStream());
                invokeOnClientConnected();
                createReader();
                this.reader.startReading();
            } catch (IOException e) {
                // do nothing
                logger.trace("Exception occured while reading from client", e);
            }
            invokeOnClientDisconnected();
            logger.debug("Client disconnected.");
        }
    }

    /**
     * Stops server sockets and sub thread if running asynchronously.
     * @throws IOException
     * @throws InterruptedException
     */
    public void stopServer() throws IOException, InterruptedException {
        synchronized (this.runLock) {
            if (!this.started) {
                throw new IllegalStateException("Server not started");
            }
            this.started = false;
        }
        logger.debug("Stopping server.");
        this.serverSocket.close();
        if (this.clientSocket != null && !clientSocket.isClosed()) {
            this.clientSocket.close();
        }
        if (this.subThread != null) {
            this.subThread.join();
        }
    }

    private void createReader() throws IOException {
        this.reader = new RequestHandler(this.clientSocket.getInputStream());
        this.reader.onReadFromServer(new EventWithMessage<StringMessage>() {
            @Override
            public void onEventOccurred(StringMessage msg) {
                logger.debug("Got message from client: " + msg.getData());
                Message message = Server.this.config.getProtocol().toMessage(msg.getData());
                while (message != null) {
                    Server.this.invokeOnReceive(message);
                    message = Server.this.config.getProtocol().toMessage("");
                }
            }
        });
    }

    private class InnerServer implements Runnable {

        @Override
        public void run() {
            try {
                writer = null;
                reader = null;
                clientSocket = serverSocket.accept();
                writer = new PrintWriter(clientSocket.getOutputStream());
                invokeOnClientConnected();
                logger.debug("Client connected.");
                createReader();
                reader.onReaderClosed(new Event() {
                    @Override
                    public void onEventOccurred() {
                        invokeOnClientDisconnected();
                        logger.debug("Client disconnected.");
                        subThread = new Thread(new InnerServer());
                        subThread.start();
                    }
                });
                if(started) {
                    subThread = new Thread(reader);
                    subThread.start();
                }
            } catch (IOException e) {
                logger.trace("Exception occured while reading from server", e);
            }
        }
    }

    /**
     * Sends message to connected client.
     * @param message
     */
    public void send(Message message) {
        if (writer != null) {
            writer.print(this.config.getProtocol().toString(message));
            writer.flush();
        }
    }

    private void invokeOnReceive(Message message) {
        if (receiver != null) {
            receiver.onEventOccurred(message);
        }
    }

    /**
     * Setter for handler when received message.
     * @param receiver Handler to be called when message is received.
     */
    public void onReceive(EventWithMessage<Message> receiver) {
        this.receiver = receiver;
    }

    private void invokeOnClientConnected() {
        if(this.clientConnected != null) {
            this.clientConnected.onEventOccurred();
        }
    }

    /**
     * Setter for handler when client connects to server.
     * @param event Handler to be called when client connects to server.
     */
    public void onClientConnected(Event event) {
        this.clientConnected = event;
    }

    private void invokeOnClientDisconnected() {
        if(this.clientDisconnected != null) {
            this.clientDisconnected.onEventOccurred();
        }
    }

    /**
     * Setter for handler when client disconnects to server.
     * @param event Handler to be called when client disconnects to server.
     */
    public void onClientDisconnected(Event event) {
        this.clientDisconnected = event;
    }

    public boolean isStarted() {
        synchronized (runLock) {
            return this.started;
        }
    }
}
