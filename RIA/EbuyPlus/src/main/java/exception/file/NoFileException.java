package exception.file;

import org.eclipse.jdt.annotation.NonNull;

public class NoFileException extends Exception {
	private static final long serialVersionUID = 1L;

	public NoFileException(@NonNull final String message) {
		super(message);
	}
}