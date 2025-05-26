 

public class MenuItemIngredient {
    private MenuItem menuItem;
    private Ingredient ingredient;
    private double amount;
    
    public MenuItemIngredient(MenuItem menuItem, Ingredient ingredient, double amount) {
        this.menuItem = menuItem;
        this.ingredient = ingredient;
        this.amount = amount;
    }
    
    public MenuItem getMenuItem() {
        return menuItem;
    }
    
    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }
    
    public Ingredient getIngredient() {
        return ingredient;
    }
    
    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
} 