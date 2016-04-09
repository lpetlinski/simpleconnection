package lpetlinski.simpleconnection;

import lpetlinski.simpleconnection.events.Event;
import lpetlinski.simpleconnection.events.EventWithMessage;
import lpetlinski.simpleconnection.events.Message;
import lpetlinski.simpleconnection.events.StringMessage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    private Thread subThread;
    private PrintWriter writer;
    private RequestHandler reader;
    private Socket clientSocket;
    private ClientConfig config;
    private boolean started;
    private final Object runLock = new Object();

    private EventWithMessage<Message> receiver;
    private Event connectionClosed;

    public Client(ClientConfig config) {
        this.config = config;
        this.started = false;
        if(config == null) {
            this.config = new ClientConfig();
        }
    }

    public void startClient() throws IOException {
        synchronized (runLock) {
            if(this.started) {
                throw new IllegalStateException("Client already started");
            }
            this.started = true;
        }
        if(this.config.isSynchronous()) {
            startSynchronously();
        } else {
            this.subThread = new Thread(new InnerClient());
            this.subThread.start();
        }
    }

    private void startSynchronously() throws IOException {
        this.clientSocket = new Socket(this.config.getAddress(), this.config.getPort());
        this.writer = new PrintWriter(this.clientSocket.getOutputStream());
        createReader();
    }

    private void createReader() throws IOException {
        this.reader = new RequestHandler(this.clientSocket.getInputStream());
        this.reader.onReadFromServer(new EventWithMessage<StringMessage>() {
            @Override
            public void onEventOccurred(StringMessage msg) {
                Message message = Client.this.config.getProtocol().toMessage(msg.getData());
                if (message != null) {
                    Client.this.invokeOnReceive(message);
                }
            }
        });
        this.reader.onReaderClosed(new Event() {
            @Override
            public void onEventOccurred() {
                Client.this.invokeOnConnectionClosed();
                if (Client.this.isStarted()) {
                    Client.this.stopClient();
                }
            }
        });
        this.reader.startReading();
    }

    public void stopClient() {
        synchronized (runLock) {
            if(!this.started) {
                throw new IllegalStateException("Client already stopped");
            }
            this.started = false;
        }
        try {
            if(!this.clientSocket.isClosed()) {
                this.clientSocket.close();
            }
            if(this.subThread != null && this.subThread.isAlive()) {
                this.subThread.join();
            }
        } catch (Exception e) {
            // do nothing
        }
    }

    private class InnerClient implements Runnable {

        @Override
        public void run() {
            try {
                clientSocket = new Socket(config.getAddress(), config.getPort());
                writer = new PrintWriter(clientSocket.getOutputStream());
                createReader();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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

    public void onReceive(EventWithMessage<Message> receiver) {
        this.receiver = receiver;
    }

    private void invokeOnConnectionClosed() {
        if(this.connectionClosed != null) {
            this.connectionClosed.onEventOccurred();
        }
    }

    public void onConnectionClosed(Event event) {
        this.connectionClosed = event;
    }

    public boolean isStarted() {
        synchronized (runLock) {
            return this.started;
        }
    }
}
