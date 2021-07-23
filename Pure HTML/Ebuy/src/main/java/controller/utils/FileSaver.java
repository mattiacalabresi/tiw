package controller.utils;

import java.time.format.DateTimeFormatter;

import javax.servlet.ServletContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import exception.file.ExceededFileSizeException;
import exception.file.FileEmptyException;
import exception.file.NoFileException;
import exception.file.NotAnImageIexeption;

/**
 * Utility class used to interact with the
 * local storage.
 */
public class FileSaver {
	@NonNull
	private static final String TEMP_DIR = "javax.servlet.context.tempdir";
	
	private FileSaver() {
		
	}
	
	/**
	 * Return a unique string representing the current date and time.
	 * This string is suitable to be used for a file.
	 * The string is in the format "yyyyMMdd_HHmmssSSS"
	 * and does not include the file extension.
	 */
	@NonNull
	private static String getDate() {
		return DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS").format(LocalDateTime.now());
	}
	
	/**
	 * Return a unique file name using information about the current time.
	 * The returned name prederves the extension of the given one.
	 * The old file name is solely used to retrieve information about 
	 * the file extension.
	 */
	@NonNull
	private static String rename(@NonNull final String oldFileName) {
		final String[] parts = oldFileName.split("\\.");
		final String fileExtension = parts[parts.length-1];
		
		return FileSaver.getDate() + "." + fileExtension;
	}
	
	/**
	 * Save the uploaded file in the default directory, then returns
	 * the name of the saved file.
	 */
	@NonNull
	static String saveAndGet(@NonNull final String folderPath, @Nullable final FileItem file)
			throws NoFileException, FileEmptyException, NotAnImageIexeption, FileUploadException, ExceededFileSizeException {
		if (file == null)
			throw new NoFileException("The uploaded file does not exists");
		if (file.getSize() <= 0)
			throw new FileEmptyException("The uploaded file doesn't exists or is empty");
		if (file.getSize() > Values.Item.MAX_IMAGE_SIZE)
			throw new ExceededFileSizeException("File size can't exceed " + Values.Item.MAX_IMGAGE_SIZE_MB + " MB");

		final String contentType = file.getContentType();

		if (!contentType.startsWith("image"))
			throw new NotAnImageIexeption("The uploaded file is not an image");

		// choose a unique name for the item image
		final String oldFileName = file.getName();
		final String newFileName = FileSaver.rename(oldFileName);

		final String itemImagePath = folderPath + newFileName;
		final File image = new File(itemImagePath);

		// save file to default folder
		try (final InputStream inputStream = file.getInputStream()) {
			Files.copy(inputStream, image.toPath());
		} catch (final IOException e) {
			throw new FileUploadException("Unable to write file to local storage");
		}
		return newFileName;
	}
	
	/**
	 * Create and return the {@link ServletFileUpload}
	 * corresponding to the temp directory where
	 * large files will be stored while waiting to be processed.
	 */
	@NonNull
	public static ServletFileUpload getServletFileUpload(@NonNull final ServletContext context) {
		// create a factory for disk-based file items
		final DiskFileItemFactory factory = new DiskFileItemFactory();

		// configure a repository (to ensure a secure temp location is used)
		final File repository = (File) context.getAttribute(TEMP_DIR);
		factory.setRepository(repository);

		// create a new file upload handler
		return new ServletFileUpload(factory);
	}
} 
