package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.jdt.annotation.NonNull;

import controller.utils.Values;
import model.beans.Item;

/**
 * Helper class that interacts to the database
 * and handles all the procedures regarding a
 * {@link Item}.
 */
public class ItemDAO {
	@NonNull
	private final Connection connection;

	private ItemDAO(@NonNull final Connection connection) {
		this.connection = connection;
	}

	@NonNull
	public static ItemDAO from(@NonNull final Connection connection) {
		return new ItemDAO(connection);
	}

	/**
	 * Insert in the database a new record corresponding to the given {@link item},
	 * then update the {@link Item} with the proper ID.
	 */
	public void insertItem(@NonNull final Item item) throws SQLException {
		final String query = "INSERT INTO items (code, name, description, image_name) VALUES (?, ?, ?, ?)";

		try (final PreparedStatement statement = this.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

			statement.setString(1, item.getCode());
			statement.setString(2, item.getName());
			statement.setString(3, item.getDescription());
			statement.setString(4, item.getImageName());

			final int resultCode = statement.executeUpdate();

			if (resultCode != 1)
				throw new SQLException("Wrong query executed, " + resultCode + " rows affected");

			try (final ResultSet generatedKeys = statement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					final int id = generatedKeys.getInt(1);
					item.setId(id);
				} else
					throw new SQLException("Item insertion failed, no ID obtained");
			}
		}
	}

	/**
	 * Return the {@link Item} corresponding to the given ID.
	 */
	@NonNull
	public Item getItem(final int id) throws SQLException {
		final String query = "SELECT * FROM items WHERE id=?";

		try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
			statement.setInt(1, id);

			try (final ResultSet result = statement.executeQuery()) {
				if(result.next())
					return createItem(result);
				throw new SQLException("No Item corresponding to the given ID: " + id);
			}
		}
	}

	/**
	 * Create an {@link Item} with the given result.
	 */
	@NonNull
	private Item createItem(@NonNull final ResultSet result) throws SQLException {
		final int id = result.getInt(Values.Item.ID);
		final String code = result.getString(Values.Item.CODE);
		final String name = result.getString(Values.Item.NAME);
		final String description = result.getString(Values.Item.DESCRIPTION);
		final String imageName = result.getString(Values.Item.IMAGE_NAME);

		return new Item(id, code, name, description, imageName);
	}
}
