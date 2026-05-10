package edu.edunova.services;

import edu.edunova.entities.Matiere;
import edu.edunova.utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class MatiereService {

    private final Connection cnx;


    public MatiereService() {
        cnx = MyConnection.getInstance().getCnx();
    }


    public List<Matiere> getData() {
        List<Matiere> matieres = new ArrayList<>();
        String requete = "SELECT * FROM matiere ORDER BY nom_m";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                matieres.add(new Matiere(
                        rs.getInt("id_m"),
                        rs.getString("nom_m"),
                        rs.getInt("coefficient_m")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return matieres;
    }


    public Matiere getById(int id) {
        String requete = "SELECT * FROM matiere WHERE id_m = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Matiere(
                        rs.getInt("id_m"),
                        rs.getString("nom_m"),
                        rs.getInt("coefficient_m")
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }


    public void insertDataTestSiVide() {
        if (!getData().isEmpty()) return;

        String[][] matieres = {
                {"Mathématiques", "4"},
                {"Français", "4"},
                {"Anglais", "3"},
                {"Histoire-Géographie", "2"},
                {"Sciences Physiques", "3"}
        };

        String requete = "INSERT INTO matiere (nom_m, coefficient_m) VALUES (?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            for (String[] m : matieres) {
                pst.setString(1, m[0]);
                pst.setInt(2, Integer.parseInt(m[1]));
                pst.executeUpdate();
            }
            System.out.println("5 matières par défaut insérées.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
