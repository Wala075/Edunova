package edu.edunova_a.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton de connexion MySQL avec reconnexion automatique.
 */
public class MyConnection {

    private static final String URL   = "jdbc:mysql://localhost:3306/edunova"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Africa/Tunis"
            + "&zeroDateTimeBehavior=CONVERT_TO_NULL";
    private static final String LOGIN = "root";
    private static final String PWD   = "";

    private Connection cnx;
    private static MyConnection instance;

    private MyConnection() {
        connect();
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    /**
     * Retourne une connexion valide.
     * Si la connexion est nulle ou fermée, elle est recréée automatiquement.
     */
    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed() || !cnx.isValid(2)) {
                System.out.println("[MyConnection] Reconnexion à MySQL...");
                connect();
            }
        } catch (SQLException e) {
            System.err.println("[MyConnection] Vérification connexion échouée : " + e.getMessage());
            connect();
        }
        return cnx;
    }

    private void connect() {
        try {
            cnx = DriverManager.getConnection(URL, LOGIN, PWD);
            System.out.println("[MyConnection] Connexion établie !");
        } catch (SQLException e) {
            System.err.println("[MyConnection] Erreur de connexion : " + e.getMessage());
            System.err.println("[MyConnection] Vérifiez que MySQL est démarré sur le port 3306");
            cnx = null;
        }
    }
}
