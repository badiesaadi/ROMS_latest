import javafx.stage.Stage;

public class StageManager {
    private static Stage primaryStage;
    private static boolean isMaximized;
    private static double width;
    private static double height;

    public static void initialize(Stage stage) {
        primaryStage = stage;
        isMaximized = stage.isMaximized();
        width = stage.getWidth();
        height = stage.getHeight();

        stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
            isMaximized = newVal;
        });

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (!stage.isMaximized()) {
                width = newVal.doubleValue();
            }
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (!stage.isMaximized()) {
                height = newVal.doubleValue();
            }
        });
    }

    public static void applyStageSettings(Stage stage) {
        if (isMaximized) {
            stage.setMaximized(true);
        } else {
            stage.setWidth(width);
            stage.setHeight(height);
        }
    }
}