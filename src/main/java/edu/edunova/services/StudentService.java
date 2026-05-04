package edu.edunova.services;

import edu.edunova.entities.Student;
import edu.edunova.interfaces.IService;
import edu.edunova.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentService implements IService<Student> {

    Connection cnx = MyConnection.getInstance().getCnx();

    @Override
    public void addEntity(Student s) {
        String q = "INSERT INTO student (nom_s, prenom_s) VALUES (?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(q);
            pst.setString(1, s.getNom_s());
            pst.setString(2, s.getPrenom_s());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur ajout student : " + e.getMessage());
        }
    }

    @Override
    public List<Student> getData() {
        List<Student> list = new ArrayList<>();
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT id_s, nom_s, prenom_s FROM student ORDER BY nom_s, prenom_s");
            while (rs.next()) {
                list.add(new Student(
                        rs.getInt("id_s"),
                        rs.getString("nom_s"),
                        rs.getString("prenom_s")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture student : " + e.getMessage());
        }
        return list;
    }

    /** Liste complète avec contacts parents (pour la page Gestion parents). */
    public List<Student> getAllWithContacts() {
        List<Student> list = new ArrayList<>();
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT id_s, nom_s, prenom_s, email_parent, telephone_parent " +
                "FROM student ORDER BY nom_s, prenom_s")) {
            while (rs.next()) {
                Student s = new Student(
                        rs.getInt("id_s"),
                        rs.getString("nom_s"),
                        rs.getString("prenom_s"));
                s.setEmail_parent(rs.getString("email_parent"));
                s.setTelephone_parent(rs.getString("telephone_parent"));
                list.add(s);
            }
        } catch (SQLException e) {
            // Fallback si colonnes pas encore ajoutées
            System.err.println("getAllWithContacts erreur, fallback : " + e.getMessage());
            return getData();
        }
        return list;
    }

    @Override
    public void deleteEntity(Student s) {
        try {
            PreparedStatement pst = cnx.prepareStatement("DELETE FROM student WHERE id_s = ?");
            pst.setInt(1, s.getId_s());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur suppression student : " + e.getMessage());
        }
    }

    @Override
    public void updateEntity(int id, Student s) {
        try {
            PreparedStatement pst = cnx.prepareStatement(
                    "UPDATE student SET nom_s = ?, prenom_s = ? WHERE id_s = ?");
            pst.setString(1, s.getNom_s());
            pst.setString(2, s.getPrenom_s());
            pst.setInt(3, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur update student : " + e.getMessage());
        }
    }

    public int countAll() {
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM student");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur count students : " + e.getMessage());
        }
        return 0;
    }

    public Student findById(int id) {
        try {
            PreparedStatement pst = cnx.prepareStatement(
                    "SELECT id_s, nom_s, prenom_s, email_parent, telephone_parent " +
                    "FROM student WHERE id_s = ?");
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Student s = new Student(
                        rs.getInt("id_s"),
                        rs.getString("nom_s"),
                        rs.getString("prenom_s"));
                s.setEmail_parent(rs.getString("email_parent"));
                s.setTelephone_parent(rs.getString("telephone_parent"));
                return s;
            }
        } catch (SQLException e) {
            // Si les colonnes email_parent/telephone_parent n'existent pas (migration non exécutée),
            // on retombe sur la requête simple.
            try {
                PreparedStatement pst = cnx.prepareStatement(
                        "SELECT id_s, nom_s, prenom_s FROM student WHERE id_s = ?");
                pst.setInt(1, id);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    return new Student(
                            rs.getInt("id_s"),
                            rs.getString("nom_s"),
                            rs.getString("prenom_s"));
                }
            } catch (SQLException e2) {
                System.err.println("Erreur findById student : " + e2.getMessage());
            }
        }
        return null;
    }

    /** Met à jour l'email parent (utilisé avant envoi Brevo si vide). */
    public void updateEmailParent(int studentId, String email) {
        try (PreparedStatement pst = cnx.prepareStatement(
                "UPDATE student SET email_parent = ? WHERE id_s = ?")) {
            pst.setString(1, email);
            pst.setInt(2, studentId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("StudentService.updateEmailParent : " + e.getMessage());
        }
    }

    /** Met à jour le téléphone parent. */
    public void updateTelephoneParent(int studentId, String tel) {
        try (PreparedStatement pst = cnx.prepareStatement(
                "UPDATE student SET telephone_parent = ? WHERE id_s = ?")) {
            pst.setString(1, tel);
            pst.setInt(2, studentId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("StudentService.updateTelephoneParent : " + e.getMessage());
        }
    }
}
