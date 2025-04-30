package model.database;

import model.enums.PerformanceTypeEnum;
import model.enums.ReservationStatusEnum;
import model.models.Laptop;
import model.models.Student;
import model.models.Reservation;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Interaktivt test-program til at afprøve DAO-funktionalitet via konsol
 */
public class DAOTester {
  private static Scanner scanner = new Scanner(System.in);
  private static LaptopDAO laptopDAO = new LaptopDAO();
  private static StudentDAO studentDAO = new StudentDAO();
  private static ReservationDAO reservationDAO = new ReservationDAO();

  public static void main(String[] args) {
    boolean running = true;

    System.out.println("===== DAO Test Program =====");

    while (running) {
      System.out.println("\nVælg en test:");
      System.out.println("1: Vis alle laptops");
      System.out.println("2: Vis alle studerende");
      System.out.println("3: Vis alle reservationer");
      System.out.println("4: Tilføj ny laptop");
      System.out.println("5: Tilføj ny student");
      System.out.println("6: Opret reservation");
      System.out.println("7: Opdater reservation status");
      System.out.println("8: Find laptop via ID");
      System.out.println("9: Find student via ID");
      System.out.println("0: Afslut");

      System.out.print("Vælg: ");
      int choice = scanner.nextInt();
      scanner.nextLine(); // Fjern newline

      try {
        switch (choice) {
          case 0:
            running = false;
            break;
          case 1:
            showAllLaptops();
            break;
          case 2:
            showAllStudents();
            break;
          case 3:
            showAllReservations();
            break;
          case 4:
            addNewLaptop();
            break;
          case 5:
            addNewStudent();
            break;
          case 6:
            createReservation();
            break;
          case 7:
            updateReservationStatus();
            break;
          case 8:
            findLaptopById();
            break;
          case 9:
            findStudentById();
            break;
          default:
            System.out.println("Ugyldigt valg. Prøv igen.");
        }
      } catch (SQLException e) {
        System.out.println("Database fejl: " + e.getMessage());
        e.printStackTrace();
      } catch (Exception e) {
        System.out.println("Fejl: " + e.getMessage());
        e.printStackTrace();
      }
    }

    System.out.println("Program afsluttet.");
    // Luk Connection Pool
    DatabaseConnection.closePool();
  }

  // Metode 1: Vis alle laptops
  private static void showAllLaptops() throws SQLException {
    System.out.println("\n=== Alle Laptops ===");
    List<Laptop> laptops = laptopDAO.getAllLaptops();

    if (laptops.isEmpty()) {
      System.out.println("Ingen laptops fundet i databasen.");
    } else {
      System.out.println("ID | Brand | Model | GB | RAM | Performance");
      System.out.println("-----------------------------------------");

      for (Laptop laptop : laptops) {
        System.out.printf("%s | %s | %s | %d | %d | %s%n",
                laptop.getId(),
                laptop.getBrand(),
                laptop.getModel(),
                laptop.getGigabyte(),
                laptop.getRam(),
                laptop.getPerformanceType());
      }
      System.out.println("Total: " + laptops.size() + " laptops");
    }
  }

  // Metode 2: Vis alle studerende
  private static void showAllStudents() throws SQLException {
    System.out.println("\n=== Alle Studerende ===");
    List<Student> students = studentDAO.getAllStudents();

    if (students.isEmpty()) {
      System.out.println("Ingen studerende fundet i databasen.");
    } else {
      System.out.println("VIA ID | Navn | Email | Performance behov | Har laptop");
      System.out.println("-----------------------------------------------------");

      for (Student student : students) {
        System.out.printf("%d | %s | %s | %s | %s%n",
                student.getViaId(),
                student.getName(),
                student.getEmail(),
                student.getPerformanceNeeded(),
                student.isHasLaptop() ? "Ja" : "Nej");
      }
      System.out.println("Total: " + students.size() + " studerende");
    }
  }

