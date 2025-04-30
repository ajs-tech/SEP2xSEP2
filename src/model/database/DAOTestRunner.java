package model.database;

import model.database.LaptopDAO;
import model.database.StudentDAO;
import model.database.ReservationDAO;
import model.enums.PerformanceTypeEnum;
import model.enums.ReservationStatusEnum;
import model.models.Laptop;
import model.models.Student;
import model.models.Reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public class DAOTestRunner {

  public static void main(String[] args) {
    System.out.println("--- Starting DAO Manual Test Runner ---");

    LaptopDAO laptopDAO = new LaptopDAO();
    StudentDAO studentDAO = new StudentDAO();
    ReservationDAO reservationDAO = new ReservationDAO();

    // --- Test Data ---
    // NOTE: Laptop constructor creates a random UUID
    Laptop testLaptop = new Laptop("ManualTest", "DAO-Check", 256, 8, PerformanceTypeEnum.LOW);
    UUID laptopId = testLaptop.getId();
    System.out.println("Generated Test Laptop UUID: " + laptopId);

    // Use a unique VIA ID for testing
    int studentViaId = 999999;
    Student testStudent = new Student(
            "Test Student",
            new Date(System.currentTimeMillis() + 31536000000L), // Approx 1 year later
            "Test Degree",
            studentViaId,
            "test.student@example.com",
            12345670,
            PerformanceTypeEnum.LOW
    );

    Reservation testReservation = null; // To be created after student/laptop inserted
    UUID reservationId = null;

    // --- Clean Before Test (Optional but recommended) ---
    System.out.println(" --- Attempting Pre-Test Cleanup ---");
    try {
      // Vi henter først eventuelle reservationer der peger på vores test-elementer
      ReservationDAO tempReservationDAO = new ReservationDAO();
      // Du kan evt. implementere en metode i ReservationDAO til at hente reservationer efter laptop_id
      // I mellemtiden kan vi slette alle reservationer der måtte være tilknyttet test-laptop (ikke optimalt, men virker)
      try {
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM Reservation WHERE laptop_uuid = ? OR student_via_id = ?");
        stmt.setString(1, laptopId.toString());
        stmt.setInt(2, studentViaId);
        stmt.executeUpdate();
        stmt.close();
        conn.close();
      } catch (Exception e) {
        System.out.println("Cleanup reservation error: " + e.getMessage());
      }

      // Derefter kan vi slette laptop og student
      try { laptopDAO.delete(laptopId); } catch (Exception e) { System.out.println("Cleanup laptop error: " + e.getMessage()); }
      try { studentDAO.delete(studentViaId); } catch (Exception e) { System.out.println("Cleanup student error: " + e.getMessage()); }
    } catch (Exception e) {
      System.out.println("General cleanup error: " + e.getMessage());
    }
    System.out.println("Cleanup attempted.");

    // --- LaptopDAO Test ---
    System.out.println(" --- Testing LaptopDAO ---");
    try {
      System.out.println("Inserting Laptop...");
      boolean inserted = laptopDAO.insert(testLaptop);
      System.out.println("Laptop Inserted: " + inserted);

      if (inserted) {
        System.out.println("Getting Laptop by ID: " + laptopId);
        Laptop retrieved = laptopDAO.getById(laptopId);
        if (retrieved != null) {
          System.out.println("Retrieved Laptop: " + retrieved.getBrand() + " " + retrieved.getModel() + " (DB UUID might differ: " + retrieved.getId() + ")");

          System.out.println("Updating Laptop...");
          retrieved.setModel("DAO-Check-Updated"); // Update the retrieved object
          boolean updated = laptopDAO.update(retrieved); // Pass the object with the DB's UUID
          System.out.println("Laptop Updated: " + updated);

          System.out.println("Getting Updated Laptop by ID: " + retrieved.getId());
          Laptop retrievedUpdated = laptopDAO.getById(retrieved.getId());
          if(retrievedUpdated != null) {
            System.out.println("Retrieved Updated Laptop Model: " + retrievedUpdated.getModel());
          } else {
            System.out.println("Failed to retrieve updated laptop.");
          }

        } else {
          System.out.println("Failed to retrieve laptop after insert.");
        }
      } else {
        System.out.println("Skipping further laptop tests due to insert failure.");
      }

    } catch (SQLException e) {
      System.err.println("LaptopDAO Error: " + e.getMessage());
      e.printStackTrace();
    }

    // --- StudentDAO Test ---
    System.out.println(" --- Testing StudentDAO ---");
    try {
      System.out.println("Inserting Student...");
      boolean inserted = studentDAO.insert(testStudent);
      System.out.println("Student Inserted: " + inserted);

      if (inserted) {
        System.out.println("Getting Student by ID: " + studentViaId);
        Student retrieved = studentDAO.getById(studentViaId);
        if (retrieved != null) {
          System.out.println("Retrieved Student: " + retrieved.getName() + " (VIA ID: " + retrieved.getViaId() + ")");

          System.out.println("Updating Student...");
          retrieved.setEmail("updated.student@example.com");
          boolean updated = studentDAO.update(retrieved);
          System.out.println("Student Updated: " + updated);

          System.out.println("Getting Updated Student by ID: " + studentViaId);
          Student retrievedUpdated = studentDAO.getById(studentViaId);
          if(retrievedUpdated != null) {
            System.out.println("Retrieved Updated Student Email: " + retrievedUpdated.getEmail());
          } else {
            System.out.println("Failed to retrieve updated student.");
          }
        } else {
          System.out.println("Failed to retrieve student after insert.");
        }
      } else {
        System.out.println("Skipping further student tests due to insert failure.");
      }

    } catch (SQLException e) {
      System.err.println("StudentDAO Error: " + e.getMessage());
      e.printStackTrace();
    }

    // --- ReservationDAO Test ---
    System.out.println(" --- Testing ReservationDAO ---");
            // Retrieve fresh student/laptop objects as they might have changed
            Laptop currentLaptop = null;
    Student currentStudent = null;
    try {
      currentLaptop = laptopDAO.getById(laptopId); // Use original ID 
      currentStudent = studentDAO.getById(studentViaId);
    } catch (SQLException e) {
      System.err.println("Failed to retrieve student/laptop for reservation test: " + e.getMessage());
    }

    if (currentLaptop != null && currentStudent != null) {
      System.out.println("Laptop and Student retrieved for reservation test.");
      try {
        // NOTE: Reservation constructor creates a random UUID
        testReservation = new Reservation(currentStudent, currentLaptop);
        reservationId = testReservation.getReservationId();
        System.out.println("Generated Test Reservation UUID: " + reservationId);

        System.out.println("Inserting Reservation...");
        boolean inserted = reservationDAO.insert(testReservation);
        System.out.println("Reservation Inserted: " + inserted);

        if (inserted) {
          System.out.println("Getting Reservation by ID: " + reservationId);
          // Use the ID generated by the object we inserted
          Reservation retrieved = reservationDAO.getById(reservationId);
          if (retrieved != null) {
            System.out.println("Retrieved Reservation Status: " + retrieved.getStatus());
            System.out.println("  -> Laptop: " + retrieved.getLaptop().getModel());
            System.out.println("  -> Student: " + retrieved.getStudent().getName());

            System.out.println("Updating Reservation Status...");
            // Use reflection to get the DB object's actual UUID
            UUID dbReservationId = retrieved.getReservationId();
            retrieved.changeStatus(ReservationStatusEnum.COMPLETED);
            boolean updated = reservationDAO.update(retrieved);
            System.out.println("Reservation Updated: " + updated);

            System.out.println("Getting Updated Reservation by ID: " + dbReservationId);
            Reservation retrievedUpdated = reservationDAO.getById(dbReservationId);
            if (retrievedUpdated != null) {
              System.out.println("Retrieved Updated Reservation Status: " + retrievedUpdated.getStatus());
            } else {
              System.out.println("Failed to retrieve updated reservation.");
            }

            // Use the DB ID for deletion
            reservationId = dbReservationId;
          } else {
            System.out.println("Failed to retrieve reservation after insert.");
            reservationId = null; // Can't delete if not retrieved
          }
        } else {
          System.out.println("Skipping further reservation tests due to insert failure.");
          reservationId = null;
        }
      } catch (SQLException e) {
        System.err.println("ReservationDAO Error: " + e.getMessage());
        e.printStackTrace();
        reservationId = null;
      }
    } else {
      System.out.println("Skipping Reservation tests: Could not retrieve prerequisite Laptop or Student.");
    }

    // --- Final Cleanup ---
    System.out.println("--- Attempting Post-Test Cleanup ---");
    try {
      if (reservationId != null) {
        System.out.println("Deleting Reservation: " + reservationId);
        boolean deleted = reservationDAO.delete(reservationId);
        System.out.println("Reservation Deleted: " + deleted);
      }
    } catch (SQLException e) {
      System.err.println("Cleanup Reservation Error: " + e.getMessage());
    }
    try {
      System.out.println("Deleting Laptop: " + laptopId);
      boolean deleted = laptopDAO.delete(laptopId);
      System.out.println("Laptop Deleted: " + deleted);
    } catch (SQLException e) {
      System.err.println("Cleanup Laptop Error: " + e.getMessage());
    }
    try {
      System.out.println("Deleting Student: " + studentViaId);
      boolean deleted = studentDAO.delete(studentViaId);
      System.out.println("Student Deleted: " + deleted);
    } catch (SQLException e) {
      System.err.println("Cleanup Student Error: " + e.getMessage());
    }

    System.out.println("DAO Manual Test Runner Finished");
  }
}
