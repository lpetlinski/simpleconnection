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
            TemporaryMessage tmp = getMessage(message, mapper);
            if (tmp == null) {
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
            return (Message) result;
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

    private TemporaryMessage getMessage(String msg, ObjectMapper mapper) {
        TemporaryMessage tmp = null;
        String tmpMessage = buffer + msg;
        int end = tmpMessage.lastIndexOf("}");
        int actual = tmpMessage.indexOf("}");
        while (tmp == null && actual != -1 && actual <= end) {
            try {
                String actualMsg = tmpMessage.substring(0, actual + 1);
                tmp = mapper.readValue(actualMsg, TemporaryMessage.class);
                if(actual == end) {
                    buffer = "";
                } else {
                    buffer = tmpMessage.substring(actual + 1, tmpMessage.length());
                }
            } catch (IOException e) {
                actual = tmpMessage.indexOf("}", actual + 1);
            }
        }
        return tmp;
    }

    private static class TemporaryMessage {
        private String classType;
        private String message;

        public TemporaryMessage() {
        }

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