  // Metode 3: Vis alle reservationer
  private static void showAllReservations() throws SQLException {
    System.out.println("\n=== Alle Reservationer ===");
    List<Reservation> reservations = reservationDAO.getAllReservations();

    if (reservations.isEmpty()) {
      System.out.println("Ingen reservationer fundet i databasen.");
    } else {
      System.out.println("Reservations ID | Status | Student | Laptop");
      System.out.println("------------------------------------------");

      for (Reservation reservation : reservations) {
        System.out.printf("%s | %s | %s (ID: %d) | %s %s%n",
                reservation.getReservationId(),
                reservation.getStatus(),
                reservation.getStudent().getName(),
                reservation.getStudent().getViaId(),
                reservation.getLaptop().getBrand(),
                reservation.getLaptop().getModel());
      }
      System.out.println("Total: " + reservations.size() + " reservationer");
    }
  }

  // Metode 4: Tilføj ny laptop
  private static void addNewLaptop() throws SQLException {
    System.out.println("\n=== Tilføj Ny Laptop ===");

    System.out.print("Brand: ");
    String brand = scanner.nextLine();

    System.out.print("Model: ");
    String model = scanner.nextLine();

    System.out.print("Gigabyte (hårddisk): ");
    int gigabyte = scanner.nextInt();
    scanner.nextLine(); // Fjern newline

    System.out.print("RAM: ");
    int ram = scanner.nextInt();
    scanner.nextLine(); // Fjern newline

    System.out.print("Performance Type (LOW/HIGH): ");
    String perfType = scanner.nextLine().toUpperCase();
    PerformanceTypeEnum performanceType = PerformanceTypeEnum.valueOf(perfType);

    Laptop laptop = new Laptop(brand, model, gigabyte, ram, performanceType);
    boolean success = laptopDAO.insert(laptop);

    if (success) {
      System.out.println("Laptop oprettet med succes! ID: " + laptop.getId());
    } else {
      System.out.println("Kunne ikke oprette laptop.");
    }
  }

  // Metode 5: Tilføj ny student
  private static void addNewStudent() throws SQLException {
    System.out.println("\n=== Tilføj Ny Student ===");

    System.out.print("Navn: ");
    String name = scanner.nextLine();

    System.out.print("Uddannelse: ");
    String degree = scanner.nextLine();

    System.out.print("VIA ID: ");
    int viaId = scanner.nextInt();
    scanner.nextLine(); // Fjern newline

    System.out.print("Email: ");
    String email = scanner.nextLine();

    System.out.print("Telefonnummer: ");
    int phoneNumber = scanner.nextInt();
    scanner.nextLine(); // Fjern newline

    System.out.print("Performance behov (LOW/HIGH): ");
    String perfType = scanner.nextLine().toUpperCase();
    PerformanceTypeEnum performanceNeeded = PerformanceTypeEnum.valueOf(perfType);

    // Opret en slutdato for uddannelsen (1 år frem)
    Date degreeEndDate = new Date(System.currentTimeMillis() + 31536000000L);

    Student student = new Student(name, degreeEndDate, degree, viaId, email, phoneNumber, performanceNeeded);
    boolean success = studentDAO.insert(student);

    if (success) {
      System.out.println("Student oprettet med succes! VIA ID: " + student.getViaId());
    } else {
      System.out.println("Kunne ikke oprette student.");
    }
  }

  // Metode 6: Opret reservation
  private static void createReservation() throws SQLException {
    System.out.println("\n=== Opret Reservation ===");

    // Find først student
    System.out.print("Student VIA ID: ");
    int studentId = scanner.nextInt();
    scanner.nextLine(); // Fjern newline

    Student student = studentDAO.getById(studentId);
    if (student == null) {
      System.out.println("Student ikke fundet.");
      return;
    }

    // Find laptop
    System.out.print("Laptop UUID: ");
    String laptopIdStr = scanner.nextLine();
    UUID laptopId = UUID.fromString(laptopIdStr);

    Laptop laptop = laptopDAO.getById(laptopId);
    if (laptop == null) {
      System.out.println("Laptop ikke fundet.");
      return;
    }

    Reservation reservation = new Reservation(student, laptop);
    boolean success = reservationDAO.insert(reservation);

    if (success) {
      System.out.println("Reservation oprettet med succes! ID: " + reservation.getReservationId());
    } else {
      System.out.println("Kunne ikke oprette reservation.");
    }
  }

