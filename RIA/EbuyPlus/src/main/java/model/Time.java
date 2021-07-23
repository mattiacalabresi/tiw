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
	private final int days;
	private final int hours;

	private Time(final double seconds) {
		this.seconds = seconds;
		this.days = (int) Math.floor(seconds / SECONDS_IN_ONE_DAY);
		this.hours = (int) Math.floor(seconds % (SECONDS_IN_ONE_DAY) / SECONDS_IN_ONE_HOUR);
	}

	/**
	 * Return a new {@link Time} instance equivalent to the amount of given {@code seconds}.
	 */
	@NonNull
	public static Time fromSeconds(final double seconds) {
		return new Time(seconds);
	}

	public int getDays() {
		return this.days;
	}

	public int getHours() {
		return this.hours;
	}
	
	public boolean isZero() {
		return this.seconds == 0d;
	}
}