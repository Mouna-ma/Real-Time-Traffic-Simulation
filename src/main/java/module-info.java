module groupfour.trafficsim {
    requires javafx.controls;
    requires javafx.fxml;
    requires traas;
    requires org.apache.logging.log4j;
    requires java.xml;

    exports groupfour.trafficsim;
    exports groupfour.trafficsim.sim;
    exports groupfour.trafficsim.ui;
}