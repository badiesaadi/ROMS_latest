public class MenuItem {
    private int itemId;
    private String title;
    private double price;
    private int quantity;
    private String categoryTitle;
    private String imagePath;

    // Default constructor
    public MenuItem() {
        this.quantity = 0;
    }

    // New constructor for database operations
    public MenuItem(int itemId, String title, double price, String categoryTitle, String imagePath) {
        this.itemId = itemId;
        this.title = title;
        this.price = price;
        this.quantity = 0;
        this.categoryTitle = categoryTitle;
        this.imagePath = imagePath;
    }

    // // Legacy constructor for backward compatibility
    // public MenuItem(int itemId, String title, double price, String categoryTitle, String imagePath) {
    //     this(itemId, title, price, categoryTitle, imagePath, 0);
    // }

    // Getters and setters
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Legacy getters for backward compatibility
    public int getId() {
        return getItemId();
    }

    public String getName() {
        return getTitle();
    }

    public String getCategory() {
        return getCategoryTitle();
    }

    public void setId(int id) {
        setItemId(id);
    }

    public void setName(String name) {
        setTitle(name);
    }

    public void setCategory(String category) {
        setCategoryTitle(category);
    }
}
