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
import com.google.gson.GsonBuilder;

import controller.utils.ConnectionHandler;
import controller.utils.Values;
import dao.AuctionDAO;
import dao.ItemDAO;
import dao.OfferDAO;
import model.beans.AuctionDetails;
import model.beans.Item;
import model.beans.Offer;

@WebServlet(Values.Web.GET_AUCTION_DETAILS_SERVLET)
public class GetAuctionDetailsServlet extends HttpServlet {
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
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID " + auctionId);
				return;
			}

			// retrieve auction details and user ID
			final AuctionDetails auction = AuctionDAO.from(this.connection).getAuctionDetails(auctionId);
			final int userId = (int) request.getSession().getAttribute(Values.Session.USER_ID);
			
			// check whether the current user is also the owner
			if(userId != auction.getOwnerId()) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
			
			// prepare response container
			final List<Object> responseContent = new ArrayList<>(3);
			
			// retrieve item
			final Item item = ItemDAO.from(this.connection).getItem(auction.getItemId());
			
			// insert data in the container
			responseContent.add(auction);
			responseContent.add(item);
			
			// different content will be shown depending whether the auction is open or closed
			if(!auction.isClosed()) {	
				final List<Offer> offers = OfferDAO.from(this.connection).getOffers(auction.getId());
				responseContent.add(offers);
			}
			
			// return json container as result
			final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			final String json = gson.toJson(responseContent);
			
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
