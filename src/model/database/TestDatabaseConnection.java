// Opret en ny klasse til at teste direkte adgang til databasen uden pools:
package model.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDatabaseConnection {
  public static void main(String[] args) {
    try {
      // Test direkte forbindelse til databasen
      Class.forName("org.postgresql.Driver");
      Connection conn = java.sql.DriverManager.getConnection(
              "jdbc:postgresql://localhost:5432/postgres",
              "postgres",
              "Seshej1991");

      System.out.println("Forbindelse oprettet!");

      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT 1");
      if (rs.next()) {
        System.out.println("Query udført succesfult!");
      }

      conn.close();
      System.out.println("Forbindelse lukket normalt");
    } catch (Exception e) {
      System.out.println("Fejl: " + e.getMessage());
      e.printStackTrace();
    }
  }
}