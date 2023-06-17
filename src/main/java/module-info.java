module com.napolitanoveroni.expirationdate {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires org.mnode.ical4j.core;

    opens com.napolitanoveroni.expirationdate to javafx.fxml;
    exports com.napolitanoveroni.expirationdate;
}