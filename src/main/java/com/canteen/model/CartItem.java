package com.canteen.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * One line in the shopping cart. Lives ONLY in the HTTP session
 * (session attribute "cart", a Map&lt;Integer, CartItem&gt; keyed by menu item id)
 * - never in the database. Serializable because sessions may be persisted.
 */
public class CartItem implements Serializable {

    private int menuItemId;
    private String name;
    private BigDecimal price;
    private boolean veg;
    private int qty;

    public CartItem() {}

    public CartItem(int menuItemId, String name, BigDecimal price, boolean veg, int qty) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.price = price;
        this.veg = veg;
        this.qty = qty;
    }

    public int getMenuItemId() { return menuItemId; }
    public void setMenuItemId(int menuItemId) { this.menuItemId = menuItemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public boolean isVeg() { return veg; }
    public void setVeg(boolean veg) { this.veg = veg; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public BigDecimal lineTotal() {
        return price.multiply(BigDecimal.valueOf(qty));
    }
}
