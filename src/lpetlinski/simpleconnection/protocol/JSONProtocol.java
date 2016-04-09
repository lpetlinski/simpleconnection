package lpetlinski.simpleconnection.protocol;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lpetlinski.simpleconnection.events.Message;

import java.io.IOException;

public class JSONProtocol extends BaseProtocol {

    private String buffer = "";
    private Object lockObj = new Object();

    @Override
    public Message toMessage(String message) {
        synchronized (lockObj) {
            ObjectMapper mapper = new ObjectMapper();
            TemporaryMessage tmp;
            try {
                tmp = mapper.readValue(buffer + message, TemporaryMessage.class);
            } catch (IOException e) {
                buffer += message;
                return null;
            }

            Class klass;
            try {
                klass = Class.forName(tmp.getClassType());
            } catch (ClassNotFoundException e) {
                this.invokeParseError(e);
                return null;
            }

            Object result;
            try {
                result = mapper.readValue(tmp.getMessage(), klass);
            } catch (IOException e) {
                this.invokeParseError(e);
                return null;
            }

            this.buffer = "";
            return (Message)result;
        }
    }

    @Override
    public String toString(Message message) {
        String result = null;
        try {
            ObjectMapper mapper = new ObjectMapper();

            TemporaryMessage tmp = new TemporaryMessage();
            tmp.setMessage(mapper.writeValueAsString(message));
            tmp.setClassType(message.getClass().getName());

            result = mapper.writeValueAsString(tmp);

        } catch (Exception e) {
            this.invokeInvalidMessage(e);
        }
        return result;
    }

    private static class TemporaryMessage{
        private String classType;
        private String message;

        public TemporaryMessage() {}

        @JsonGetter
        public String getClassType() {
            return classType;
        }

        @JsonSetter
        public void setClassType(String classType) {
            this.classType = classType;
        }

        @JsonGetter
        public String getMessage() {
            return message;
        }

        @JsonSetter
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
