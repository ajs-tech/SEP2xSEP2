package model;

import model.enums.PerformanceTypeEnum;
import model.logic.DataManager;
import model.models.Laptop;
import model.models.Student;

import java.util.Date;

public class MainV5 {
    public static void main(String[] args) {
        System.out.println("--- STARTER LAPTOP UDLÅNSSYSTEM ---");

        // Opret central DataManager
        DataManager dataManager = new DataManager();
        System.out.println("DataManager oprettet og klar til brug");

        // SCENARIO 1: OPRETTELSE AF LAPTOPS
        System.out.println("\n--- SCENARIO 1: OPRETTELSE AF LAPTOPS ---");
        for (int i = 1; i <= 5; i++) {
            Laptop highPerformanceLaptop = dataManager.createLaptop(PerformanceTypeEnum.HIGH, dataManager.getReservationManagerObject());
            highPerformanceLaptop.setBrand("Lenovo");
            highPerformanceLaptop.setModel("Yoga " + i);
            System.out.println("Tilføjet Lenovo Yoga laptop #" + i + " (HighPerformanceLaptop) - State: Available");

            Laptop lowPerformanceLaptop = dataManager.createLaptop(PerformanceTypeEnum.LOW, dataManager.getReservationManagerObject());
            lowPerformanceLaptop.setBrand("Lenovo");
            lowPerformanceLaptop.setModel("IdeaPad " + i);
            System.out.println("Tilføjet Lenovo IdeaPad laptop #" + i + " (LowPerformanceLaptop) - State: Available");
        }

        System.out.println("Laptop-oversigt:");
        System.out.println("- Antal laptops tilføjet: " + dataManager.getAllLaptops().size());
        System.out.println("- Antal laptops i Available state: " + dataManager.getAmountOfAvailableLaptops());
        System.out.println("- Antal laptops i Loaned state: " + dataManager.getAmountOfLoanedLaptops());

        // SCENARIO 2: OPRETTELSE AF FLERE STUDERENDE END LAPTOPS
        System.out.println("\n--- SCENARIO 2: OPRETTELSE AF FLERE STUDERENDE END LAPTOPS ---");
        for (int i = 1; i <= 8; i++) {
            Student highPerformanceStudent = dataManager.createStudent(
                    "HighPerformance Student " + i,
                    new Date(),
                    "Software Engineering",
                    12345 + i,
                    "highstudent" + i + "@via.dk",
                    10000000 + i,
                    PerformanceTypeEnum.HIGH
            );
            System.out.println("Studerende oprettet: " + highPerformanceStudent.getName() + " (HighPerformance)");
        }

        for (int i = 1; i <= 7; i++) {
            Student lowPerformanceStudent = dataManager.createStudent(
                    "LowPerformance Student " + i,
                    new Date(),
                    "Software Engineering",
                    22345 + i,
                    "lowstudent" + i + "@via.dk",
                    20000000 + i,
                    PerformanceTypeEnum.LOW
            );
            System.out.println("Studerende oprettet: " + lowPerformanceStudent.getName() + " (LowPerformance)");
        }

        System.out.println("\nStuderende-oversigt:");
        System.out.println("- Antal studerende oprettet: " + dataManager.getAllStudents().size());
        System.out.println("- Studerende med høj ydelses behov: " + dataManager.getStudentCountOfHighPowerNeeds());
        System.out.println("- Studerende med lav ydelses behov: " + dataManager.getStudentCountOfLowPowerNeeds());

        // SCENARIO 3: LAPTOPS TILDELES OG KØER OPDATERES
        System.out.println("\n--- SCENARIO 3: LAPTOPS TILDELES OG KØER OPDATERES ---");
        System.out.println("Laptop-oversigt før tildeling:");
        System.out.println("- Antal laptops i Available state: " + dataManager.getAmountOfAvailableLaptops());
        System.out.println("- Antal laptops i Loaned state: " + dataManager.getAmountOfLoanedLaptops());

        System.out.println("\nEfter tildeling:");
        System.out.println("- Antal laptops i Available state: " + dataManager.getAmountOfAvailableLaptops());
        System.out.println("- Antal laptops i Loaned state: " + dataManager.getAmountOfLoanedLaptops());
        System.out.println("- Studerende i kø til høj-ydelses laptops: " + dataManager.getReservationManagerObject().getHighNeedingQueueSize());
        System.out.println("- Studerende i kø til lav-ydelses laptops: " + dataManager.getReservationManagerObject().getLowNeedingQueueSize());
        System.out.println("- Aktive reservationer: " + dataManager.getReservationManagerObject().getAmountOfActiveReservations());
        System.out.println(dataManager.getAllLaptops());

    }

}
