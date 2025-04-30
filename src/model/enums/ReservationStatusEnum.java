package model.enums;

public enum ReservationStatusEnum {
    // Ændret til at matche database constraints. Bemærk 'ACTIVE' (uppercase) i stedet for 'Active'
    ACTIVE("ACTIVE"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED");

    private final String dbValue;

    ReservationStatusEnum(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }
}