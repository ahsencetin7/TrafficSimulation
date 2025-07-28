module com.example.trafficlight {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.logging;

    exports org.example.traf;

    opens org.example.traf to javafx.fxml;
    exports org.example.traf.view;
    opens org.example.traf.view to javafx.fxml;
    exports org.example.traf.controller;
    opens org.example.traf.controller to javafx.fxml;
    exports org.example.traf.model;
    opens org.example.traf.model to javafx.fxml;
    exports org.example.traf.util;
    opens org.example.traf.util to javafx.fxml;
}