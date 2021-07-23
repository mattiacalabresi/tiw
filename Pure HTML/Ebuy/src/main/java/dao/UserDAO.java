package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.eclipse.jdt.annotation.NonNull;

import controller.utils.Values;
import exception.InvalidCredentialsException;
import exception.NoBuyerException;
import model.beans.User;

/**
 * Helper class that interacts to the database
 * and handles all the procedures regarding a
 * {@link User}.
 */
public class UserDAO {
	@NonNull
	private final Connection connection;

	private UserDAO(@NonNull final Connection connection) {
		this.connection = connection;
	}
	
	@NonNull
	public static UserDAO from(@NonNull final Connection connection) {
		return new UserDAO(connection);
	}

	/**
	 * Return the {@link User} that is also the winner of the
	 * {@link Auction} (and, therefore, the buyer of the item)
	 * corresponding to the given ID.
	 * 
	 * If the {@link Auction} has no winner (eg. no one placed any offer),
	 * a {@link NoBuyerException} will be thrown.
	 */
	@NonNull
	public User getBuyer(final int auctionId) throws SQLException, NoBuyerException {
		final String query = 
					"(SELECT username, shipping_address "
				+    "FROM users "
				+    "WHERE id = (SELECT users.id "
				+			     "FROM users JOIN offers on users.id=offers.user_id "
				+ 				 "WHERE offers.auction_id=? AND offers.price=(SELECT MAX(price) "
				+																     "FROM offers "
				+ 																	 "WHERE auction_id=?"
				+ 																 	 ")"
				+ 				 ")"
				+ 	 ") UNION "
				+ 	 "SELECT 'null', 'null' "
				+ 	 "LIMIT 1";

		try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
			statement.setInt(1, auctionId);
			statement.setInt(2, auctionId);

			try (final ResultSet result = statement.executeQuery()) {
				if (result.next()) {
					final User user = createUser(result);
					
					if(user.getName().equals(Values.User.EMPTY_FIELD) || user.getShippingAddress().equals(Values.User.EMPTY_FIELD))
						throw new NoBuyerException("No buyer found for the given Auction");
					
					return user;
				}
				throw new SQLException("No auction corresponding to the given ID: " + auctionId);
			}
		}
	}
	
	/**
	 * Return the ID of the {@link User} associated with the given credentials.
	 * @throws SQLException 
	 */
	public int getUserId(@NonNull final String username, @NonNull final String password) throws InvalidCredentialsException, SQLException {
		final String query = "SELECT id FROM users WHERE username=? AND password=?";

		try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
			statement.setString(1, username);
			statement.setString(2, password);

			try (final ResultSet result = statement.executeQuery()) {

				if (result.next()) {
					final int id = result.getInt(Values.User.ID);

					if (id == 0)
						throw new InvalidCredentialsException("No ID found that matches the given credentials");

					return id;
				}
				throw new InvalidCredentialsException("Wrong credentials provided");
			}
		}
	}

	/**
	 * Create an {@link User} from the given result.
	 */
	@NonNull
	private User createUser(@NonNull final ResultSet result) throws SQLException {
		final String username = result.getString(Values.User.USERNAME);
		final String shippingAddress = result.getString(Values.User.SHIPPING_ADDRESS);
		
		return new User(username, shippingAddress);
	}
}
