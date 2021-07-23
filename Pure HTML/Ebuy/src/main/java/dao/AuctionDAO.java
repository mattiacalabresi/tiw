package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import controller.utils.Values;
import model.Time;
import model.beans.AuctionDetails;
import model.beans.User;
import model.beans.Auction;

/**
 * Helper class that interacts to the database
 * and handles all the procedures regarding a
 * {@link Auction} or {@link AuctionDetails}.
 */
public class AuctionDAO {
	@NonNull
	private final Connection connection;

	private AuctionDAO(@NonNull final Connection connection) {
		this.connection = connection;
	}

	@NonNull
	public static AuctionDAO from(@NonNull final Connection connection) {
		return new AuctionDAO(connection);
	}
	
	/**
	 * Return a list of {@link Auction}s won by the {@link User}
	 * corresponding to the given ID.
	 * To be won, an {@code auction} must satisfy the following constraints:
	 * - the given {@link User} must have placed the highest offer on that auction
	 * - the auction must be closed (and therefore expired)
	 */
	@NonNull
	public List<Auction> getWonAuctions(final int userId) throws SQLException {
		final String query = 
				    "SELECT A.id, items.code, items.name, A.maximum_offer, A.expire_seconds "
				  + "FROM items JOIN "
				  + 				"(SELECT auctions.id, auctions.item_id, P.maximum_offer, TIMESTAMPDIFF(SECOND, auctions.expire_timestamp, NOW()) AS expire_seconds "
				  + 				 "FROM auctions JOIN( "
				  + 									"SELECT offers.auction_id, O.maximum_offer "
				  + 									"FROM offers JOIN( "
				  + 														"SELECT auction_id, MAX(price) AS maximum_offer "
				  + 														"FROM offers "
				  + 														"GROUP BY auction_id "
				  +													") AS O ON (O.auction_id=offers.auction_id AND O.maximum_offer=offers.price) "
				  + 									"WHERE offers.user_id=? "
				  + 							  ") AS P ON P.auction_id=auctions.id "
				  + 				 "WHERE closed=1 "
				  + 		   ") AS A ON A.item_id=items.id "
				  + "ORDER BY expire_seconds ASC";
		
		try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
			statement.setInt(1, userId);

			try (final ResultSet result = statement.executeQuery()) {
				final List<Auction> auctions = new ArrayList<>();

				while (result.next()) {
					final Auction auction = createAuction(result);
					auctions.add(auction);
				}
				return auctions;
			}
		}
	}
	
	/**
	 * Return a list of {@link Auction}s that corresponds to valid {@code auctions},
	 * whose  {@link Item} {@code name} or {@code description} is matched (totally or partially) by the given search query.
	 * To be valid, an {@code auction} must satisfy the following constraints:
	 * - the requestor can't be also the owner
	 * - the auction must be open (not closed)
	 * - the auction can't be expired
	 * 
	 * NOTE: the search query is case insensitive.
	 */
	@NonNull
	public List<Auction> findValidAuctions(final int requestorId, @NonNull final String searchQuery) throws SQLException {
		final String query = 
				    "SELECT auctions.id, items.code, items.name, T.maximum_offer, TIMESTAMPDIFF(SECOND, NOW(), auctions.expire_timestamp) AS expire_seconds "
				  + "FROM auctions "
				  + 	"JOIN ( "
				  + 		"SELECT auctions.id, IFNULL(MAX(offers.price), 0.00) AS maximum_offer "
				  + 		"FROM auctions LEFT JOIN offers ON auctions.id=offers.auction_id "
				  + 		"GROUP BY auctions.id "
				  + 		") AS T ON auctions.id=T.id "
				  + 	"JOIN items ON auctions.item_id=items.id "
				  + "WHERE auctions.owner_id<>? AND auctions.closed=0  AND TIMESTAMPDIFF(SECOND, NOW(), auctions.expire_timestamp)>0 AND (items.name LIKE ? OR items.description LIKE ?) "
				  + "ORDER BY expire_seconds ASC";

		try (final PreparedStatement statement = this.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			statement.setInt(1, requestorId);
			
			final String escapedSearchQuery = "%" + searchQuery + "%";
			statement.setString(2, escapedSearchQuery);
			statement.setString(3, escapedSearchQuery);

			try (final ResultSet result = statement.executeQuery()) {
				final List<Auction> auctions = new ArrayList<>();

				while (result.next()) {
					final Auction auction = createAuction(result);
					auctions.add(auction);
				}
				return auctions;
			}
		}
	}

	/**
	 * Close the {@link Auction} corresponding to the given ID by updating the
	 * cooresponding field in the database.
	 */
	public void closeAuction(final int auctionId) throws SQLException {
		final String query = "UPDATE auctions SET closed=1 WHERE id=?";

		try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
			statement.setInt(1, auctionId);

			final int resultCode = statement.executeUpdate();

			if (resultCode != 1)
				throw new SQLException("Wrong query executed, " + resultCode + " rows affected");
		}
	}

	/**
	 * Insert into the database a new record corresponding to the given {@link AuctionDetails},
	 * then update the ID of the inserted {@link AuctionDetails}.
	 */
	public void insertAuction(@NonNull final AuctionDetails auction) throws SQLException {
		final String query = 
				  "INSERT INTO auctions (item_id, owner_id, base_price, minimum_rise, expire_timestamp, closed) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";

		try (final PreparedStatement statement = this.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			statement.setInt(1, auction.getItemId());
			statement.setInt(2, auction.getOwnerId());
			statement.setDouble(3, auction.getBasePrice());
			statement.setInt(4, auction.getMinimumRise());
			statement.setObject(5, auction.getExpireDateTime());
			statement.setBoolean(6, auction.isClosed());

			final int resultCode = statement.executeUpdate();
			
			if (resultCode != 1)
				throw new SQLException("Wrong query executed, " + resultCode + " rows affected");

			try (final ResultSet generatedKeys = statement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					final int id = generatedKeys.getInt(1);
					auction.setId(id);
				} else
					throw new SQLException("Auction insertion failed, no ID obtained");
			}
		}
	}

	/**
	 * Return the {@link AuctionDetails} belonging to the {@link Auction}
	 * corresponding to the given ID.
	 */
	@NonNull
	public AuctionDetails getAuctionDetails(final int auctionId) throws SQLException {
		final String query = 
				  "SELECT auctions.*, T.maximum_offer "
				+ "FROM auctions JOIN ( "
				+				"SELECT auctions.id, IFNULL(MAX(offers.price), 0.00) AS maximum_offer "
				+ 				"FROM auctions LEFT JOIN offers ON auctions.id=offers.auction_id "
				+ 				"GROUP BY auctions.id "
				+			") AS T ON auctions.id=T.id "
				+ "WHERE auctions.id=?";

		try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
			statement.setInt(1, auctionId);

			try (final ResultSet result = statement.executeQuery()) {

				if (result.next())
					return createAuctionDetails(result);
				throw new SQLException("Query returned an empty result");
			}
		}
	}

	/**
	 * Return the list of open {@link Auction}s belonging to the {@link User}
	 * corresponding to the given ID.
	 * 
	 * Open auctions can be in the past (expired but not yet closed) or in the future
	 * (not yet expired).
	 */
	@NonNull
	public List<Auction> getOpenAuctions(final int ownerId) throws SQLException {
		final String query = 
				   "SELECT auctions.id, items.code, items.name, T.maximum_offer, GREATEST(TIMESTAMPDIFF(SECOND, NOW(), auctions.expire_timestamp), 0) AS expire_seconds " 
				 + "FROM auctions " 
			     + 		"JOIN ( "
			     + 			"SELECT auctions.id, IFNULL(MAX(offers.price), 0.00) AS maximum_offer "
			     + 			"FROM auctions LEFT JOIN offers ON auctions.id=offers.auction_id "
			     + 			"GROUP BY auctions.id "
			     + 			") AS T ON auctions.id=T.id "
			     + 		"JOIN items ON auctions.item_id=items.id "
			     + "WHERE auctions.owner_id=? AND auctions.closed=0 "
			     + "ORDER BY auctions.expire_timestamp ASC";

		try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
			statement.setInt(1, ownerId);

			try (final ResultSet result = statement.executeQuery()) {
				final List<Auction> auctions = new ArrayList<>();

				while (result.next()) {
					final Auction auction = createAuction(result);
					auctions.add(auction);
				}
				return auctions;
			}
		}
	}
	
	/**
	 * Return the list of closed {@link Auction}s belonging to the {@link User}
	 * corresponding to the given ID.
	 * 
	 * Closed auctions can only be in the past (expired and closed).
	 */
	@NonNull
	public List<Auction> getClosedAuctions(final int ownerId) throws SQLException {
		final String query = 
				   "SELECT auctions.id, items.code, items.name, T.maximum_offer, TIMESTAMPDIFF(SECOND, auctions.expire_timestamp, NOW()) AS expire_seconds " 
				 + "FROM auctions " 
			     + 		"JOIN ( "
			     + 			"SELECT auctions.id, IFNULL(MAX(offers.price), 0.00) AS maximum_offer "
			     + 			"FROM auctions LEFT JOIN offers ON auctions.id=offers.auction_id "
			     + 			"GROUP BY auctions.id "
			     + 			") AS T ON auctions.id=T.id "
			     + 		"JOIN items ON auctions.item_id=items.id "
			     + "WHERE auctions.owner_id=? AND auctions.closed=1 "
			     + "ORDER BY auctions.expire_timestamp DESC";

		try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
			statement.setInt(1, ownerId);

			try (final ResultSet result = statement.executeQuery()) {
				final List<Auction> auctions = new ArrayList<>();

				while (result.next()) {
					final Auction auction = createAuction(result);
					auctions.add(auction);
				}
				return auctions;
			}
		}
	}

	/**
	 * Create an {@link AuctionDetails} with the given result.
	 */
	@NonNull
	private AuctionDetails createAuctionDetails(@NonNull final ResultSet result) throws SQLException {
		final int id = result.getInt(Values.Auction.ID);
		final int itemId = result.getInt(Values.Auction.ITEM_ID);
		final int ownerId = result.getInt(Values.Auction.OWNER_ID);
		final double basePrice = result.getDouble(Values.Auction.BASE_PRICE);
		final int minimumRise = result.getInt(Values.Auction.MINIMUM_RISE);
		final double maximumOffer = result.getDouble(Values.Auction.MAXIMUM_OFFER);
		final LocalDateTime expireDateTime = result.getObject(Values.Auction.EXPIRE_DATETIME_DB, LocalDateTime.class);
		final boolean closed = result.getBoolean(Values.Auction.CLOSED);

		return new AuctionDetails(id, itemId, ownerId, basePrice, minimumRise, maximumOffer, expireDateTime, closed);
	}

	/**
	 * Create an {@link Auction} with the given result.
	 */
	@NonNull
	private Auction createAuction(@NonNull final ResultSet result) throws SQLException {
		final int auctionId = result.getInt(Values.Auction.ID);
		final String itemCode = result.getString(Values.Item.CODE);
		final String itemName = result.getString(Values.Item.NAME);
		final double maximumOffer = result.getDouble(Values.Auction.MAXIMUM_OFFER);
		final double expireSeconds = result.getDouble(Values.Auction.EXPIRE_SECONDS);

		final Time timeLeft = Time.fromSeconds(expireSeconds);

		return new Auction(auctionId, itemCode, itemName, maximumOffer, timeLeft);
	}
}
