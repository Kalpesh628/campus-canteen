package com.canteen.model;

import java.math.BigDecimal;

/** One dish on the canteen menu. */
public class MenuItem {

    private int id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;   // e.g. Breakfast, Lunch, Snacks, Beverages
    private boolean veg;
    private boolean available; // false = temporarily out of stock / hidden from students
    private String imageUrl;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isVeg() { return veg; }
    public void setVeg(boolean veg) { this.veg = veg; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
