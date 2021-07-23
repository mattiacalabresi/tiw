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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import controller.utils.ConnectionHandler;
import controller.utils.TemplateEngineBuilder;
import controller.utils.Values;
import dao.AuctionDAO;
import dao.ItemDAO;
import dao.OfferDAO;
import dao.UserDAO;
import exception.NoBuyerException;
import model.beans.AuctionDetails;
import model.beans.Item;
import model.beans.Offer;
import model.beans.User;

@WebServlet(Values.Web.SHOW_AUCTION_DETAILS_SERVLET)
public class ShowAuctionDetailsServlet extends HttpServlet {
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
			final int auctionId = Integer.parseInt(request.getParameter(Values.Params.AUCTION_ID));
			
			// check fo inadmissible IDs
			if(auctionId <= 0){
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID " + auctionId);
				return;
			}

			// retrieve auction details and user ID
			final AuctionDetails auction = AuctionDAO.from(this.connection).getAuctionDetails(auctionId);
			final int userId = (int) request.getSession().getAttribute(Values.Session.USER_ID);
			
			// check whether the current user is also the owner
			if(userId != auction.getOwnerId()) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User has no permission to access the specified content");
				return;
			}
			
			// retrieve item
			final Item item = ItemDAO.from(this.connection).getItem(auction.getItemId());
			
			// retrieve image error message (if any)
			final String imageErrorMessage = (String) request.getSession().getAttribute(Values.Params.IMAGE_ERROR_MESSAGE);
			request.getSession().removeAttribute(Values.Params.IMAGE_ERROR_MESSAGE);
			
			// retrieve success message (if any)
			final String successMessage = (String) request.getSession().getAttribute(Values.Params.SUCCESS_MESSAGE);
			request.getSession().removeAttribute(Values.Params.SUCCESS_MESSAGE);
			
			// prepare details to be shown
			final WebContext webContext = new WebContext(request, response, getServletContext(), request.getLocale());
			webContext.setVariable(Values.Params.AUCTION_DETAILS, auction);
			webContext.setVariable(Values.Params.ITEM, item);
			webContext.setVariable(Values.Params.IMAGE_ERROR_MESSAGE, imageErrorMessage);
			webContext.setVariable(Values.Params.SUCCESS_MESSAGE, successMessage);
			
			// different content will be shown depending whether the auction is open or closed
			if(auction.isClosed()) {	
				try {
					final User buyer = UserDAO.from(this.connection).getBuyer(auction.getId());
					webContext.setVariable(Values.Params.BUYER, buyer);
				} catch(final NoBuyerException ignored) {
					// no buyer found (eg. no one placed any offer on the auction: no winner, no buyer, no user to display)
				}
			} else {
				final List<Offer> offers = OfferDAO.from(this.connection).getOffers(auction.getId());
				webContext.setVariable(Values.Params.OFFERS, offers);
			}
			
			// show details page
			templateEngine.process(Values.Web.AUCTION_DETAILS_PAGE, webContext, response.getWriter());
			
		} catch (final NumberFormatException | NullPointerException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid Auction ID");
		} catch (final SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
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
