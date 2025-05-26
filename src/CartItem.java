 
public class CartItem {
    private MenuItem menuItem;
    private int quantity;
    private double total;

    public CartItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.total = menuItem.getPrice() * quantity;
    }

    public MenuItem getMenuItem() { return menuItem; }
    public int getQuantity() { return quantity; }
    public double getTotal() { return total; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.total = menuItem.getPrice() * quantity;
    }
}