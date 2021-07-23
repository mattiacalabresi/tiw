package model.beans;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNull;

/**
 * This class represents an Offer made for
 * an Auction.
 * 
 * All parameters that refers to currency are
 * intended to be in euros (EUR).
 */
public class Offer {
	/**
	 * The name of the User
	 * that placed the offer.
	 */
	@NonNull
	private final String userName;
	/**
	 * The amount of money offered.
	 */
	private final double price;
	/**
	 * The date and time the Offer
	 * was made at.
	 */
	@NonNull
	private final LocalDateTime dateTime;

	public Offer(@NonNull final String userName, final double price, @NonNull final LocalDateTime dateTime) {
		this.userName = userName;
		this.price = price;
		this.dateTime = dateTime;
	}

	@NonNull
	public String getUserName() {
		return this.userName;
	}

	public double getPrice() {
		return this.price;
	}

	@NonNull
	public LocalDateTime getDateTime() {
		return this.dateTime;
	}
	
	@NonNull
	public String getDateTimeFormatted() {
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
		return this.dateTime.format(formatter);
	}
}
