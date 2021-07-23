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

import controller.utils.AuctionFormValidator;
import controller.utils.ConnectionHandler;
import controller.utils.TemplateEngineBuilder;
import controller.utils.Values;
import dao.AuctionDAO;
import model.beans.Auction;

@WebServlet(Values.Web.SHOW_SELL_SERVLET)
public class ShowSellServlet extends HttpServlet {
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
		// get user id
		final int userId = (int) request.getSession().getAttribute(Values.Session.USER_ID);

		try {
			final AuctionDAO auctionDao = AuctionDAO.from(this.connection);

			final List<Auction> openAuctions = auctionDao.getOpenAuctions(userId);
			final List<Auction> closedAuctions = auctionDao.getClosedAuctions(userId);
			
			// retrive and remove the validator (if present)
			final AuctionFormValidator validator = (AuctionFormValidator) request.getSession().getAttribute(Values.Params.AUCTION_FORM_VALIDATOR);
			request.getSession().removeAttribute(Values.Params.AUCTION_FORM_VALIDATOR);

			// save auctions to context
			final WebContext webContext = new WebContext(request, response, getServletContext(), request.getLocale());
			webContext.setVariable(Values.Params.OPEN_AUCTIONS, openAuctions);
			webContext.setVariable(Values.Params.CLOSED_AUCTIONS, closedAuctions);
			// save validator (if present)
			webContext.setVariable(Values.Params.AUCTION_FORM_VALIDATOR, validator);

			// show offers page
			templateEngine.process(Values.Web.SELL_PAGE, webContext, response.getWriter());
		} catch (final SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to retrieve Auctions for the specified User");
		} catch (final ClassCastException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid error message");
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
