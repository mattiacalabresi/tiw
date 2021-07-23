package controller;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import controller.utils.ConnectionHandler;
import controller.utils.Values;
import dao.UserDAO;
import exception.InvalidCredentialsException;

@WebServlet(Values.Web.CHECK_LOGIN_SERVLET)
@MultipartConfig
public class CheckLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Nullable
	private Connection connection;

	@Override
	public void init() throws ServletException {
		this.connection = ConnectionHandler.createConnection(getServletContext());
	}

	@Override
	protected void doGet(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The requested resource is not available");
	}

	@Override
	protected void doPost(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response) throws ServletException, IOException {
		try {
			// retrieve data froom request
			final String username = StringEscapeUtils.escapeJava(request.getParameter(Values.User.USERNAME));
			final String password = StringEscapeUtils.escapeJava(request.getParameter(Values.User.PASSWORD));
			
			// validate data
			if(username == null || password == null || username.isBlank() || password.isBlank()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing credentials");
				return;
			}
		
			// search the user in the DB
			final UserDAO userDao = UserDAO.from(this.connection);
		
			final int userId = userDao.getUserId(username, password);
			
			// user found: proceed and store useful data in the session
			request.getSession().setAttribute(Values.Session.USER_ID, userId);
			request.getSession().setAttribute(Values.Session.USERNAME, username);
			
			// return ok status
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(username);

		} catch (final InvalidCredentialsException e) {
			// user not found, send error message
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Invalid username or password");
		} catch (final SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Something went wrong: " + e.getMessage());
		} catch(final NullPointerException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing or invalid credentials");
		}
	}
	
	@Override
	public void destroy() {
		ConnectionHandler.closeConnection(this.connection);
	}
}
