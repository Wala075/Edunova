package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection — fournit une connexion JDBC à MySQL.
 *
 * FIX (thread-safety) : l'ancienne implémentation partageait une seule
 * instance Connection entre tous les threads (services Forum, Like,
 * Comment, Notification…). Les requêtes concurrentes corrompaient la
 * connexion partagée.
 *
 * Solution : getInstance() crée désormais une nouvelle connexion à chaque
 * appel. Toutes les méthodes de service utilisent déjà le pattern
 * try-with-resources, donc chaque connexion est fermée automatiquement
 * après usage — aucune fuite de ressource.
 *
 * Pour une application en production, remplacez cette classe par un pool
 * de connexions (HikariCP) en ajoutant la dépendance dans pom.xml :
 *   <dependency>
 *       <groupId>com.zaxxer</groupId>
 *       <artifactId>HikariCP</artifactId>
 *       <version>5.1.0</version>
 *   </dependency>
 */
public class DatabaseConnection {

    private static final String URL =
        "jdbc:mysql://localhost:3306/edunova" +
        "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private DatabaseConnection() {}

    /**
     * Returns a new Connection each time.
     * Callers MUST close it (use try-with-resources).
     */
    public static Connection getInstance() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL introuvable : " + e.getMessage());
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * No-op kept for backward compatibility (MainApp.stop() calls this).
     * With per-call connections there is nothing to close globally.
     */
    public static void close() {
        // nothing to do — each connection is closed by its try-with-resources block
    }
}
