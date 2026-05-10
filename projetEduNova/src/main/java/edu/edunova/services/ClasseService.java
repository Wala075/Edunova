package edu.edunova.services;

import edu.edunova.entities.Classe;
import edu.edunova.interfaces.IClasseService;
import edu.edunova.utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class ClasseService implements IClasseService {

    private final Connection cnx;

    /** Initialises the service with the shared MySQL connection. */
    public ClasseService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    @Override
    public void addEntity(Classe classe) {
        String requete = "INSERT INTO classe (nom, niveau, capacite) VALUES (?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setString(1, classe.getNom());
            pst.setString(2, classe.getNiveau());
            pst.setInt(3, classe.getCapacite());
            pst.executeUpdate();
            System.out.println("Classe ajoutée!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public void deleteEntity(Classe classe) {
        String details = getReferencesDetails(classe.getId());
        if (!details.isEmpty()) {
            System.out.println("Suppression bloquée! Cette classe contient:\n" + details);
            return;
        }

        String requete = "DELETE FROM classe WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setInt(1, classe.getId());
            pst.executeUpdate();
            System.out.println("Classe supprimée!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateEntity(int id, Classe classe) {
        String requete = "UPDATE classe SET nom = ?, niveau = ?, capacite = ? WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setString(1, classe.getNom());
            pst.setString(2, classe.getNiveau());
            pst.setInt(3, classe.getCapacite());
            pst.setInt(4, id);
            pst.executeUpdate();
            System.out.println("Classe modifiée!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Classe> getData() {
        List<Classe> classes = new ArrayList<>();
        String requete = "SELECT * FROM classe ORDER BY niveau, nom";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                classes.add(new Classe(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("niveau"),
                        rs.getInt("capacite")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return classes;
    }

    @Override
    public Classe getById(int id) {
        String requete = "SELECT * FROM classe WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Classe(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("niveau"),
                        rs.getInt("capacite")
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public String getReferencesDetails(int classeId) {
        StringBuilder sb = new StringBuilder();
        sb.append(checkRef("student", classeId, "élève(s)"));
        sb.append(checkRef("seance", classeId, "séance(s)"));
        sb.append(checkRef("live_session", classeId, "session(s) en ligne"));
        return sb.toString();
    }


    private String checkRef(String table, int classeId, String label) {
        String requete = "SELECT COUNT(*) FROM " + table + " WHERE classe_id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setInt(1, classeId);
            ResultSet rs = pst.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return "  • " + rs.getInt(1) + " " + label + "\n";
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
}
