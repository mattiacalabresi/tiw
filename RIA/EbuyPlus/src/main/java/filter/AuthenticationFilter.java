package filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jdt.annotation.NonNull;

import controller.utils.Values;
import exception.InvalidUserException;

/**
 * Check whether the user data in the session are valid and, if not, notifies
 * the user he is not authorized to proceed.
 * 
 * This filter should be used in every Servlet to avoid unauthorized access to private
 * content.
 */
public class AuthenticationFilter implements Filter {

	@Override
	public void doFilter(@NonNull final ServletRequest servletRequest, @NonNull final ServletResponse servletResponse, @NonNull final FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest request = (HttpServletRequest) servletRequest;
		final HttpServletResponse response = (HttpServletResponse) servletResponse;
		
		try {
			final HttpSession session = request.getSession();
			
			// retrieve user information from session
			final int userId = (int) session.getAttribute(Values.Session.USER_ID);
			final String username = (String) session.getAttribute(Values.User.USERNAME);

			// validate user information
			if(session.isNew() || userId <= 0|| username == null || username.isBlank())
				throw new InvalidUserException("Unable to authenticate User");
			
			// pass the request along the filter chain
			chain.doFilter(request, response);
		} catch(final NumberFormatException | ClassCastException | NullPointerException | InvalidUserException e) {
			e.printStackTrace();
			// notify the client he is not authorizzed to proceed
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}