  // Metode 7: Opdater reservation status
  private static void updateReservationStatus() throws SQLException {
    System.out.println("\n=== Opdater Reservation Status ===");

    System.out.print("Reservation UUID: ");
    String reservationIdStr = scanner.nextLine();
    UUID reservationId = UUID.fromString(reservationIdStr);

    Reservation reservation = reservationDAO.getById(reservationId);
    if (reservation == null) {
      System.out.println("Reservation ikke fundet.");
      return;
    }

    System.out.println("Nuværende status: " + reservation.getStatus());
    System.out.println("Vælg ny status:");
    System.out.println("1: ACTIVE");
    System.out.println("2: COMPLETED");
    System.out.println("3: CANCELLED");

    int statusChoice = scanner.nextInt();
    scanner.nextLine(); // Fjern newline

    ReservationStatusEnum newStatus;
    switch (statusChoice) {
      case 1:
        newStatus = ReservationStatusEnum.ACTIVE;
        break;
      case 2:
        newStatus = ReservationStatusEnum.COMPLETED;
        break;
      case 3:
        newStatus = ReservationStatusEnum.CANCELLED;
        break;
      default:
        System.out.println("Ugyldigt valg. Status ikke ændret.");
        return;
    }

    reservation.changeStatus(newStatus);
    boolean success = reservationDAO.update(reservation);

    if (success) {
      System.out.println("Reservation status opdateret til: " + newStatus);
    } else {
      System.out.println("Kunne ikke opdatere reservation status.");
    }
  }

  // Metode 8: Find laptop via ID
  private static void findLaptopById() throws SQLException {
    System.out.println("\n=== Find Laptop via ID ===");

    System.out.print("Laptop UUID: ");
    String laptopIdStr = scanner.nextLine();
    UUID laptopId = UUID.fromString(laptopIdStr);

    Laptop laptop = laptopDAO.getById(laptopId);

    if (laptop == null) {
      System.out.println("Ingen laptop fundet med ID: " + laptopId);
    } else {
      System.out.println("Fundet laptop:");
      System.out.println("ID: " + laptop.getId());
      System.out.println("Brand: " + laptop.getBrand());
      System.out.println("Model: " + laptop.getModel());
      System.out.println("Gigabyte: " + laptop.getGigabyte());
      System.out.println("RAM: " + laptop.getRam());
      System.out.println("Performance Type: " + laptop.getPerformanceType());
      System.out.println("Status: " + (laptop.isAvailable() ? "Tilgængelig" : "Udlånt"));
    }
  }

  // Metode 9: Find student via ID
  private static void findStudentById() throws SQLException {
    System.out.println("\n=== Find Student via ID ===");

    System.out.print("Student VIA ID: ");
    int studentId = scanner.nextInt();
    scanner.nextLine(); // Fjern newline

    Student student = studentDAO.getById(studentId);

    if (student == null) {
      System.out.println("Ingen student fundet med VIA ID: " + studentId);
    } else {
      System.out.println("Fundet student:");
      System.out.println("VIA ID: " + student.getViaId());
      System.out.println("Navn: " + student.getName());
      System.out.println("Email: " + student.getEmail());
      System.out.println("Telefon: " + student.getPhoneNumber());
      System.out.println("Uddannelse: " + student.getDegreeTitle());
      System.out.println("Uddannelse slut: " + student.getDegreeEndDate());
      System.out.println("Performance behov: " + student.getPerformanceNeeded());
      System.out.println("Har laptop: " + (student.isHasLaptop() ? "Ja" : "Nej"));

      // Hent også reservationer for denne student
      try {
        List<Reservation> studentReservations = reservationDAO.getByStudentId(studentId);
        System.out.println("\nStudentens reservationer (" + studentReservations.size() + "):");

        if (!studentReservations.isEmpty()) {
          for (Reservation reservation : studentReservations) {
            System.out.printf("ID: %s | Status: %s | Laptop: %s %s%n",
                    reservation.getReservationId(),
                    reservation.getStatus(),
                    reservation.getLaptop().getBrand(),
                    reservation.getLaptop().getModel());
          }
        }
      } catch (SQLException e) {
        System.out.println("Kunne ikke hente reservationer: " + e.getMessage());
      }
    }
  }
}