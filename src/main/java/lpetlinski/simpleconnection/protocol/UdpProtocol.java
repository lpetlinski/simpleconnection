package lpetlinski.simpleconnection.protocol;

import lpetlinski.simpleconnection.events.UdpMessage;

public interface UdpProtocol {

    public UdpMessage toMessage(String message);

    public String toString(UdpMessage message);
}
