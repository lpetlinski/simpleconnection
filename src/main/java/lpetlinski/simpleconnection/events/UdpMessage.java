package lpetlinski.simpleconnection.events;

public class UdpMessage implements Message {
    private String adress;
    private int port;

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
