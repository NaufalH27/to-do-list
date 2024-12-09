package org.uns.todolist.helper;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class ResponsiveHelper {
     public static void animateResize(Region box, double newHeight, Duration duration) {
        Timeline timeline = new Timeline();
        KeyValue maxHeightKeyValue = new KeyValue(box.maxHeightProperty(), newHeight);
        KeyValue minHeightKeyValue = new KeyValue(box.minHeightProperty(), newHeight);
        KeyFrame keyFrame = new KeyFrame(duration, maxHeightKeyValue, minHeightKeyValue);    
        timeline.getKeyFrames().add(keyFrame);    
        timeline.play();
    }
}
