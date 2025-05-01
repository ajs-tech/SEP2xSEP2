package model.database;

import model.enums.PerformanceTypeEnum;
import model.models.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Data Access Object for køhåndtering med database persistering
 */
public class QueueDAO {
  private static final Logger logger = Logger.getLogger(QueueDAO.class.getName());
  private StudentDAO studentDAO = new StudentDAO();

  /**
   * Tilføj en student til ydelseskøen i databasen
   * @param student Studenten der skal tilføjes
   * @param performanceType Ydelsestypen køen er for
   * @return true hvis operationen lykkedes
   */
  public boolean addToQueue(Student student, PerformanceTypeEnum performanceType) throws SQLException {
    String sql = "INSERT INTO QueueEntry (student_via_id, performance_type, entry_date) VALUES (?, ?, CURRENT_TIMESTAMP)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, student.getViaId());
      stmt.setString(2, performanceType.name());

      int affectedRows = stmt.executeUpdate();
      return affectedRows > 0;
    }
  }

  /**
   * Henter studerende i kø for en specifik ydelsestype, sorteret efter indgangsdato
   * @param performanceType Ydelsestypen at hente studerende for
   * @return Liste af studerende i kø
   */
  public List<Student> getStudentsInQueue(PerformanceTypeEnum performanceType) throws SQLException {
    List<Student> studentsInQueue = new ArrayList<>();
    String sql = "SELECT student_via_id FROM QueueEntry WHERE performance_type = ? ORDER BY entry_date ASC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, performanceType.name());

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          int studentViaId = rs.getInt("student_via_id");
          Student student = studentDAO.getById(studentViaId);
          if (student != null) {
            studentsInQueue.add(student);
          }
        }
      }
    }
    return studentsInQueue;
  }

  /**
   * Fjern en student fra køen
   * @param studentViaId Studentens VIA ID
   * @param performanceType Ydelsestypen køen er for
   * @return true hvis operationen lykkedes
   */
  public boolean removeFromQueue(int studentViaId, PerformanceTypeEnum performanceType) throws SQLException {
    String sql = "DELETE FROM QueueEntry WHERE student_via_id = ? AND performance_type = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, studentViaId);
      stmt.setString(2, performanceType.name());

      int affectedRows = stmt.executeUpdate();
      return affectedRows > 0;
    }
  }

  /**
   * Henter den næste student i kø for en ydelsestype og fjerner dem med transaktionssikkerhed
   * @param performanceType Ydelsestypen at hente fra
   * @return Studenten eller null hvis køen er tom
   */
  public Student getAndRemoveNextInQueue(PerformanceTypeEnum performanceType) throws SQLException {
    Connection conn = null;
    try {
      conn = DatabaseConnection.getConnection();
      conn.setAutoCommit(false);

      // Få den næste student i kø
      String selectSql = "SELECT student_via_id, entry_id FROM QueueEntry WHERE performance_type = ? ORDER BY entry_date ASC LIMIT 1";
      Student student = null;
      int entryId = -1;

      try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
        stmt.setString(1, performanceType.name());
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            int studentViaId = rs.getInt("student_via_id");
            entryId = rs.getInt("entry_id");
            student = studentDAO.getById(studentViaId);
          }
        }
      }

      // Fjern fra kø hvis fundet
      if (student != null && entryId > 0) {
        String deleteSql = "DELETE FROM QueueEntry WHERE entry_id = ?";
        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
          deleteStmt.setInt(1, entryId);
          deleteStmt.executeUpdate();
        }
      }

      conn.commit();
      return student;

    } catch (SQLException e) {
      if (conn != null) {
        try {
          conn.rollback();
          logger.log(Level.WARNING, "Transaktion rullet tilbage: " + e.getMessage());
        } catch (SQLException ex) {
          logger.log(Level.SEVERE, "Fejl under rollback: " + ex.getMessage());
        }
      }
      throw e;
    } finally {
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
          conn.close();
        } catch (SQLException e) {
          logger.log(Level.WARNING, "Fejl ved nulstilling af forbindelse: " + e.getMessage());
        }
      }
    }
  }

  /**
   * Tjekker om en student er i køen
   * @param studentViaId Studentens VIA ID
   * @return true hvis studenten er i en kø
   */
  public boolean isStudentInAnyQueue(int studentViaId) throws SQLException {
    String sql = "SELECT COUNT(*) FROM QueueEntry WHERE student_via_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, studentViaId);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0;
        }
      }
    }
    return false;
  }

  /**
   * Henter størrelsen på en specifik kø
   * @param performanceType Ydelsestypen at tjekke for
   * @return Antallet af studerende i køen
   */
  public int getQueueSize(PerformanceTypeEnum performanceType) throws SQLException {
    String sql = "SELECT COUNT(*) FROM QueueEntry WHERE performance_type = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, performanceType.name());

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1);
        }
      }
    }
    return 0;
  }}