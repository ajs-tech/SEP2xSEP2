package model.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton klasse til at håndtere database forbindelser med forbindelsespool og overvågning
 */
public class DatabaseConnection {
  private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());
  private static ComboPooledDataSource cpds = new ComboPooledDataSource();
  private static boolean initialized = false;
  private static boolean poolClosed = false;

  static {
    try {
      // Konfigurer connection pool
      initializeConnectionPool();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Fejl ved initialisering af database forbindelsespool", e);
    }
  }

  private static void initializeConnectionPool() {
    if (initialized) {
      return;
    }

    try {
      // Ændre disse indstillinger til at matche din database
      // For PostgreSQL
      cpds.setDriverClass("org.postgresql.Driver");
      cpds.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
      cpds.setUser("postgres");  // Erstat med dit PostgreSQL-brugernavn
      cpds.setPassword("Seshej1991");  // Erstat med dit PostgreSQL-kodeord

      // Forbindelsespool konfiguration
      // I DatabaseConnection.java, ændr connection pool konfigurationen:
// Forbindelsespool konfiguration
      cpds.setInitialPoolSize(3);     // Reducer fra 5
      cpds.setMinPoolSize(2);         // Reducer fra 5
      cpds.setMaxPoolSize(10);        // Reducer fra 20
      cpds.setAcquireIncrement(1);    // Reducer fra 5
      cpds.setMaxStatements(50);      // Reducer fra 100
      cpds.setIdleConnectionTestPeriod(60); // Reducer fra 300 sekunder
      cpds.setCheckoutTimeout(20000); // Øg timeout til 20 sekunder

      // Forbedret timeout indstillinger
      // 10 sekunder timeout for at hente en forbindelse
      cpds.setMaxIdleTime(1800); // 30 minutter maksimal inaktiv tid
      cpds.setMaxConnectionAge(14400); // 4 timer maksimal levetid for en forbindelse

      // Konfigurer automatisk test af forbindelser
      cpds.setTestConnectionOnCheckout(false); // Undgå test ved hver forbindelse for at forbedre ydelse
      cpds.setPreferredTestQuery("SELECT 1"); // Simpel test forespørgsel
      cpds.setAutomaticTestTable("c3p0_test_table"); // Tabel til test (opret denne hvis du bruger denne metode)

      // Konfigurer genoprettelses-forsøg
      cpds.setAcquireRetryAttempts(3);
      cpds.setAcquireRetryDelay(1000); // 1 sekund mellem forsøg

      initialized = true;
      poolClosed = false;
      logger.info("Database forbindelsespool initialiseret");
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Fejl ved konfiguration af database forbindelsespool", e);
      throw new RuntimeException("Kunne ikke initialisere database forbindelsespool", e);
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
    if (poolClosed) {
      throw new SQLException("Connection pool er lukket");
    }

    if (!initialized) {
      initializeConnectionPool();
    }

    try {
      Connection conn = cpds.getConnection();
      // Log forbindelseshentning ved højt debug niveau
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Database forbindelse hentet fra pool");
      }
      return conn;
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Fejl ved hentning af database forbindelse: " + e.getMessage(), e);

      // Log pool status ved fejl
      logPoolStatus();

      throw e;
    }
  }

  /**
   * Lukker forbindelsespoolen - kald denne ved programafslutning
   */
  public static void closePool() {
    if (cpds != null && !poolClosed) {
      logger.info("Lukker database forbindelsespool");
      cpds.close();
      poolClosed = true;
      initialized = false;
    }
  }

  /**
   * Tester om databasen er tilgængelig
   * @return true hvis databasen er tilgængelig
   */
  public static boolean testConnection() {
    try (Connection conn = getConnection()) {
      return conn != null && !conn.isClosed();
    } catch (SQLException e) {
      logger.log(Level.WARNING, "Database forbindelsestest fejlede: " + e.getMessage(), e);
      return false;
    }
  }

  /**
   * Henter statistik om connection pool status
   * @return String med pool statistik
   */
  public static String getPoolStats() {
    try {
      StringBuilder stats = new StringBuilder();
      stats.append("Database Connection Pool Status:\n");
      stats.append("  Connections in use: ").append(cpds.getNumBusyConnections()).append("\n");
      stats.append("  Idle connections: ").append(cpds.getNumIdleConnections()).append("\n");
      stats.append("  Total connections: ").append(cpds.getNumConnections()).append("\n");
      stats.append("  Threads waiting: ").append(cpds.getThreadPoolNumActiveThreads()).append("\n");
      return stats.toString();
    } catch (SQLException e) {
      logger.log(Level.WARNING, "Fejl ved hentning af pool statistik", e);
      return "Kunne ikke hente pool statistik: " + e.getMessage();
    }
  }

  /**
   * Logger detaljeret statistik om connection pool status
   */
  public static void logPoolStatus() {
    try {
      logger.info(getPoolStats());
    } catch (Exception e) {
      logger.log(Level.WARNING, "Fejl ved logging af pool status", e);
    }
  }

  /**
   * Forsøger at geninitialisere connection pool ved alvorlige problemer
   */
  public static void reinitializePool() {
    logger.warning("Geninitialiserer database forbindelsespool");
    try {
      // Luk den nuværende pool
      if (cpds != null && !poolClosed) {
        cpds.close();
        poolClosed = true;
      }

      // Opret en ny pool
      cpds = new ComboPooledDataSource();
      initialized = false;
      initializeConnectionPool();

      logger.info("Database forbindelsespool geninitialiseret succesfuldt");
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Fejl ved geninitialisering af database forbindelsespool", e);
      throw new RuntimeException("Kunne ikke geninitialisere database forbindelsespool", e);
    }
  }
}