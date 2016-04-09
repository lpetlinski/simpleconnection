package lpetlinski.simpleconnection.protocol;

import lpetlinski.simpleconnection.events.ErrorMessage;
import lpetlinski.simpleconnection.events.EventWithMessage;

public abstract class BaseProtocol implements Protocol {

    private EventWithMessage<ErrorMessage> invalidMessage;
    private EventWithMessage<ErrorMessage> parseError;

    protected void invokeInvalidMessage(Exception exc) {
        if(this.invalidMessage != null) {
            invalidMessage.onEventOccurred(new ErrorMessage(exc));
        }
    }

    protected void invokeParseError(Exception exc) {
        if(this.parseError != null) {
            parseError.onEventOccurred(new ErrorMessage(exc));
        }
    }

    public void onInvalidMessage(EventWithMessage<ErrorMessage> event) {
        this.invalidMessage = event;
    }

    public void onParseError(EventWithMessage<ErrorMessage> event) {
        this.parseError = event;
    }

}
