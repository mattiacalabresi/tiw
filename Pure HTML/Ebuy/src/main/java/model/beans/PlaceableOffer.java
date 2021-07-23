package model.beans;

import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNull;

public class PlaceableOffer {
	private final int auctionId;
	private final int userId;
	private final double offeredPrice;
	@NonNull
	private final LocalDateTime offerDateTime;

	public PlaceableOffer(final int auctionId, final int userId, final double offeredPrice, @NonNull final LocalDateTime offerDateTime) {

		this.auctionId = auctionId;
		this.userId = userId;
		this.offeredPrice = offeredPrice;
		this.offerDateTime = offerDateTime;
	}

	public int getAuctionId() {
		return this.auctionId;
	}

	public int getUserId() {
		return this.userId;
	}

	public double getOfferedPrice() {
		return this.offeredPrice;
	}

	@NonNull
	public LocalDateTime getOfferDateTime() {
		return this.offerDateTime;
	}
}
