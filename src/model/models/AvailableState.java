package model.models;

public class AvailableState implements LaptopState{
    public final static LaptopState LOANED_STATE_INSTANCE = new LoanedState();


    @Override
    public void click(Laptop laptop) {
        laptop.changeState(LOANED_STATE_INSTANCE);
    }

}
