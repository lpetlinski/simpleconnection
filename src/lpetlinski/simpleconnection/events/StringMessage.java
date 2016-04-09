package lpetlinski.simpleconnection.events;

public class StringMessage implements Message {

    private String data;

    public StringMessage(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}
