module com.napolitanoveroni.expirationdate {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.napolitanoveroni.expirationdate to javafx.fxml;
    exports com.napolitanoveroni.expirationdate;
}