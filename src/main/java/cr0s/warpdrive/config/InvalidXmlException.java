package cr0s.warpdrive.config;

public class InvalidXmlException extends Exception {

	public InvalidXmlException() {
		super("An unknown XML error occurred");
	}

	public InvalidXmlException(String message) {
		super(message);
	}

	public InvalidXmlException(Throwable cause) {
		super(cause);
	}

	public InvalidXmlException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidXmlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
