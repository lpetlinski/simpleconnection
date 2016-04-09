package lpetlinski.simpleconnection.protocol;

import lpetlinski.simpleconnection.events.Message;

public interface Protocol {

    public Message toMessage(String message);

    public String toString(Message message);
}
