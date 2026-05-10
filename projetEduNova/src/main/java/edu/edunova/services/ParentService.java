package edu.edunova.services;

import edu.edunova.entities.Parent;
import edu.edunova.interfaces.IParentService;
import edu.edunova.utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;


public class ParentService implements IParentService {

    private final Connection cnx;


    public ParentService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    @Override
    public void addEntity(Parent p) {
        String requete = "INSERT INTO parent (nom_par, prenom_par, email_par, classe_id) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setString(1, p.getNom());
            pst.setString(2, p.getPrenom() != null ? p.getPrenom() : "");
            pst.setString(3, p.getEmail());
            if (p.getClasseId() != null) {
                pst.setInt(4, p.getClasseId());
            } else {
                pst.setNull(4, Types.INTEGER);
            }
            pst.executeUpdate();
            System.out.println("Parent ajouté!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteEntity(Parent p) {
        String requete = "DELETE FROM parent WHERE id_par = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setInt(1, p.getId());
            pst.executeUpdate();
            System.out.println("Parent supprimé!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateEntity(int id, Parent p) {
        String requete = "UPDATE parent SET nom_par = ?, prenom_par = ?, email_par = ?, classe_id = ? WHERE id_par = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setString(1, p.getNom());
            pst.setString(2, p.getPrenom() != null ? p.getPrenom() : "");
            pst.setString(3, p.getEmail());
            if (p.getClasseId() != null) {
                pst.setInt(4, p.getClasseId());
            } else {
                pst.setNull(4, Types.INTEGER);
            }
            pst.setInt(5, id);
            pst.executeUpdate();
            System.out.println("Parent modifié!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Parent> getData() {
        List<Parent> list = new ArrayList<>();
        String requete = "SELECT p.id_par, p.nom_par, p.prenom_par, p.email_par, p.classe_id, c.nom AS classe_nom "
                + "FROM parent p LEFT JOIN classe c ON p.classe_id = c.id "
                + "ORDER BY c.nom, p.nom_par";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                int classeId = rs.getInt("classe_id");
                Integer classeIdValue = rs.wasNull() ? null : classeId;
                list.add(new Parent(
                        rs.getInt("id_par"),
                        rs.getString("nom_par"),
                        rs.getString("prenom_par"),
                        rs.getString("email_par"),
                        classeIdValue,
                        rs.getString("classe_nom")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    @Override
    public List<Parent> getByClasseId(int classeId) {
        List<Parent> list = new ArrayList<>();
        String requete = "SELECT p.id_par, p.nom_par, p.prenom_par, p.email_par, p.classe_id, c.nom AS classe_nom "
                + "FROM parent p LEFT JOIN classe c ON p.classe_id = c.id "
                + "WHERE p.classe_id = ? ORDER BY p.nom_par";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setInt(1, classeId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new Parent(
                        rs.getInt("id_par"),
                        rs.getString("nom_par"),
                        rs.getString("prenom_par"),
                        rs.getString("email_par"),
                        rs.getInt("classe_id"),
                        rs.getString("classe_nom")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }
}
