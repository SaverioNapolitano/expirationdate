package com.napolitanoveroni.expirationdate;

import javafx.scene.control.Alert;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.TimeZone;

public class UtilsDB {

    public static LocalDate convertSQLDateToLocalDate(Date SQLDate) {
        java.util.Date date = new java.util.Date(SQLDate.getTime());
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static void onSQLException(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}
