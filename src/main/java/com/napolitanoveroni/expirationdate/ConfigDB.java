package com.napolitanoveroni.expirationdate;

import java.util.TimeZone;

/**
 * The ConfigDB class provides configuration information for the database connection.
 *
 * @author SaverioNapolitano, MatteV02
 * @version 2023.07.10
 */
public class ConfigDB {
	/**
	 * The JDBC driver class name for MySQL.
	 */
	public static final String JDBC_Driver = "com.mysql.cj.jdbc.Driver";

	/**
	 * The JDBC URL for connecting to the MySQL database.
	 * The URL includes the database name, username, password, and the server timezone.
	 */
	public static final String JDBC_URL =
		"jdbc:mysql://localhost:3306/jdbc_schema?user=username&password" + "=Password&serverTimezone=" + TimeZone.getDefault().getID();
}
