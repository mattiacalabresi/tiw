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
import dao.ItemDAO;
import dao.OfferDAO;
import model.beans.AuctionDetails;
import model.beans.Item;
import model.beans.Offer;

@WebServlet(Values.Web.GET_OFFERS_SERVLET)
public class GetOffersServlet extends HttpServlet {
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
			if (auctionId <= 0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Invalid ID " + auctionId);
				return;
			}
			
			final int userId = (int) request.getSession().getAttribute(Values.Session.USER_ID);

			// retrieve data corresponding to the Auction ID
			final AuctionDetails auction = AuctionDAO.from(this.connection).getAuctionDetails(auctionId);
			
			// can't access my own auction
			if(auction.getOwnerId() == userId) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Invalid ID " + auctionId);
				return;
			}
			
			// can't show offers for a closed auction
			if(auction.isClosed()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Can't show offers for a closed auction");
				return;
			}
			
			// can't show offers for an open but expired auction
			if(auction.isExpired()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Can't show offers for an expired auction");
				return;
			}
			
			final Item item = ItemDAO.from(this.connection).getItem(auction.getItemId());
			final List<Offer> offers = OfferDAO.from(this.connection).getOffers(auction.getId());

			// group data to be retrieved
			final List<Object> responseContent = new ArrayList<>();
			responseContent.add(auction);
			responseContent.add(item);
			responseContent.add(offers);
			
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
			response.getWriter().println("Unable to retrieve Offers for the specified Auction");
		} catch (final ClassCastException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid erorr message");
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
