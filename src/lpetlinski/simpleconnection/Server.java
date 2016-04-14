package lpetlinski.simpleconnection;

import lpetlinski.simpleconnection.events.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

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

    public Server(ServerConfig config) {
        this.config = config;
        if(config == null) {
            this.config = new ServerConfig();
        }
        this.started = false;
    }

    public void startServer() throws IOException {
        synchronized (this.runLock) {
            if (this.started) {
                throw new IllegalStateException("Server already started");
            }
            this.started = true;
        }
        this.serverSocket = new ServerSocket(this.config.getPort());
        if (this.config.isSynchronous()) {
            runServerSynchronously();
        } else {
            this.subThread = new Thread(new InnerServer());
            this.subThread.start();
        }
    }

    private void runServerSynchronously() {
        while (true) {
            try {
                this.writer = null;
                this.reader = null;
                this.clientSocket = serverSocket.accept();
                this.writer = new PrintWriter(this.clientSocket.getOutputStream());
                invokeOnClientConnected();
                createReader();
                this.reader.startReading();
            } catch (IOException e) {
                // do nothing
            }
            invokeOnClientDisconnected();
        }
    }

    public void stopServer() throws IOException, InterruptedException {
        synchronized (this.runLock) {
            if (!this.started) {
                throw new IllegalStateException("Server not started");
            }
            this.started = false;
        }
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
                invokeOnClientConnected();
                writer = new PrintWriter(clientSocket.getOutputStream());
                createReader();
                reader.onReaderClosed(new Event() {
                    @Override
                    public void onEventOccurred() {
                        invokeOnClientDisconnected();
                        subThread = new Thread(new InnerServer());
                        subThread.start();
                    }
                });
                subThread = new Thread(reader);
                subThread.start();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

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

    public void onReceive(EventWithMessage<Message> receiver) {
        this.receiver = receiver;
    }

    private void invokeOnClientConnected() {
        if(this.clientConnected != null) {
            this.clientConnected.onEventOccurred();
        }
    }

    public void onClientConnected(Event event) {
        this.clientConnected = event;
    }

    private void invokeOnClientDisconnected() {
        if(this.clientDisconnected != null) {
            this.clientDisconnected.onEventOccurred();
        }
    }

    public void onClientDisconnected(Event event) {
        this.clientDisconnected = event;
    }

    public boolean isStarted() {
        synchronized (runLock) {
            return this.started;
        }
    }
}
