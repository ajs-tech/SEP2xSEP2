package model.models;

public class LoanedState implements LaptopState{
    public final static LaptopState AVAILABLE_STATE_INSTANCE = new AvailableState();


    @Override
    public void click(Laptop laptop) {
        laptop.changeState(AVAILABLE_STATE_INSTANCE);
    }


}
