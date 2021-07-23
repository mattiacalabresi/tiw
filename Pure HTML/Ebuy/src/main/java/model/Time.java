package model;

import org.eclipse.jdt.annotation.NonNull;

/**
 * This class represents the time
 * in the format "days and hours".
 * It is intended to be used as an
 * indiction of the time left before an
 * Auction expires.
 */
public class Time {
	private static final int SECONDS_IN_ONE_HOUR = 3600;
	private static final int SECONDS_IN_ONE_DAY = SECONDS_IN_ONE_HOUR * 24;
	
	private final double seconds;

	private Time(final double seconds) {
		this.seconds = seconds;
	}

	/**
	 * Return a new {@link Time} instance equivalent to the amount of given {@code seconds}.
	 */
	@NonNull
	public static Time fromSeconds(final double seconds) {
		return new Time(seconds);
	}

	public int getDays() {
		return (int) Math.floor(seconds / SECONDS_IN_ONE_DAY);
	}

	public int getHours() {
		return (int) Math.floor(seconds % (SECONDS_IN_ONE_DAY) / SECONDS_IN_ONE_HOUR);
	}
	
	public boolean isZero() {
		return this.seconds == 0d;
	}
}