package exception.file;

import org.eclipse.jdt.annotation.NonNull;

public class FileEmptyException  extends Exception {
	private static final long serialVersionUID = 1L;

	public FileEmptyException(@NonNull final String message) {
		super(message);
	}
}
