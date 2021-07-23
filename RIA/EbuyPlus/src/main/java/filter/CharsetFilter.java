package filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.jdt.annotation.NonNull;

import controller.utils.Values;

/**
 * This filter intercepts all the requests and responses going
 * from-to a  Servlet and encodes them using the default
 * 'UTF-8' encoding.
 * 
 * Its usage is intended to be as the first filter of the chain
 * serving a given Servlet.
 *
 */
public class CharsetFilter implements Filter {

	@Override
	public void doFilter(@NonNull final ServletRequest request, @NonNull final ServletResponse response, @NonNull final FilterChain chain) throws IOException, ServletException {
		request.setCharacterEncoding(Values.Config.ENCODING_UTF_8);
		
		response.setCharacterEncoding(Values.Config.ENCODING_UTF_8);
		response.setContentType(Values.Config.CONTENT_TYPE_APPLICATION_JSON);
		
		// pass the request along the filter chain
		chain.doFilter(request, response);
	}
}