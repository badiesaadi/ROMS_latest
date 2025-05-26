 

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class MenuItemController {
    @FXML
    private VBox menuCardContainer;

    @FXML
    private ImageView menuImage;

    @FXML
    private Label menuName;

    @FXML
    private Label menuPrice;

    @FXML
    private Spinner<Integer> addToOrder;

    @FXML
    private Button submitQuantityBtn;
}
