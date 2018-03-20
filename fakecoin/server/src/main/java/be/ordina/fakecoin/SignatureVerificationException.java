package be.ordina.fakecoin;

public class SignatureVerificationException extends RuntimeException {

    public SignatureVerificationException(Throwable cause) {
        super(cause);
    }
}
