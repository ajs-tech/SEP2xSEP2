package model.database;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class SimpleDatabaseTest {
  public static void main(String[] args) {
    try {
      System.out.println("Tester simpel database forbindelse...");

      // Simplere direkte forbindelse uden pool
      Connection conn = java.sql.DriverManager.getConnection(
              "jdbc:postgresql://localhost:5432/postgres",
              "postgres",
              "Seshej1991");

      System.out.println("Forbindelse oprettet!");

      // Test med simpel query
      Statement stmt = conn.createStatement();
      stmt.execute("SELECT 1");
      System.out.println("Query udført succesfult!");

      // Luk forbindelsen
      conn.close();
      System.out.println("Forbindelse lukket normalt");

    } catch (SQLException e) {
      System.out.println("Database fejl: " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      System.out.println("Generel fejl: " + e.getMessage());
      e.printStackTrace();
    }
  }
}