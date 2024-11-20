module org.example.jobaifinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;
    requires org.apache.pdfbox;
    requires java.sql;

    opens org.example.jobaifinal to javafx.fxml;
    exports org.example.jobaifinal;
}