package model.database;

import model.enums.PerformanceTypeEnum;
import model.models.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student entities
 * Forbedret med korrekt håndtering af afhængige reservationer
 */
public class StudentDAO {

  /**
   * Henter alle studerende fra databasen
   * @return Liste af studerende
   */
  public List<Student> getAllStudents() throws SQLException {
    List<Student> students = new ArrayList<>();
    String sql = "SELECT via_id, name, degree_end_date, degree_title, email, phone_number, performance_needed, has_laptop FROM Student";

    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        Student student = mapResultSetToStudent(rs);
        students.add(student);
      }
    }
    return students;
  }

  /**
   * Henter en student baseret på VIA ID
   * @param viaId Student VIA ID
   * @return Student objekt eller null hvis ikke fundet
   */
  public Student getById(int viaId) throws SQLException {
    String sql = "SELECT via_id, name, degree_end_date, degree_title, email, phone_number, performance_needed, has_laptop FROM Student WHERE via_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, viaId);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToStudent(rs);
        }
      }
    }
    return null;
  }

  /**
   * Indsætter en ny student i databasen
   * @param student Student objekt
   * @return true hvis operationen lykkedes
   */
  public boolean insert(Student student) throws SQLException {
    String sql = "INSERT INTO Student (via_id, name, degree_end_date, degree_title, email, phone_number, performance_needed, has_laptop) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, student.getViaId());
      stmt.setString(2, student.getName());
      stmt.setDate(3, new java.sql.Date(student.getDegreeEndDate().getTime()));
      stmt.setString(4, student.getDegreeTitle());
      stmt.setString(5, student.getEmail());
      stmt.setInt(6, student.getPhoneNumber());
      stmt.setString(7, student.getPerformanceNeeded().name());
      stmt.setBoolean(8, student.isHasLaptop());

      int affectedRows = stmt.executeUpdate();
      return affectedRows > 0;
    }
  }

  /**
   * Opdaterer en eksisterende student
   * @param student Student objekt med opdaterede oplysninger
   * @return true hvis operationen lykkedes
   */
  public boolean update(Student student) throws SQLException {
    String sql = "UPDATE Student SET name = ?, degree_end_date = ?, degree_title = ?, email = ?, phone_number = ?, performance_needed = ?, has_laptop = ? WHERE via_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, student.getName());
      stmt.setDate(2, new java.sql.Date(student.getDegreeEndDate().getTime()));
      stmt.setString(3, student.getDegreeTitle());
      stmt.setString(4, student.getEmail());
      stmt.setInt(5, student.getPhoneNumber());
      stmt.setString(6, student.getPerformanceNeeded().name());
      stmt.setBoolean(7, student.isHasLaptop());
      stmt.setInt(8, student.getViaId());

      int affectedRows = stmt.executeUpdate();
      return affectedRows > 0;
    }
  }

  /**
   * Sletter en student fra databasen
   * @param viaId Student VIA ID
   * @return true hvis operationen lykkedes
   */
  public boolean delete(int viaId) throws SQLException {
    // Først slet afhængige reservationer
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement("DELETE FROM Reservation WHERE student_via_id = ?")) {
      stmt.setInt(1, viaId);
      stmt.executeUpdate(); // Vi ignorerer resultatet, da der måske ikke er nogen reservationer
    } catch (SQLException e) {
      System.err.println("Advarsel: Kunne ikke slette reservationer for student " + viaId + ": " + e.getMessage());
      // Vi fortsætter alligevel
    }

    // Derefter slet studenten
    String sql = "DELETE FROM Student WHERE via_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, viaId);

      int affectedRows = stmt.executeUpdate();
      return affectedRows > 0;
    }
  }

  /**
   * Hjælpemetode til at konvertere ResultSet til Student objekt
   */
  public Student mapResultSetToStudent(ResultSet rs) throws SQLException {
    String name = rs.getString("name");
    java.util.Date degreeEndDate = rs.getDate("degree_end_date");
    String degreeTitle = rs.getString("degree_title");
    int viaId = rs.getInt("via_id");
    String email = rs.getString("email");
    int phoneNumber = rs.getInt("phone_number");
    PerformanceTypeEnum performanceNeeded = PerformanceTypeEnum.valueOf(rs.getString("performance_needed"));
    boolean hasLaptop = rs.getBoolean("has_laptop");

    Student student = new Student(name, degreeEndDate, degreeTitle, viaId, email, phoneNumber, performanceNeeded);

    // Set hasLaptop baseret på databaseværdien
    if (hasLaptop != student.isHasLaptop()) {
      student.setHasLaptopToOpposite();
    }

    return student;
  }
}