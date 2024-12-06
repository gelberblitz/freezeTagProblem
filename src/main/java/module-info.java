module org.example.bachelorarbeit_fx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens org.example.bachelorarbeit_fx to javafx.fxml;
    exports org.example.bachelorarbeit_fx;
}