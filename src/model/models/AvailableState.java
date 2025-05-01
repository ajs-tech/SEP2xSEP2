package model.models;

public class AvailableState implements LaptopState {
    @Override
    public String getPropertyName() {
        return "toAvailableState";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isLoaned() {
        return false;
    }
}
