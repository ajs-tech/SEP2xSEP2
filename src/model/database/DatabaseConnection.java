package model.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton klasse til at håndtere database forbindelser med forbindelsespool
 */
public class DatabaseConnection {
  private static ComboPooledDataSource cpds = new ComboPooledDataSource();

  static {
    try {
      // Ændre disse indstillinger til at matche din database
      // For PostgreSQL
      cpds.setDriverClass("org.postgresql.Driver");
      cpds.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
      cpds.setUser("postgres");  // Erstat med dit PostgreSQL-brugernavn
      cpds.setPassword("Seshej1991");  // Erstat med dit PostgreSQL-kodeord

      // Forbindelsespool konfiguration
      cpds.setInitialPoolSize(5);
      cpds.setMinPoolSize(5);
      cpds.setMaxPoolSize(20);
      cpds.setAcquireIncrement(5);
      cpds.setMaxStatements(100);
      cpds.setIdleConnectionTestPeriod(300);
      cpds.setTestConnectionOnCheckin(true);
    } catch (Exception e) {
      System.err.println("FATAL ERROR: Database connection pool initialization failed:");
      e.printStackTrace();
      // I produktionskode kunne du også logge dette eller kaste en RuntimeException
    }
  }

  private DatabaseConnection() {
    // Private konstruktør for singleton
  }

  /**
   * Henter en forbindelse fra poolen
   * @return Database forbindelse
   * @throws SQLException hvis der er problemer med at etablere forbindelsen
   */
  public static Connection getConnection() throws SQLException {
    return cpds.getConnection();
  }

  /**
   * Lukker forbindelsespoolen - kald denne ved programafslutning
   */
  public static void closePool() {
    cpds.close();
  }
}