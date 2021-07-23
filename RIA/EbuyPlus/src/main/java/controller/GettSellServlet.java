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
import model.beans.Auction;

@WebServlet(Values.Web.GET_SELL_SERVLET)
public class GettSellServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	@Nullable
	private Connection connection;
	
	@Override
	public void init() throws ServletException {
		this.connection = ConnectionHandler.createConnection(getServletContext());
	}

	@Override
	protected void doGet(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response) throws ServletException, IOException {
		final int userId = (int) request.getSession().getAttribute(Values.Session.USER_ID);
		
		try {
			final AuctionDAO auctionDao = AuctionDAO.from(this.connection);
			
			// retrieve open auctions
			final List<Auction> openAuctions = auctionDao.getOpenAuctions(userId);
			
			// retrieve closed auctions
			final List<Auction> closedAuctions = auctionDao.getClosedAuctions(userId);
			
			// prepare data to be converted to JSON
			final List<Object> responseContent = new ArrayList<>();
			responseContent.add(openAuctions);
			responseContent.add(closedAuctions);
			
			final String json = new Gson().toJson(responseContent);
			
			// send JSON data to the client
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(json);
		} catch (final SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An unexpected error occurred, retry");
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
