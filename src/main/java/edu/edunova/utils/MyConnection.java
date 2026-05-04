package edu.edunova.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    private String url = "jdbc:mysql://localhost:3306/edunova";
    private String login = "root";
    private String pwd = "";

    private Connection cnx;
    private static MyConnection instance;

    // Constructeur privé
    private MyConnection() {
        try {
            cnx = DriverManager.getConnection(url, login, pwd);
            System.out.println("Connexion établie !");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }

    // Méthode pour obtenir l'instance unique (Singleton)
    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection(); // La correction est ici
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}