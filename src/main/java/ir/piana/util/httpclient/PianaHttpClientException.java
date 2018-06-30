package ir.piana.util.httpclient;

public class PianaHttpClientException extends Exception {
    private PianaRestException restException;

    public PianaHttpClientException(PianaRestException restException) {
        super();
        this.restException = restException;
    }

    public PianaHttpClientException(
            PianaRestException restException, String message) {
        super(message);
        this.restException = restException;
    }

    public PianaHttpClientException(
            PianaRestException restException, Exception exception) {
        super(exception);
        this.restException = restException;
    }

    public PianaRestException getRestException() {
        return restException;
    }
}
