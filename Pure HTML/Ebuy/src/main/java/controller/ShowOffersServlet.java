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
import model.beans.AuctionDetails;
import model.beans.Item;
import model.beans.Offer;

@WebServlet(Values.Web.SHOW_OFFERS_SERVLET)
public class ShowOffersServlet extends HttpServlet {
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
			if (auctionId <= 0) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID " + auctionId);
				return;
			}
			
			final int userId = (int) request.getSession().getAttribute(Values.Session.USER_ID);

			// retrieve data corresponding to the Auction ID
			final AuctionDetails auction = AuctionDAO.from(this.connection).getAuctionDetails(auctionId);
			
			// can't access my own auction
			if(auction.getOwnerId() == userId) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID " + auctionId);
				return;
			}
			
			// can't show offers for a closed auction
			if(auction.isClosed()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Can't show offers for a closed auction");
				return;
			}
			
			// can't show offers for an open but expired auction
			if(auction.isExpired()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Can't show offers for an expired auction");
				return;
			}
			
			final Item item = ItemDAO.from(this.connection).getItem(auction.getItemId());
			final List<Offer> offers = OfferDAO.from(this.connection).getOffers(auction.getId());
			
			// retrieve error message (if any)
			final String errorMessage = (String) request.getSession().getAttribute(Values.Params.ERROR_MESSAGE);
			request.getSession().removeAttribute(Values.Params.ERROR_MESSAGE);
			
			// retrieve success message (if any)
			final String successMessage = (String) request.getSession().getAttribute(Values.Params.SUCCESS_MESSAGE);
			request.getSession().removeAttribute(Values.Params.SUCCESS_MESSAGE);

			// save data to context
			final WebContext webContext = new WebContext(request, response, getServletContext(), request.getLocale());
			webContext.setVariable(Values.Params.AUCTION_DETAILS, auction);
			webContext.setVariable(Values.Params.ITEM, item);
			webContext.setVariable(Values.Params.OFFERS, offers);
			// save error message (if any)
			webContext.setVariable(Values.Params.ERROR_MESSAGE, errorMessage);
			// save success message (if any)
			webContext.setVariable(Values.Params.SUCCESS_MESSAGE, successMessage);

			// show details page
			templateEngine.process(Values.Web.OFFERS_PAGE, webContext, response.getWriter());
		} catch (final NumberFormatException | NullPointerException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid Auction ID");
		} catch (final SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to retrieve Offers for the specified Auction");
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
