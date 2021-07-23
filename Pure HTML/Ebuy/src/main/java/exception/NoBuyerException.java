package exception;

import org.eclipse.jdt.annotation.NonNull;

public class NoBuyerException extends Exception {
	private static final long serialVersionUID = 1L;

	public NoBuyerException(@NonNull final String message) {
		super(message);
	}
}