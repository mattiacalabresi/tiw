package controller.utils;

import java.util.Arrays;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An helper class that retrieves data from cookies contained
 * in an {@code HttpServletRequest}.
 */
public class CookieHelper {
	/**
	 * The character used to separate items in a list
	 * inside a cookie.
	 */
	@NonNull
	private static final String LIST_SEPARATOR = "-";

	private CookieHelper() {

	}

	/**
	 * Returns the list of IDs contained in the given request.
	 * If the list is empty, {@code null} will be returned instead.
	 */
	@Nullable
	public static int[] getRecentIds(@NonNull final HttpServletRequest request) throws NullPointerException, NumberFormatException {
		// retrieve the auction_ids cookie from response header
		final Cookie[] cookies = request.getCookies();
		String idsString = null;

		final String username = (String) request.getSession().getAttribute(Values.User.USERNAME);
		final String cookieName = username + "_" + Values.Params.RECENT_AUCTIONS_IDS;

		for (int i = 0; i < cookies.length; i++) {
			final Cookie cookie = cookies[i];

			if (cookie.getName().equals(cookieName)) {
				// cookie found
				idsString = cookie.getValue().isEmpty() ? null : cookie.getValue();
				break;
			}
		}

		int ids[] = null;

		if (idsString != null) {
			// parse IDs from string to int
			ids = Arrays.stream(idsString.split(LIST_SEPARATOR)).mapToInt(Integer::parseInt).toArray();
		}
		return ids;
	}
}
