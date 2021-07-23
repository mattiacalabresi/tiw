package exception;

import org.eclipse.jdt.annotation.NonNull;

public class InvalidCredentialsException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidCredentialsException(@NonNull final String message) {
		super(message);
	}
}