package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import controller.utils.ConnectionHandler;
import controller.utils.Values;
import dao.AuctionDAO;
import model.beans.Auction;

@WebServlet(Values.Web.SEARCH_AUTCTIONS_SERVLET)
public class SearchAuctionsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Nullable
	private Connection connection;
	
	@Override
	public void init() throws ServletException {
		this.connection = ConnectionHandler.createConnection(getServletContext());
	}

	@Override
	protected void doGet(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response) throws ServletException, IOException {
		try {
			// retrieve search query
			final String searchQuery = StringEscapeUtils.escapeJava(request.getParameter(Values.Params.SEARCH_QUERY));
			
			// check for valid query string
			if(searchQuery == null || searchQuery.isBlank()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or empty search query");
				return;
			}
			
			final int userId = (int) request.getSession().getAttribute(Values.Session.USER_ID);
		
			// get auctions corresponding to search query
			final List<Auction> searchedAuctions  = AuctionDAO.from(this.connection).findValidAuctions(userId, searchQuery);
			// store result in session
			request.getSession().setAttribute(Values.Params.SEARCHED_AUCTIONS, searchedAuctions);
			
			// show buy page
			final String showBuyPath = getServletContext().getContextPath() + Values.Web.SHOW_BUY_SERVLET;
			response.sendRedirect(showBuyPath);
		} catch (final SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to find the specified Auctions");
		} catch (final NumberFormatException | NullPointerException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid search query");
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
