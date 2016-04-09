package lpetlinski.simpleconnection.events;

public interface EventWithMessage<T extends Message>  {

    public void onEventOccurred(T event);

}
