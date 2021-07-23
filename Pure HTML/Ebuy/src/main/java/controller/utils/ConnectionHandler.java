package controller.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Factory class used to create and destroy
 * a {@link Connection}.
 */
public class ConnectionHandler {
	@NonNull
	private static final String DB_DRIVER = "dbDriver";
	@NonNull
	private static final String DB_URL = "dbUrl";
	@NonNull
	private static final String DB_USER = "dbUser";
	@NonNull
	private static final String DB_PASSWORD = "dbPassword";

	private ConnectionHandler() {

	}

	/**
	 * Create a new {@link Connection} using the parameters stored
	 * into the 'web.xml' file, retrieved by the given {@link ServletContext}.
	 */
	@NonNull
	public static Connection createConnection(@NonNull final ServletContext context) throws UnavailableException {
		try {
			final String driver = context.getInitParameter(DB_DRIVER);
			final String url = context.getInitParameter(DB_URL);
			final String user = context.getInitParameter(DB_USER);
			final String password = context.getInitParameter(DB_PASSWORD);

			Class.forName(driver);
			return DriverManager.getConnection(url, user, password);
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
			throw new UnavailableException("Unable to load database driver");
		} catch (final SQLException e) {
			e.printStackTrace();
			throw new UnavailableException("Unable to establish database connection");
		}
	}

	/**
	 * Close the given {@link Connection}, if exists.
	 */
	public static void closeConnection(@NonNull final Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
