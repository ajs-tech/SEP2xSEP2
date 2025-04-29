package model.logic.laptopLogic;

import model.enums.PerformanceTypeEnum;
import model.models.Laptop;

import java.util.ArrayList;

public interface LaptopDataInterface {

    // Laptop getters og setters

    ArrayList<Laptop> getAllLaptops();

    int getAmountOfAvailableLaptops();

    int getAmountOfLoanedLaptops();

    int getAmountOfLaptopsByState(String classSimpleName);

    Laptop findAvailableLaptop(PerformanceTypeEnum performanceTypeEnum);

    Laptop createLaptop(PerformanceTypeEnum performanceTypeEnum);

}
