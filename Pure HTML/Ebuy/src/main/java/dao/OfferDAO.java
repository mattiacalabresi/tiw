package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import controller.utils.Values;
import model.beans.Offer;
import model.beans.PlaceableOffer;

/**
 * Helper class that interacts to the database
 * and handles all the procedures regarding a
 * {@link Offer}.
 */
public class OfferDAO {
	@NonNull
	private final Connection connection;

	private OfferDAO(@NonNull final Connection connection) {
		this.connection = connection;
	}

	@NonNull
	public static OfferDAO from(@NonNull final Connection connection) {
		return new OfferDAO(connection);
	}
	
	/**
	 * Insert into the database a new tuple corresponding to the given {@link PlaceableOffer}.
	 */
	public void placeOffer(@NonNull final PlaceableOffer offer) throws SQLException {
		final String query = "INSERT INTO offers (auction_id, user_id, price, timestamp) VALUES (?, ?, ?, ?)";

		try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
			statement.setInt(1, offer.getAuctionId());
			statement.setInt(2, offer.getUserId());
			statement.setDouble(3, offer.getOfferedPrice());
			statement.setObject(4, offer.getOfferDateTime());

			final int resultCode = statement.executeUpdate();

			if (resultCode != 1)
				throw new SQLException("Wrong query executed, " + resultCode + " rows affected");
		}
	}

	/**
	 * Return the list of {@link Offer}s corresponding to the given ID.
	 */
	@NonNull
	public List<Offer> getOffers(final int auctionId) throws SQLException {
		final List<Offer> offers = new ArrayList<>();
		final String query = 
				 "SELECT users.username, offers.price, offers.timestamp "
				+ "FROM users JOIN offers ON users.id=offers.user_id "
				+ "WHERE offers.auction_id=? "
				+ "ORDER BY offers.timestamp DESC";

		try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
			statement.setInt(1, auctionId);

			try (final ResultSet result = statement.executeQuery()) {
				while(result.next()) {
					final Offer offer = createOffer(result);
					offers.add(offer);
				}
				return offers;
			}
		}
	}
	
	/**
	 * Return the ID of the last {@link User} that placed an {@link Offer}
	 * for the {@link Auction} corresponding to the given ID.
	 */
	@NonNull
	public int getLastOffererId(final int auctionId) throws SQLException {
		final String query =
				  "SELECT offers.user_id "
				+ "FROM offers JOIN ( "
				+ "					SELECT auction_id, MAX(timestamp) as max_timestamp "
				+ "                    FROM offers "
				+ "                    GROUP BY auction_id "
				+ "				) AS T ON offers.auction_id=T.auction_id AND offers.timestamp=T.max_timestamp "
				+ "WHERE offers.auction_id=? "
				+ "UNION "
				+ "SELECT -1 as user_id "
				+ "LIMIT 1";
		
		try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
			statement.setInt(1, auctionId);

			try (final ResultSet result = statement.executeQuery()) {

				if (result.next())
					return result.getInt(Values.Session.USER_ID);
				throw new SQLException("Query returned an empty result");
			}
		}
	}
	
	/**
	 * Create an {@link Offer} with the given result.
	 */
	@NonNull
	private Offer createOffer(@NonNull final ResultSet result) throws SQLException {
		final String userName = result.getString(Values.User.USERNAME);
		final double price = result.getDouble(Values.Offer.PRICE);
		final LocalDateTime dateTime = result.getObject(Values.Offer.TIMESTAMP, LocalDateTime.class);
		
		return new Offer(userName, price, dateTime);
	}
}
