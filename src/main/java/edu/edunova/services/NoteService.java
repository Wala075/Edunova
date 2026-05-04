package edu.edunova.services;

import edu.edunova.entities.Note;
import edu.edunova.interfaces.INoteService;
import edu.edunova.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteService implements INoteService {

    Connection cnx = MyConnection.getInstance().getCnx();

    @Override
    public void addEntity(Note n) {
        String query = "INSERT INTO note (valeur, coefficient, type_eval, trimestre, " +
                "date_saisie, student_id, matiere_id, annee_scolaire) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setDouble(1, n.getValeur());
            pst.setInt(2, n.getCoefficient());
            pst.setString(3, n.getType_eval());
            pst.setInt(4, n.getTrimestre());
            // ✅ CORRECTION : conversion java.util.Date → java.sql.Date
            pst.setDate(5, new java.sql.Date(n.getDate_saisie().getTime()));
            pst.setInt(6, n.getStudent_id());
            pst.setInt(7, n.getMatiere_id());
            pst.setString(8, n.getAnnee_scolaire());

            pst.executeUpdate();
            System.out.println("Note ajoutée avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur d'ajout : " + e.getMessage());
        }
    }

    @Override
    public List<Note> getData() {
        List<Note> notes = new ArrayList<>();
        String query = "SELECT * FROM note";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                Note n = new Note();
                n.setId_n(rs.getInt("id_n"));
                n.setValeur(rs.getDouble("valeur"));
                n.setCoefficient(rs.getInt("coefficient"));
                n.setType_eval(rs.getString("type_eval"));
                n.setTrimestre(rs.getInt("trimestre"));
                n.setDate_saisie(rs.getDate("date_saisie"));
                n.setStudent_id(rs.getInt("student_id"));
                n.setMatiere_id(rs.getInt("matiere_id"));
                n.setAnnee_scolaire(rs.getString("annee_scolaire"));
                notes.add(n);
            }
        } catch (SQLException e) {
            System.err.println("Erreur de lecture : " + e.getMessage());
        }
        return notes;
    }

    // ✅ DELETE - Compléter
    @Override
    public void deleteEntity(Note n) {
        String query = "DELETE FROM note WHERE id_n = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, n.getId_n());
            pst.executeUpdate();
            System.out.println("Note supprimée avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur de suppression : " + e.getMessage());
        }
    }

    // ✅ UPDATE - Compléter
    @Override
    public void updateEntity(int id, Note n) {
        String query = "UPDATE note SET valeur = ?, coefficient = ?, type_eval = ?, " +
                "trimestre = ?, date_saisie = ?, student_id = ?, matiere_id = ?, " +
                "annee_scolaire = ? WHERE id_n = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setDouble(1, n.getValeur());
            pst.setInt(2, n.getCoefficient());
            pst.setString(3, n.getType_eval());
            pst.setInt(4, n.getTrimestre());
            pst.setDate(5, new java.sql.Date(n.getDate_saisie().getTime()));
            pst.setInt(6, n.getStudent_id());
            pst.setInt(7, n.getMatiere_id());
            pst.setString(8, n.getAnnee_scolaire());
            pst.setInt(9, id);
            pst.executeUpdate();
            System.out.println("Note modifiée avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur de modification : " + e.getMessage());
        }
    }

    // ✅ Notes par étudiant
    @Override
    public List<Note> getNotesByStudent(int studentId) {
        List<Note> notes = new ArrayList<>();
        String query = "SELECT * FROM note WHERE student_id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, studentId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Note n = new Note();
                n.setId_n(rs.getInt("id_n"));
                n.setValeur(rs.getDouble("valeur"));
                n.setCoefficient(rs.getInt("coefficient"));
                n.setType_eval(rs.getString("type_eval"));
                n.setTrimestre(rs.getInt("trimestre"));
                n.setDate_saisie(rs.getDate("date_saisie"));
                n.setStudent_id(rs.getInt("student_id"));
                n.setMatiere_id(rs.getInt("matiere_id"));
                n.setAnnee_scolaire(rs.getString("annee_scolaire"));
                notes.add(n);
            }
        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
        return notes;
    }

    // ✅ Calculer moyenne d'un étudiant
    @Override
    public double calculerMoyenne(int studentId) {
        String query = "SELECT SUM(valeur * coefficient) / SUM(coefficient) AS moyenne " +
                "FROM note WHERE student_id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, studentId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getDouble("moyenne");
        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
        return 0;
    }

    // ✅ Moyenne d'une classe pour une matière
    @Override
    public double calculerMoyenneClasse(int matiereId, String annee) {
        String query = "SELECT AVG(valeur) AS moyenne FROM note " +
                "WHERE matiere_id = ? AND annee_scolaire = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, matiereId);
            pst.setString(2, annee);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getDouble("moyenne");
        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
        return 0;
    }

    // ✅ Nombre d'élèves distincts ayant au moins une note dans une matière
    public int countStudentsByMatiere(int matiereId, String annee) {
        String q = "SELECT COUNT(DISTINCT student_id) FROM note " +
                "WHERE matiere_id = ? AND annee_scolaire = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(q);
            pst.setInt(1, matiereId);
            pst.setString(2, annee);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur countStudentsByMatiere : " + e.getMessage());
        }
        return 0;
    }

    // ✅ Total notes saisies dans une année scolaire
    public int countTotalByAnnee(String annee) {
        String q = "SELECT COUNT(*) FROM note WHERE annee_scolaire = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(q);
            pst.setString(1, annee);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur countTotalByAnnee : " + e.getMessage());
        }
        return 0;
    }

    // ✅ Notes par mois pour le chart (retourne map mois 1-12 -> count)
    public java.util.Map<Integer, Integer> countByMonth(String annee) {
        java.util.Map<Integer, Integer> map = new java.util.HashMap<>();
        for (int i = 1; i <= 12; i++) map.put(i, 0);
        String q = "SELECT MONTH(date_saisie) AS m, COUNT(*) AS c FROM note " +
                "WHERE annee_scolaire = ? GROUP BY MONTH(date_saisie)";
        try {
            PreparedStatement pst = cnx.prepareStatement(q);
            pst.setString(1, annee);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) map.put(rs.getInt("m"), rs.getInt("c"));
        } catch (SQLException e) {
            System.err.println("Erreur countByMonth : " + e.getMessage());
        }
        return map;
    }
}