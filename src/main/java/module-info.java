module com.napolitanoveroni.expirationdate {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires net.sf.biweekly;
    requires java.desktop;

    opens com.napolitanoveroni.expirationdate to javafx.fxml;
    exports com.napolitanoveroni.expirationdate;
}