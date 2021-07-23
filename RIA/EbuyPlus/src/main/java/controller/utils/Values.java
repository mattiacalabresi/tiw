package controller.utils;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A collection of all the values
 * needed in the application, grouped in
 * one place for convenience.
 */
public class Values {
	public static class Web {
		// servlet mappings
		@NonNull
		public static final String CHECK_LOGIN_SERVLET = "/" + "check-login";
		@NonNull
		public static final String GET_SELL_SERVLET = "/" + "sell";
		@NonNull
		public static final String GET_BUYER_SERVLET = "/" + "get-buyer";
		@NonNull
		public static final String LOGOUT_SERVLET = "/" + "logout";
		@NonNull
		public static final String CREATE_AUCTION_SERVLET = "/" + "create";
		@NonNull
		public static final String GET_AUCTION_DETAILS_SERVLET = "/" + "details";
		@NonNull
		public static final String CLOSE_AUCTION_SERVLET = "/" + "close";
		@NonNull
		public static final String SEARCH_AUTCTIONS_SERVLET = "/" + "search";
		@NonNull
		public static final String GET_BUY_SERVLET = "/" + "buy";
		@NonNull
		public static final String GET_OFFERS_SERVLET = "/" + "offers";
		@NonNull
		public static final String PLACE_OFFER_SERVLET = "/" + "place";
		@NonNull
		public static final String GET_IMAGE_SERVLET = "/" + "get-image";

		// location of html pages
		@NonNull
		private static final String WEB_INF = "WEB-INF";

		private Web() {

		}
	}
	
	public static class Params {
		// absolute path where images are stored
		@NonNull
		public static final String IMAGE_FOLDER_PATH = "C:\\absolute\\path\\to\\item-images\\";
		
		// from javascript to java
		@NonNull
		public static final String AUCTION_ID = "auction_id";
		@NonNull
		public static final String SEARCH_QUERY = "search_query";
		@NonNull
		public static final String OFFERED_PRICE = "offered_price";
		@NonNull
		public static final String RECENT_AUCTIONS_IDS = "recent_ids";
		
		private Params() {
			
		}
	}

	public static class Session {
		@NonNull
		public static final String USER_ID = "user_id";
		@NonNull
		public static final String USERNAME = "username";

		private Session() {

		}
	}

	public static class User {
		@NonNull
		public static final String ID = "id";
		@NonNull
		public static final String USERNAME = "username";
		@NonNull
		public static final String PASSWORD = "password";
		@NonNull
		public static final String SHIPPING_ADDRESS = "shipping_address";
		
		@NonNull
		public static final String EMPTY_FIELD = "null";

		private User() {

		}
	}

	public static class Auction {
		@NonNull
		public static final String ID = "id";
		@NonNull
		public static final String ITEM_ID = "item_id";
		@NonNull
		public static final String OWNER_ID = "owner_id";
		@NonNull
		public static final String BASE_PRICE = "base_price";
		@NonNull
		public static final String MINIMUM_RISE = "minimum_rise";
		@NonNull
		public static final String EXPIRE_DATETIME = "expire_datetime";
		@NonNull
		public static final String EXPIRE_DATETIME_DB = "expire_timestamp";
		@NonNull
		public static final String CLOSED = "closed";
		@NonNull
		public static final String MAXIMUM_OFFER = "maximum_offer";
		@NonNull
		public static final String EXPIRE_SECONDS = "expire_seconds";

		static final double MIN_BASE_PRICE = 0.01;
		static final int MIN_MINIMUM_RISE = 1;

		private Auction() {

		}
	}

	public static class Item {
		@NonNull
		public static final String ID = "id";
		@NonNull
		public static final String CODE = "code";
		@NonNull
		public static final String NAME = "name";
		@NonNull
		public static final String DESCRIPTION = "description";
		@NonNull
		public static final String IMAGE = "image";
		@NonNull
		public static final String IMAGE_NAME = "image_name";

		static final int MAX_CODE_LENGTH = 45;
		static final int MAX_NAME_LENGTH = 45;
		static final int MAX_DESCRIPTION_LENGTH = 100;
		static final double MAX_IMAGE_SIZE = 1e7; // in bytes
		static final int MAX_IMGAGE_SIZE_MB = 10;

		private Item() {

		}
	}

	public static class Offer {
		@NonNull
		public static final String PRICE = "price";
		@NonNull
		public static final String TIMESTAMP = "timestamp";

		private Offer() {

		}
	}
	
	public static class Config {
		@NonNull
		public static final String ENCODING_UTF_8 = "UTF-8";
		@NonNull
		public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
		
		private Config() {
			
		}
	}

	private Values() {

	}
}
