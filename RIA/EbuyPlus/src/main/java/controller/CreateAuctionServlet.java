package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;

import controller.utils.AuctionFormValidator;
import controller.utils.ConnectionHandler;
import controller.utils.FileSaver;
import controller.utils.Values;
import dao.AuctionDAO;
import dao.ItemDAO;
import model.beans.AuctionDetails;
import model.beans.Item;

@WebServlet(Values.Web.CREATE_AUCTION_SERVLET)
@MultipartConfig
public class CreateAuctionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Nullable
	private Connection connection;
	@Nullable
	private ServletFileUpload servletFileUpload;

	@Override
	public void init() throws ServletException {
		this.connection = ConnectionHandler.createConnection(getServletContext());
		this.servletFileUpload = FileSaver.getServletFileUpload(getServletContext());
	}

	@Override
	protected void doGet(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The requested resource is not available");
	}

	@Override
	protected void doPost(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response) throws ServletException, IOException {
		// parse the request
		final Map<String, List<FileItem>> attributes = this.servletFileUpload.parseParameterMap(request);

		try {
			// retrieve all form fields
			final String itemCodeStr = StringEscapeUtils.escapeJava(attributes.get(Values.Item.CODE).get(0).getString());
			final String itemNameStr = StringEscapeUtils.escapeJava(attributes.get(Values.Item.NAME).get(0).getString());
			final String itemDescriptionStr = StringEscapeUtils.escapeJava(attributes.get(Values.Item.DESCRIPTION).get(0).getString());
			final FileItem itemImage = attributes.get(Values.Item.IMAGE).get(0);
			
			final String basePriceStr = StringEscapeUtils.escapeJava(attributes.get(Values.Auction.BASE_PRICE).get(0).getString());
			final String minimumRiseStr = StringEscapeUtils.escapeJava(attributes.get(Values.Auction.MINIMUM_RISE).get(0).getString());
			final String expireDateTimeStr = StringEscapeUtils.escapeJava(attributes.get(Values.Auction.EXPIRE_DATETIME).get(0).getString());
			
			// validate all form fields
			final AuctionFormValidator validator = AuctionFormValidator.getInstance();
			validator.validate(itemCodeStr, itemNameStr, itemDescriptionStr, itemImage, basePriceStr, minimumRiseStr, expireDateTimeStr);
			
			// proceed only if all fields are valid
			if(validator.isValidForm()) {
				// retrieve user ID from session
				final int userId = (int) request.getSession().getAttribute(Values.Session.USER_ID);

				// create and insert in the DB a new item
				final Item item = new Item(validator.getItemCode(), validator.getItemName(), validator.getItemDescription(), validator.getItemImageName());
				ItemDAO.from(this.connection).insertItem(item);

				// create and insert in the DB a new Auction (referring to the current item)
				final AuctionDetails auction = new AuctionDetails(item.getId(), userId, validator.getBasePrice(), validator.getMinimumRise(), validator.getExpireDateTime());
				AuctionDAO.from(this.connection).insertAuction(auction);
				
				validator.setSuccessMessage("Auction created successfully");
				validator.setAuctionId(auction.getId());
			}
			
			// return the validator containing the creation result
			final String json = new Gson().toJson(validator);

			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(json);

		} catch (final FileUploadException e) {
				e.printStackTrace();
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println(e.getMessage());
		} catch (final SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Unable to complete the specified operation");
		}
	}
	
	@Override
	public void destroy() {
		ConnectionHandler.closeConnection(this.connection);
	}
}
