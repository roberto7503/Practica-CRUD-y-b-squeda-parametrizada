module org.example.proyectocrudg3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens org.example.proyectocrudg3 to javafx.fxml;
    opens org.example.proyectocrudg3.controller to javafx.fxml;
    opens org.example.proyectocrudg3.model to javafx.base;

    exports org.example.proyectocrudg3;
    exports org.example.proyectocrudg3.controller;
    exports org.example.proyectocrudg3.model;
}