package model.models;

public class LoanedState implements LaptopState {
    @Override
    public String getPropertyName() {
        return "toLoanedState";
    }
    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public boolean isLoaned() {
        return true;
    }
}
