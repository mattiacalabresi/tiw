package controller;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import controller.utils.ConnectionHandler;
import controller.utils.Values;
import controller.utils.TemplateEngineBuilder;

@WebServlet(Values.Web.SHOW_LOGIN_SERVLET)
public class ShowLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Nullable
	private Connection connection;
	@Nullable
	private TemplateEngine templateEngine;

	@Override
	public void init() throws ServletException {
		this.connection = ConnectionHandler.createConnection(getServletContext());
		this.templateEngine = TemplateEngineBuilder.build(getServletContext());
	}

	@Override
	protected void doGet(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response) throws ServletException, IOException {
		try {
			// retrive and remove erorr messages (if any)
			final String errorMessage = (String) request.getSession().getAttribute(Values.Params.ERROR_MESSAGE);
			request.getSession().removeAttribute(Values.Params.ERROR_MESSAGE);

			// show error messages (if any)
			final WebContext webContext = new WebContext(request, response, getServletContext(), request.getLocale());
			webContext.setVariable(Values.Params.ERROR_MESSAGE, errorMessage);

			// show login page
			templateEngine.process(Values.Web.LOGIN_PAGE, webContext, response.getWriter());
		} catch (final ClassCastException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid erorr message");
		}
	}

	@Override
	protected void doPost(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The requested resource is not available");
	}
	
	@Override
	public void destroy() {
		ConnectionHandler.closeConnection(this.connection);
	}
}
