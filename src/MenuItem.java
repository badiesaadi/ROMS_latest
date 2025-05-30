public class MenuItem {
    private int itemId;
    private String title;
    private double price;
    private String categoryTitle;
    private String imagePath;

    // Default constructor
    public MenuItem() {
    }

    // New constructor for database operations
    public MenuItem(int itemId, String title, double price, String categoryTitle, String imagePath) {
        this.itemId = itemId;
        this.title = title;
        this.price = price;
        this.categoryTitle = categoryTitle;
        this.imagePath = imagePath;
    }

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
