package backend.exception;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(String  id) {
        super("Could not find ticket with id " + id);
    }
}
