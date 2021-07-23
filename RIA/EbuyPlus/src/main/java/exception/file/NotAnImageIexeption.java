package exception.file;

import org.eclipse.jdt.annotation.NonNull;

public class NotAnImageIexeption extends Exception {
	private static final long serialVersionUID = 1L;

	public NotAnImageIexeption(@NonNull final String message) {
		super(message);
	}
}
