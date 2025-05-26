import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SplashController {

    @FXML
    private ImageView logoImage;

    @FXML
    public void initialize() {
        Image logo = new Image(getClass().getResourceAsStream("/images/logo.jpg")); 
        logoImage.setImage(logo);
    }
}
