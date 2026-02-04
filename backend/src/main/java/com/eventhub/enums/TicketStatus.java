package com.eventhub.enums;

public enum TicketStatus {
    ACTIVE,
    CANCELLED,
    USED;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isUsed() {
        return this == USED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public boolean canBeCancelled() {
        return this == ACTIVE;
    }

    public boolean canBeUsed() {
        return this == ACTIVE;
    }

    public boolean isFinalState() {
        return this == CANCELLED || this == USED;
    }

    public boolean canTransitionTo(TicketStatus newStatus) {
        if (this.isFinalState()) {
            return false;
        }

        if (this == ACTIVE) {
            return newStatus == USED || newStatus == CANCELLED;
        }

        return false;
    }

    public String getDescription() {
        return switch (this) {
            case ACTIVE -> "Active - Ready to use";
            case CANCELLED -> "Cancelled - Refunded";
            case USED -> "Used - Already redeemed";
        };
    }
}
