package com.napolitanoveroni.expirationdate;

import java.util.TimeZone;

public class ConfigDB {
    public static final String JDBC_Driver = "com.mysql.cj.jdbc.Driver";

    public static final String JDBC_URL = "jdbc:mysql://localhost:3306/jdbc_schema?user=username&password" +
            "=Password&serverTimezone=" + TimeZone.getDefault().getID();
}
