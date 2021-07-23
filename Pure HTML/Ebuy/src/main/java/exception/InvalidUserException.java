package exception;

import org.eclipse.jdt.annotation.NonNull;

public class InvalidUserException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidUserException(@NonNull final String message) {
		super(message);
	}
}
