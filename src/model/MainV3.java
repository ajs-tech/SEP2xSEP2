package model;

import model.enums.PerformanceTypeEnum;
import model.logic.DataManager;
import model.models.Laptop;
import model.models.Student;

import java.util.ArrayList;
import java.util.Date;

public class MainV3 {
    public static void main(String[] args) {
        System.out.println("--- STARTER LAPTOP UDLÅNSSYSTEM ---");

        // Opret central DataManager
        DataManager dataManager = new DataManager();
        System.out.println("DataManager oprettet og klar til brug");

        // --- SCENARIO 1: INGEN LAPTOPS OG STUDERENDE ---
        System.out.println("\n--- SCENARIO 1: INGEN LAPTOPS OG STUDERENDE ---");
        System.out.println("Antal laptops i systemet: " + dataManager.getAllLaptops().size());
        System.out.println("Antal studerende i systemet: " + dataManager.getAllStudents().size());
        System.out.println("Studerende i kø til høj-ydelses laptops: " + dataManager.getReservationManagerObject().getHighNeedingQueueSize());
        System.out.println("Studerende i kø til lav-ydelses laptops: " + dataManager.getReservationManagerObject().getLowNeedingQueueSize());

        // --- SCENARIO 2: OPRETTELSE AF LAPTOPS ---
        System.out.println("\n--- SCENARIO 2: OPRETTELSE AF LAPTOPS ---");
        for (int i = 1; i <= 5; i++) {
            Laptop highPerformanceLaptop = dataManager.createLaptop(PerformanceTypeEnum.HIGH, dataManager.getReservationManagerObject());
            highPerformanceLaptop.setBrand("Lenovo");
            highPerformanceLaptop.setModel("Yoga " + i);
            highPerformanceLaptop.setRam(16);
            highPerformanceLaptop.setGigabyte(512);
            System.out.println("Tilføjet Lenovo Yoga laptop #" + i + " (HighPerformanceLaptop) - State: Available");

            Laptop lowPerformanceLaptop = dataManager.createLaptop(PerformanceTypeEnum.LOW, dataManager.getReservationManagerObject());
            lowPerformanceLaptop.setBrand("Lenovo");
            lowPerformanceLaptop.setModel("IdeaPad " + i);
            lowPerformanceLaptop.setRam(8);
            lowPerformanceLaptop.setGigabyte(256);
            System.out.println("Tilføjet Lenovo IdeaPad laptop #" + i + " (LowPerformanceLaptop) - State: Available");
        }

        System.out.println("Laptop-oversigt:");
        System.out.println("- Antal laptops tilføjet: " + dataManager.getAllLaptops().size());
        System.out.println("- Antal laptops i Available state: " + dataManager.getAmountOfAvailableLaptops());
        System.out.println("- Antal laptops i Loaned state: " + dataManager.getAmountOfLoanedLaptops());

        // --- SCENARIO 3: LAPTOPS BLIVER TILGÆNGELIGE, MEN INGEN STUDERENDE ---
        System.out.println("\n--- SCENARIO 3: LAPTOPS BLIVER TILGÆNGELIGE, MEN INGEN STUDERENDE ---");
        for (Laptop laptop : dataManager.getAllLaptops()) {
            laptop.changeState(new model.models.AvailableState());
        }
        System.out.println("Alle laptops er nu i Available state");
        System.out.println("Studerende i kø til høj-ydelses laptops: " + dataManager.getReservationManagerObject().getHighNeedingQueueSize());
        System.out.println("Studerende i kø til lav-ydelses laptops: " + dataManager.getReservationManagerObject().getLowNeedingQueueSize());
        System.out.println("Aktive reservationer: " + dataManager.getReservationManagerObject().getAmountOfActiveReservations());

        // --- SCENARIO 4: OPRETTELSE AF STUDERENDE OG AUTOMATISK RESERVATION ---
        System.out.println("\n--- SCENARIO 4: OPRETTELSE AF STUDERENDE OG AUTOMATISK RESERVATION ---");
        Student student1 = createStudent(dataManager, "Anna Hansen", PerformanceTypeEnum.HIGH);
        Student student2 = createStudent(dataManager, "Peter Jensen", PerformanceTypeEnum.LOW);
        Student student3 = createStudent(dataManager, "Maria Nielsen", PerformanceTypeEnum.HIGH);

        System.out.println("Studerende oprettet:");
        System.out.println("- Anna Hansen (HighPerformance)");
        System.out.println("- Peter Jensen (LowPerformance)");
        System.out.println("- Maria Nielsen (HighPerformance)");

        System.out.println("Laptop-oversigt efter reservation:");
        System.out.println("- Antal laptops i Available state: " + dataManager.getAmountOfAvailableLaptops());
        System.out.println("- Antal laptops i Loaned state: " + dataManager.getAmountOfLoanedLaptops());
        System.out.println("Reservation-oversigt:");
        System.out.println("- Aktive reservationer: " + dataManager.getReservationManagerObject().getAmountOfActiveReservations());

        // --- SCENARIO 5: LAPTOPS BLIVER TILGÆNGELIGE OG KØ-HÅNDTERING ---
        System.out.println("\n--- SCENARIO 5: LAPTOPS BLIVER TILGÆNGELIGE OG KØ-HÅNDTERING ---");
        for (Laptop laptop : dataManager.getAllLaptops()) {
            laptop.changeState(new model.models.AvailableState());
        }
        System.out.println("Alle laptops er nu i Available state igen");
        System.out.println("Studerende i kø til høj-ydelses laptops: " + dataManager.getReservationManagerObject().getHighNeedingQueueSize());
        System.out.println("Studerende i kø til lav-ydelses laptops: " + dataManager.getReservationManagerObject().getLowNeedingQueueSize());
        System.out.println("Reservation-oversigt:");
        System.out.println("- Aktive reservationer: " + dataManager.getReservationManagerObject().getAmountOfActiveReservations());

        // --- SCENARIO 6: AFBRYDELSE AF RESERVATION ---
        System.out.println("\n--- SCENARIO 6: AFBRYDELSE AF RESERVATION ---");
        if (dataManager.getReservationManagerObject().getAmountOfActiveReservations() > 0) {
            Student cancelStudent = student1;
            Laptop cancelLaptop = dataManager.getReservationManagerObject().getAllActiveReservations().get(0).getLaptop();
            System.out.println("Afbryder reservation for " + cancelStudent.getName() + " med laptop " + cancelLaptop.getBrand() + " " + cancelLaptop.getModel());
            cancelLaptop.changeState(new model.models.AvailableState());
            System.out.println("Laptop state ændret til Available");
        } else {
            System.out.println("Ingen aktive reservationer at afbryde");
        }

        System.out.println("--- SLUT PÅ LAPTOP UDLÅNSSYSTEM ---");
    }

    // Hjælpemetode til at oprette studerende
    private static Student createStudent(DataManager manager, String name, PerformanceTypeEnum performanceNeeded) {
        return manager.createStudent(
                name,
                new Date(),
                "Software Engineering",
                12345 + (int) (Math.random() * 10000),
                name.toLowerCase().replace(" ", ".") + "@via.dk",
                10000000 + (int) (Math.random() * 89999999),
                performanceNeeded
        );
    }
}
