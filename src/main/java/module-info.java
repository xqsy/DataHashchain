module io.hashchain {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    exports io.hashchain.core;
    opens io.hashchain.core to javafx.fxml;

    exports io.hashchain.ui;
    opens io.hashchain.ui to javafx.fxml;
}