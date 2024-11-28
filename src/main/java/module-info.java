module org.uns.todolist {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.google.gson;
    opens org.uns.todolist to javafx.fxml;
    exports org.uns.todolist;
}