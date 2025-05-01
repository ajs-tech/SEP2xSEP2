package model.database;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {

  public static void main(String[] args) {
    Connection conn = null;

    try {
      // Opret forbindelse til databasen
      conn = DatabaseConnection.getConnection();

      // Tjek om forbindelsen er oprettet
      if (conn != null) {
        System.out.println("Database forbindelse oprettet med succes!");
      } else {
        System.out.println("Fejl: Ingen forbindelse til databasen.");
      }

    } catch (SQLException e) {
      // Håndter fejl ved oprettelse af forbindelse
      System.out.println("Fejl ved oprettelse af database forbindelse:");
      e.printStackTrace();
    } finally {
      // Sørg for at lukke forbindelsen, hvis den blev oprettet
      if (conn != null) {
        try {
          conn.close();  // Luk forbindelsen
          System.out.println("Forbindelsen er lukket.");
        } catch (SQLException e) {
          System.out.println("Fejl ved lukning af forbindelsen:");
          e.printStackTrace();
        }
      }
    }
  }
}
