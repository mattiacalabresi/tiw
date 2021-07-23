package model.beans;

import org.eclipse.jdt.annotation.NonNull;

import model.Time;

/**
 * This class represents a lightweight version of
 * an Auction, containing only key values.
 * It is intended to be used as a sort of 'preview'
 * of what a complete Auction looks like.
 * 
 * All parameters that refers to currency are
 * intended to be in euros (EUR).
 * 
 * To see a detailed implementation of an Auction,
 * see {@link AuctionDetails}.
 */
public class Auction {
	/**
	 * The identifier of the Auction.
	 */
	private final int id;
	/**
	 * The code of the Item belonging 
	 * to the Auction.
	 */
	@NonNull
	private final String itemCode;
	/**
	 * The name of the Item belonging 
	 * to the Auction.
	 */
	@NonNull
	private final String itemName;
	/**
	 * The maximum Offer placed for
	 * this Auction so far.
	 * 
	 * If no Offers have been placed,
	 * this value defaults to 0f.
	 */
	private final double maximumOffer;
	/**
	 * The amount of time left before the
	 * Auction will expire.
	 */
	@NonNull
	private final Time timeLeft;
	
	/**
	 * Whether the auction is expired or not.
	 */
	private final boolean expired;

	public Auction(final int auctionId, @NonNull final String itemCode, @NonNull final String itemName, final double maximumOffer,
			@NonNull final Time timeLeft) {
		this.id = auctionId;
		this.itemCode = itemCode;
		this.itemName = itemName;
		this.maximumOffer = maximumOffer;
		this.timeLeft = timeLeft;
		this.expired = this.timeLeft.isZero();
	}
	
	public int getId() {
		return this.id;
	}

	@NonNull
	public String getItemCode() {
		return this.itemCode;
	}
	
	@NonNull
	public String getItemName() {
		return this.itemName;
	}
	
	public double getMaximumOffer() {
		return this.maximumOffer;
	}
	
	@NonNull
	public Time getTimeLeft() {
		return this.timeLeft;
	}
	
	public boolean isExpired() {
		return this.expired;
	}
}
