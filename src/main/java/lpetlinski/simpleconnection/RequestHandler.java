package lpetlinski.simpleconnection;

import lpetlinski.simpleconnection.events.Event;
import lpetlinski.simpleconnection.events.EventWithMessage;
import lpetlinski.simpleconnection.events.StringMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class RequestHandler implements Runnable {

    private BufferedReader reader;
    private EventWithMessage<StringMessage> onRead;
    private Event onReadClose;

    RequestHandler(InputStream stream) {
        this.reader = new BufferedReader(new InputStreamReader(stream));
    }

    void startReading() {
        char[] buffer = new char[128];
        try {
            int read = 0;
            while (read != -1) {
                read = this.reader.read(buffer);
                if (read != -1) {
                    String tmp = new String(buffer);
                    this.invokeOnRead(tmp.substring(0, read));
                }
            }
        } catch (IOException exc) {
            // do nothing
        }
        this.invokeOnReaderClosed();
    }

    private void invokeOnRead(String data) {
        if (this.onRead != null) {
            this.onRead.onEventOccurred(new StringMessage(data));
        }
    }

    private void invokeOnReaderClosed() {
        if (this.onReadClose != null) {
            this.onReadClose.onEventOccurred();
        }
    }

    void onReadFromServer(EventWithMessage<StringMessage> onRead) {
        this.onRead = onRead;
    }

    void onReaderClosed(Event onReaderClosed) {
        this.onReadClose = onReaderClosed;
    }

    @Override
    public void run() {
        this.startReading();
    }
}
