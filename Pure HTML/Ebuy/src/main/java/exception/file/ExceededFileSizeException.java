package exception.file;

import org.eclipse.jdt.annotation.NonNull;

public class ExceededFileSizeException extends Exception {
	private static final long serialVersionUID = 1L;

	public ExceededFileSizeException(@NonNull final String message) {
		super(message);
	}
}
