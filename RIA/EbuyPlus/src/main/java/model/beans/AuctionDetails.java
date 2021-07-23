package model.beans;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNull;

/**
 * This class represents an Auction with all of its
 * attributes.
 * 
 * All parameters that refers to currency are
 * intended to be in euros (EUR).
 *
 * For a lightweight version of this class,
 * see {@link Auction}.
 */
public class AuctionDetails {
	private static final int NULL_ID = -1;

	/**
	 * The identifier of the Auction.
	 */
	private int id;
	/**
	 * The identifier of the Item
	 * to which the Auciton refers to.
	 */
	private final int itemId;
	/**
	 * The identifier of the owner that created
	 * the Auction.
	 */
	private final int ownerId;
	/**
	 * The starting price of the Auction.
	 */
	private final double basePrice;
	/**
	 * The minimum amount an offer must differ
	 * from the previous one.
	 * 
	 * If an Offer has a price of 10 and the
	 * minimum rise is 12, the next Offer must
	 * have a price of at leas 10+2=12.
	 */
	private final int minimumRise;
	/**
	 * The maximum Offer placed for
	 * this Auction so far.
	 * 
	 * If no Offers have been placed,
	 * this value defaults to 0f.
	 * 
	 * If the auction
	 * has been closed, its value corresponds
	 * to the final price (that is, the price the
	 * winner has to pay).
	 */
	private final double maximumOffer;
	/**
	 * The minimum offer a User can place for this Auction.
	 * If no offers have been placed, this value is simply the
	 * sum of {@code basePrice} and {@code minimumRise}.
	 * If, instead, an Auction has some Offers, this value is computed
	 * as the sum of the largest offered price and {@code minimumRise}.
	 */
	private final double minimumOffer;
	/**
	 * The date and time at which the Auction will expire.
	 * Expired auctions do not close automatically (this needs to
	 * be done manually by the User), but if an Auction is expired,
	 * no Offers can be placed anymore.
	 */
	@NonNull
	private final LocalDateTime expireDateTime;
	/**
	 * Whether or not the Auction is closed.
	 * An open Auction can accept offers (only if it has not expired)
	 * while, a closed Auction cannot accept Offers.
	 */
	private final boolean closed;
	
	/**
	 * Whether the current Auction is expired or not.
	 */
	private final boolean expired;

	public AuctionDetails(final int id, final int itemId, final int ownerId, final double basePrice,
			final int minimumRise, final double maximumOffer, @NonNull final LocalDateTime expireDateTime, final boolean closed) {

		this.id = id;
		this.itemId = itemId;
		this.ownerId = ownerId;
		this.basePrice = basePrice;
		this.minimumRise = minimumRise;
		this.maximumOffer = maximumOffer;
		this.minimumOffer = Math.max(basePrice, maximumOffer) + minimumRise;
		this.expireDateTime = expireDateTime;
		this.closed = closed;
		this.expired = LocalDateTime.now().isAfter(expireDateTime);
		
	}

	public AuctionDetails(final int itemId, @NonNull final int ownerId, final double basePrice,
			final int minimumRise, @NonNull final LocalDateTime expireDateTime) {

		this(NULL_ID, itemId, ownerId, basePrice, minimumRise, 0.0, expireDateTime, false);
	}

	public void setId(final int id) {
		this.id = id;
	}

	public int getId() {
		if (this.id == NULL_ID)
			throw new NullPointerException("Attempting to retrieve a null ID");

		return this.id;
	}

	public int getItemId() {
		return this.itemId;
	}

	public int getOwnerId() {
		return this.ownerId;
	}

	public double getBasePrice() {
		return this.basePrice;
	}

	public int getMinimumRise() {
		return this.minimumRise;
	}
	
	public double getMaximumOffer() {
		return this.maximumOffer;
	}

	public double getMinimumOffer() {
		return this.minimumOffer;
	}

	@NonNull
	public LocalDateTime getExpireDateTime() {
		return this.expireDateTime;
	}
	
	@NonNull
	public String getExpireDateTimeFormatted() {
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
		return this.expireDateTime.format(formatter);
	}

	public boolean isClosed() {
		return this.closed;
	}
	
	public boolean isExpired() {
		return this.expired;
	}
}
