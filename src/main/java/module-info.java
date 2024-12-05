module org.uns.todolist {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.google.gson;
    requires javafx.base;
    requires org.kordamp.ikonli.javafx; 
    requires org.kordamp.ikonli.fontawesome5;
    opens org.uns.todolist to javafx.fxml;
    opens org.uns.todolist.models to com.google.gson;
    exports org.uns.todolist;
}