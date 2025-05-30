import javafx.stage.Stage;

public class StageManager {
    private static Stage primaryStage; 
    private static boolean isMaximized; 
    private static double width; 
    private static double height; 

    /**
    initializes the StageManager with the given stage and
      Sets up listeners to track changes in the stage's maximized state, width, and height.
     */
    public static void initialize(Stage stage) {
        primaryStage = stage; 
        isMaximized = stage.isMaximized(); 
        width = stage.getWidth(); 
        height = stage.getHeight(); 

        stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
            isMaximized = newVal;
        });

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (!stage.isMaximized()) { // Only update width if the stage is not maximized
                width = newVal.doubleValue();
            }
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (!stage.isMaximized()) { 
                height = newVal.doubleValue();
            }
        });
    }

    /**
     * Applies the saved stage settings (maximized state, width, and height) to the given stage.
          */
    public static void applyStageSettings(Stage stage) {
        if (isMaximized) {
            stage.setMaximized(true);
        } else {
            // Restore width and height if not maximized
            stage.setWidth(width);
            stage.setHeight(height);
        }
    }
}