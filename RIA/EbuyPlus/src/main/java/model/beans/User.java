package model.beans;

import org.eclipse.jdt.annotation.NonNull;

/**
 * This class represents a User, only in the
 * form of a winner of an Auction.
 */
public class User {
	/**
	 * The name (username) of the user.
	 */
	@NonNull
	private final String name;
	/**
	 * The address to which the Item won through the
	 * Auction has to be sent to.
	 */
	@NonNull
	private final String shippingAddress;

	public User(@NonNull final String name, @NonNull final String shippingAddress) {
		this.name = name;
		this.shippingAddress = shippingAddress;
	}

	@NonNull
	public String getName() {
		return this.name;
	}

	@NonNull
	public String getShippingAddress() {
		return this.shippingAddress;
	}
}
