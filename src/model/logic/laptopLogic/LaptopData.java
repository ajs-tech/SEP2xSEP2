package model.logic.laptopLogic;

import model.enums.PerformanceTypeEnum;
import model.logic.reservationsLogic.ReservationManager;
import model.models.Laptop;

import java.util.ArrayList;
import java.util.UUID;

public class LaptopData implements LaptopDataInterface {
    private ArrayList<Laptop> allLaptops;

    public LaptopData() {
        allLaptops = new ArrayList<>();
    }

    // Laptop getters og setters

    @Override
    public ArrayList<Laptop> getAllLaptops() {
        return allLaptops;
    }

    @Override
    public int getAmountOfAvailableLaptops(){
        int availableLaptops = 0;
        for (int i = 0; i < allLaptops.size(); i++){
            if (allLaptops.get(i).isAvailable()){
                availableLaptops++;
            }
        }
        return availableLaptops;
    }

    @Override
    public int getAmountOfLoanedLaptops(){
        int loanedLaptops = 0;
        for (Laptop laptop : allLaptops){
            if (laptop.isLoaned()){
                loanedLaptops++;
            }
        }
        return loanedLaptops;
    }

    @Override
    public int getAmountOfLaptopsByState(String classSimpleName){
        int numberOfLaptops = 0;
        for (Laptop laptop : allLaptops){
            if (laptop.getState().getClass().getSimpleName().equals(classSimpleName)){
                numberOfLaptops++;
            }
        }
        return numberOfLaptops;
    }

    @Override
    public Laptop findAvailableLaptop(PerformanceTypeEnum performanceTypeEnum) {
        for (Laptop laptop : allLaptops){
            if (laptop.isAvailable() && laptop.getPerformanceType().equals(performanceTypeEnum)){
                return laptop;
            }
        }
        return null;
    }

    @Override
    public Laptop createLaptop(PerformanceTypeEnum performanceTypeEnum, ReservationManager manager){
        Laptop laptop = new Laptop("APPLE", "M3 Pro", 520, 18, performanceTypeEnum, manager);
        allLaptops.add(laptop);
        return laptop;
    }




}
