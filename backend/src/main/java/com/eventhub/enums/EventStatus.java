package com.eventhub.enums;

public enum EventStatus {
    SCHEDULED,
    ONGOING,
    COMPLETED,
    CANCELLED;

    public boolean isScheduled() {
        return this == SCHEDULED;
    }

    public boolean isOngoing() {
        return this == ONGOING;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public boolean isActive() {
        return this == SCHEDULED || this == ONGOING;
    }

    public boolean isFinalState() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean canBeCancelled() {
        return this == SCHEDULED;
    }

    public boolean canTransitionTo(EventStatus newStatus) {
        if (this.isFinalState()) {
            return false;
        }

        return switch (this) {
            case SCHEDULED -> newStatus == ONGOING || newStatus == CANCELLED;
            case ONGOING -> newStatus == COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    public String getDescription() {
        return switch (this) {
            case SCHEDULED -> "Scheduled - Event is planned for the future";
            case ONGOING -> "Ongoing - Event is currently happening";
            case COMPLETED -> "Completed - Event has ended";
            case CANCELLED -> "Cancelled - Event was cancelled";
        };
    }

    public String getDisplayName() {
        return switch (this) {
            case SCHEDULED -> "Agendado";
            case ONGOING -> "Em andamento";
            case COMPLETED -> "Encerrado";
            case CANCELLED -> "Cancelado";
        };
    }
}
