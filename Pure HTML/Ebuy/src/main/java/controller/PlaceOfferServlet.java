package controller;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import controller.utils.ConnectionHandler;
import controller.utils.Values;
import dao.AuctionDAO;
import dao.OfferDAO;
import model.beans.AuctionDetails;
import model.beans.PlaceableOffer;

@WebServlet(Values.Web.PLACE_OFFER_SERVLET)
public class PlaceOfferServlet extends HttpServlet {
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
		int auctionId;
		try {
			// get auction ID
			auctionId = Integer.parseInt(request.getParameter(Values.Params.AUCTION_ID));

			// check for inadmissible IDs
			if (auctionId <= 0) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID " + auctionId);
				return;
			}
		} catch (final NumberFormatException | NullPointerException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid Auction ID");
			return;
		}
		
		try {
			final int userId = (int) request.getSession().getAttribute(Values.Session.USER_ID);
			final AuctionDetails auction = AuctionDAO.from(this.connection).getAuctionDetails(auctionId);
			
			// can't place an offer on my own auction
			if(userId == auction.getOwnerId()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Offerer is also the owner");
				return;
			}
			
			// can't place an offer on a closed auction
			if(auction.isClosed()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Can't place an offer on a closed auction");
				return;
			}
			
			// can't place an offer on an open but expired auction
			if(auction.isExpired()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Can't place an offer on an expired auction");
				return;
			}
			
			double offeredPrice;
			try {
				offeredPrice = Double.parseDouble(request.getParameter(Values.Params.OFFERED_PRICE));
			} catch (final NumberFormatException | NullPointerException e) {
				throw new InvalidParameterException("Missing or invalid offered price");
			}
			
			// can't place a negative or zero offer
			if(offeredPrice <= 0)
				throw new InvalidParameterException("Offered price must be a positive value");
			
			// can't place an offer below the minimum price
			if(offeredPrice < auction.getMinimumOffer())
				throw new InvalidParameterException("Offered price must be at least " + auction.getMinimumOffer() + "€");
			
			// can't place another offer if last one was also made by me
			final int lastOffererId = OfferDAO.from(this.connection).getLastOffererId(auction.getId());
			if(lastOffererId == userId)
				throw new InvalidParameterException("Rejected: you have already placed the highest offer");
			
			final PlaceableOffer offer = new PlaceableOffer(auction.getId(), userId, offeredPrice, LocalDateTime.now());
			OfferDAO.from(this.connection).placeOffer(offer);
			
			// notify the user with appropriate success message
			request.getSession().setAttribute(Values.Params.SUCCESS_MESSAGE, "Offer placed successfully");
			
			// redirect to offers page
			final String showOffersPath = getServletContext().getContextPath() + Values.Web.SHOW_OFFERS_SERVLET + "?" + Values.Params.AUCTION_ID + "=" + auction.getId();
			response.sendRedirect(showOffersPath);
		} catch (final InvalidParameterException e) {
			// notify the user with appropriate error message
			request.getSession().setAttribute(Values.Params.ERROR_MESSAGE, e.getMessage());

			final String showOffersPath = getServletContext().getContextPath() + Values.Web.SHOW_OFFERS_SERVLET + "?" + Values.Params.AUCTION_ID + "=" + auctionId;
			response.sendRedirect(showOffersPath);
		} catch (final SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to complete the specified operation");
		}
	}

	@Override
	public void destroy() {
		ConnectionHandler.closeConnection(this.connection);
	}
}
