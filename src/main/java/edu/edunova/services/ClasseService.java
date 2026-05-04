package edu.edunova.services;

import edu.edunova.entities.Classe;
import edu.edunova.entities.Matiere;
import edu.edunova.entities.Student;
import edu.edunova.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClasseService {

    private final Connection cnx = MyConnection.getInstance().getCnx();

    /** Returns all classes ordered by name. */
    public List<Classe> getData() {
        List<Classe> list = new ArrayList<>();
        String q = "SELECT id, nom, niveau, capacite FROM classe ORDER BY nom";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(q)) {
            while (rs.next()) {
                list.add(new Classe(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("niveau"),
                        rs.getInt("capacite")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement classes : " + e.getMessage());
        }
        return list;
    }

    /** Returns only the students that belong to the given class. */
    public List<Student> getStudentsByClasse(int classeId) {
        List<Student> list = new ArrayList<>();
        String q = "SELECT id_s, nom_s, prenom_s, classe_id FROM student " +
                   "WHERE classe_id = ? ORDER BY nom_s, prenom_s";
        try (PreparedStatement pst = cnx.prepareStatement(q)) {
            pst.setInt(1, classeId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new Student(
                        rs.getInt("id_s"),
                        rs.getString("nom_s"),
                        rs.getString("prenom_s"),
                        rs.getInt("classe_id")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement élèves par classe : " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns the distinct subjects that students of a given class have notes in.
     * Join path: classe → student (classe_id) → note (student_id) → matiere (matiere_id)
     */
    public List<Matiere> getMatieresByClasse(int classeId) {
        List<Matiere> list = new ArrayList<>();
        String q = "SELECT DISTINCT m.id_m, m.nom_m, m.coefficient_m " +
                   "FROM matiere m " +
                   "JOIN note n ON n.matiere_id = m.id_m " +
                   "JOIN student s ON s.id_s = n.student_id " +
                   "WHERE s.classe_id = ? " +
                   "ORDER BY m.nom_m";
        try (PreparedStatement pst = cnx.prepareStatement(q)) {
            pst.setInt(1, classeId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new Matiere(
                        rs.getInt("id_m"),
                        rs.getString("nom_m"),
                        rs.getInt("coefficient_m")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement matières par classe : " + e.getMessage());
        }
        return list;
    }
}
