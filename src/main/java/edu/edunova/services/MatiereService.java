package edu.edunova.services;

import edu.edunova.entities.Matiere;
import edu.edunova.interfaces.IService;
import edu.edunova.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatiereService implements IService<Matiere> {

    Connection cnx = MyConnection.getInstance().getCnx();

    @Override
    public void addEntity(Matiere m) {
        String q = "INSERT INTO matiere (nom_m) VALUES (?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(q);
            pst.setString(1, m.getNom_m());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur ajout matiere : " + e.getMessage());
        }
    }

    @Override
    public List<Matiere> getData() {
        List<Matiere> list = new ArrayList<>();
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery("SELECT id_m, nom_m FROM matiere ORDER BY nom_m");
            while (rs.next()) {
                list.add(new Matiere(rs.getInt("id_m"), rs.getString("nom_m")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture matiere : " + e.getMessage());
        }
        return list;
    }

    @Override
    public void deleteEntity(Matiere m) {
        try {
            PreparedStatement pst = cnx.prepareStatement("DELETE FROM matiere WHERE id_m = ?");
            pst.setInt(1, m.getId_m());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur suppression matiere : " + e.getMessage());
        }
    }

    @Override
    public void updateEntity(int id, Matiere m) {
        try {
            PreparedStatement pst = cnx.prepareStatement(
                    "UPDATE matiere SET nom_m = ? WHERE id_m = ?");
            pst.setString(1, m.getNom_m());
            pst.setInt(2, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur update matiere : " + e.getMessage());
        }
    }

    public Matiere findById(int id) {
        try {
            PreparedStatement pst = cnx.prepareStatement(
                    "SELECT id_m, nom_m FROM matiere WHERE id_m = ?");
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Matiere(rs.getInt("id_m"), rs.getString("nom_m"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur findById matiere : " + e.getMessage());
        }
        return null;
    }
}
