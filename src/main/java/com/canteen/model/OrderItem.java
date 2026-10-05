package com.canteen.model;

import java.math.BigDecimal;

/** One line of an order: item x qty, with the price frozen at order time. */
public class OrderItem {

    private int id;
    private int orderId;
    private int menuItemId;
    private String itemName; // denormalised for display
    private int qty;
    private BigDecimal priceAtOrder;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getMenuItemId() { return menuItemId; }
    public void setMenuItemId(int menuItemId) { this.menuItemId = menuItemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public BigDecimal getPriceAtOrder() { return priceAtOrder; }
    public void setPriceAtOrder(BigDecimal priceAtOrder) { this.priceAtOrder = priceAtOrder; }

    /** Derived line total (price x qty); kept in the model so JSPs never do math. */
    public BigDecimal getLineTotal() {
        if (priceAtOrder == null) return BigDecimal.ZERO;
        return priceAtOrder.multiply(BigDecimal.valueOf(qty));
    }

    public BigDecimal lineTotal() {
        return priceAtOrder.multiply(BigDecimal.valueOf(qty));
    }
}
