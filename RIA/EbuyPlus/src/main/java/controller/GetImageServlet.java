package controller;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNull;

import controller.utils.Values;

@WebServlet(Values.Web.GET_IMAGE_SERVLET + "/*")
public class GetImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response) throws ServletException, IOException {
		final String pathInfo = request.getPathInfo();

		if (pathInfo == null || pathInfo.equals("/")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("No image specified for this product");
			return;
		}

		// substring(1) to removes the first "/" which is not part of the image name
		final String imageName = URLDecoder.decode(pathInfo.substring(1), "UTF-8");

		final File image = new File(Values.Params.IMAGE_FOLDER_PATH, imageName);
		
		if (!image.exists() || image.isDirectory()) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println("Image not found");
			return;
		}

		// set headers for browser
		response.setHeader("Content-Type", getServletContext().getMimeType(imageName));
		response.setHeader("Content-Length", String.valueOf(image.length()));

		response.setHeader("Content-Disposition", "inline; filename=\"" + image.getName() + "\"");

		// copy file to output stream
		Files.copy(image.toPath(), response.getOutputStream());
	}

	@Override
	protected void doPost(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The requested resource is not available");
	}

}
