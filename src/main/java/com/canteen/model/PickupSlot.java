package com.canteen.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A pickup window (e.g. 2026-10-06, 12:00-12:30) with a cap on orders.
 * The transient "remaining" field is computed per request (maxOrders - booked)
 * and is NOT a database column.
 */
public class PickupSlot {

    private int id;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int maxOrders;
    private boolean active;
    private int remaining; // computed, not persisted

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public int getMaxOrders() { return maxOrders; }
    public void setMaxOrders(int maxOrders) { this.maxOrders = maxOrders; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getRemaining() { return remaining; }
    public void setRemaining(int remaining) { this.remaining = remaining; }

    public boolean isFull() { return remaining <= 0; }

    /** "12:00 - 12:30" style label used in dropdowns. */
    public String getLabel() {
        return startTime + " - " + endTime;
    }
}
