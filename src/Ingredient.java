 
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Ingredient {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty quantity;
    private final SimpleDoubleProperty minQuantity;
    private final SimpleStringProperty unit;
    private final SimpleStringProperty category;
    
    public Ingredient(int id, String name, double quantity, double minQuantity, String unit, String category) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.quantity = new SimpleDoubleProperty(quantity);
        this.minQuantity = new SimpleDoubleProperty(minQuantity);
        this.unit = new SimpleStringProperty(unit);
        this.category = new SimpleStringProperty(category);
    }
    
    // Getters and setters using JavaFX properties for TableView binding
    
    public int getId() {
        return id.get();
    }
    
    public SimpleIntegerProperty idProperty() {
        return id;
    }
    
    public String getName() {
        return name.get();
    }
    
    public SimpleStringProperty nameProperty() {
        return name;
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public double getQuantity() {
        return quantity.get();
    }
    
    public SimpleDoubleProperty quantityProperty() {
        return quantity;
    }
    
    public void setQuantity(double quantity) {
        this.quantity.set(quantity);
    }
    
    public double getMinQuantity() {
        return minQuantity.get();
    }
    
    public SimpleDoubleProperty minQuantityProperty() {
        return minQuantity;
    }
    
    public void setMinQuantity(double minQuantity) {
        this.minQuantity.set(minQuantity);
    }
    
    public String getUnit() {
        return unit.get();
    }
    
    public SimpleStringProperty unitProperty() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit.set(unit);
    }
    
    public String getCategory() {
        return category.get();
    }
    
    public SimpleStringProperty categoryProperty() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category.set(category);
    }
    
    public boolean isLowStock() {
        return quantity.get() <= minQuantity.get();
    }
    
    public void decreaseQuantity(double amount) {
        double newQuantity = quantity.get() - amount;
        if (newQuantity < 0) {
            newQuantity = 0;
        }
        quantity.set(newQuantity);
    }
    
    public void increaseQuantity(double amount) {
        quantity.set(quantity.get() + amount);
    }
} 