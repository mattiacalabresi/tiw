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
import model.beans.Auction;

@WebServlet(Values.Web.SHOW_BUY_SERVLET)
public class ShowBuyServlet extends HttpServlet {
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

	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response) throws ServletException, IOException {
		final int userId = (int) request.getSession().getAttribute(Values.Session.USER_ID);
		
		try {
			// retrieve searched and won auctions
			final AuctionDAO auctionDao = AuctionDAO.from(this.connection);
			final List<Auction> wonAuctions = auctionDao.getWonAuctions(userId);
			
			final List<Auction> searchedAuctions = (List<Auction>) request.getSession().getAttribute(Values.Params.SEARCHED_AUCTIONS);
			request.getSession().removeAttribute(Values.Params.SEARCHED_AUCTIONS);
			
			// save auctions to context
			final WebContext webContext = new WebContext(request, response, getServletContext(), request.getLocale());
			webContext.setVariable(Values.Params.WON_AUCTIONS, wonAuctions);
			webContext.setVariable(Values.Params.SEARCHED_AUCTIONS, searchedAuctions);
			
			// show buy page
			templateEngine.process(Values.Web.BUY_PAGE, webContext, response.getWriter());
		} catch (final SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to retrieve won Auctions for the specified User");
		} catch (final ClassCastException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid searched Auctions list");
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
