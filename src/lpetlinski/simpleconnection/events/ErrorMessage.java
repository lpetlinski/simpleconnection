package lpetlinski.simpleconnection.events;

public class ErrorMessage implements Message {
    private Exception exception;

    public ErrorMessage(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
