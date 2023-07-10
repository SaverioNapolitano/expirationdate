package com.napolitanoveroni.expirationdate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * The DateUtils class provides static utility methods for converting between Java Date and Time classes.
 *
 * @author SaverioNapolitano, MatteV02
 * @version 2023.07.10
 */
public class DateUtils {

	/**
	 * Converts a LocalDate object to a Date object.
	 *
	 * @param localDate the LocalDate object to convert
	 *
	 * @return the corresponding Date object
	 */
	public static Date asDate(LocalDate localDate) {
		return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Converts a LocalDateTime object to a Date object.
	 *
	 * @param localDateTime the LocalDateTime object to convert
	 *
	 * @return the corresponding Date object
	 */
	public static Date asDate(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Converts a Date object to a LocalDate object.
	 *
	 * @param date the Date object to convert
	 *
	 * @return the corresponding LocalDate object
	 */
	public static LocalDate asLocalDate(Date date) {
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
	}

	/**
	 * Converts a Date object to a LocalDateTime object.
	 *
	 * @param date the Date object to convert
	 *
	 * @return the corresponding LocalDateTime object
	 */
	public static LocalDateTime asLocalDateTime(Date date) {
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
}