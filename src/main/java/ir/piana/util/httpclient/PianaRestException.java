package ir.piana.util.httpclient;

public enum PianaRestException {
    DUPLICATE_PATH_PARAM,
    METHOD_NOT_SUPPORT_BODY,
    JSON_PROCESSING_EXCEPTION,
    AMBIGUOUS_STATUS,
    INTERRUPTED_EXCEPTION,
    EXECUTION_EXCEPTION,
    TIMEOUT_EXCEPTION;

    PianaRestException() {
    }
}
