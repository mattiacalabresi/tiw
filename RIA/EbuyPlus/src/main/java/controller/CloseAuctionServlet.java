package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNull;

import controller.utils.ConnectionHandler;
import controller.utils.Values;
import dao.AuctionDAO;
import model.beans.AuctionDetails;

@WebServlet(Values.Web.CLOSE_AUCTION_SERVLET)
public class CloseAuctionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@NonNull
	private Connection connection;

	@Override
	public void init() throws ServletException {
		this.connection = ConnectionHandler.createConnection(getServletContext());
	}

	@Override
	protected void doGet(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response) throws ServletException, IOException {
		try {
			final int auctionId = Integer.parseInt(request.getParameter(Values.Params.AUCTION_ID));

			// check fo inadmissible IDs
			if (auctionId <= 0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Invalid ID " + auctionId);
				return;
			}

			final AuctionDAO auctionDao = AuctionDAO.from(this.connection);

			// retrieve auction details and user ID
			final AuctionDetails auction = auctionDao.getAuctionDetails(auctionId);
			final int userId = (int) request.getSession().getAttribute(Values.Session.USER_ID);

			// check whether the current user is also the owner
			if (userId != auction.getOwnerId()) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			// check that the auction is not already closed
			if (auction.isClosed()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Attempted to close an already closed Auction");
				return;
			}
			
			// check that auction has expired (can't close a non-expired auction)
			if(auction.getExpireDateTime().isAfter(LocalDateTime.now())) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Attempted to close a non-expired Auction");
				return;
			}

			// close the auction
			auctionDao.closeAuction(auctionId);
			
			// return success message
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println("Auction closed successfully");
			
		} catch (final NumberFormatException | NullPointerException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing or invalid Auction ID");
		} catch (final SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Unable to close Auction with the given ID");
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
