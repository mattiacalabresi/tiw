package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import controller.utils.ConnectionHandler;
import controller.utils.Values;
import dao.AuctionDAO;
import dao.UserDAO;
import exception.NoBuyerException;
import model.beans.AuctionDetails;
import model.beans.User;

@WebServlet(Values.Web.GET_BUYER_SERVLET)
public class GetBuyerServlet extends HttpServlet {
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
			final int auctionId = Integer.parseInt(request.getParameter(Values.Params.AUCTION_ID));
			
			// check fo inadmissible IDs
			if(auctionId <= 0){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Invalid ID " + auctionId);
				return;
			}

			// retrieve auction details
			final AuctionDetails auction = AuctionDAO.from(this.connection).getAuctionDetails(auctionId);
			
			// buyer is valid only for closed and expired auctions
			if(!auction.isClosed() || !auction.isExpired()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Buyer can only be retrieved for expired and closed auctions");
				return;
			}

			User buyer = null;
			try {
				 buyer = UserDAO.from(this.connection).getBuyer(auction.getId());
			} catch(final NoBuyerException ignored) {
				// no buyer found (eg. no one placed any offer on the auction: no winner, no buyer, no user to display)
			}
			
			// return user or empty
			List<Object> responseContent = new ArrayList<>(2);
			responseContent.add(buyer);
			responseContent.add(auction.getMaximumOffer()); // needed to show along buyer details
			
			final String json = new Gson().toJson(responseContent);
			
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(json);
			
		} catch (final NumberFormatException | NullPointerException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing or invalid Auction ID");
		} catch (final SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
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
