package com.canteen.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A student order. Lifecycle:
 * PLACED -> ACCEPTED -> PREPARING -> READY -> PICKED_UP
 *    \-> REJECTED (from PLACED or ACCEPTED, with a reason)
 *
 * price_at_order is snapshotted per line item so later menu price changes
 * never rewrite history.
 */
public class Order {

    public static final String PLACED = "PLACED";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String PREPARING = "PREPARING";
    public static final String READY = "READY";
    public static final String PICKED_UP = "PICKED_UP";
    public static final String REJECTED = "REJECTED";

    private int id;
    private int userId;
    private String userName;   // denormalised for admin list views
    private int slotId;
    private PickupSlot slot;   // populated for detail views
    private String status;
    private BigDecimal total;
    private String paymentMode = "PAY_AT_CANTEEN";
    private String note;
    private String rejectReason;
    private LocalDateTime createdAt;
    private List<OrderItem> items = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }

    public PickupSlot getSlot() { return slot; }
    public void setSlot(PickupSlot slot) { this.slot = slot; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    /** Legal next statuses for the admin workflow; enforced server-side. */
    public static List<String> allowedNext(String current) {
        return switch (current) {
            case PLACED -> List.of(ACCEPTED, REJECTED);
            case ACCEPTED -> List.of(PREPARING, REJECTED);
            case PREPARING -> List.of(READY);
            case READY -> List.of(PICKED_UP);
            default -> List.of(); // PICKED_UP and REJECTED are terminal
        };
    }
}
