package edu.edunova.services;

import edu.edunova.entities.Teacher;
import edu.edunova.utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class TeacherService {

    private final Connection cnx;


    public TeacherService() {
        cnx = MyConnection.getInstance().getCnx();
    }


    public List<Teacher> getData() {
        List<Teacher> teachers = new ArrayList<>();
        String requete = "SELECT * FROM teacher WHERE actif_t = 1 ORDER BY nom_t, prenom_t";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                teachers.add(new Teacher(
                        rs.getInt("id_t"),
                        rs.getString("nom_t"),
                        rs.getString("prenom_t"),
                        rs.getString("specialite_t")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return teachers;
    }

    /**
     * @param id  primary key
     * @return the matching teacher, or {@code null} when not found
     */
    public Teacher getById(int id) {
        String requete = "SELECT * FROM teacher WHERE id_t = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Teacher(
                        rs.getInt("id_t"),
                        rs.getString("nom_t"),
                        rs.getString("prenom_t"),
                        rs.getString("specialite_t")
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Inserts four default teachers when the table is empty. Intended as a
     * convenience for first-time local setup; safe to call repeatedly.
     */
    public void insertDataTestSiVide() {
        if (!getData().isEmpty()) return;

        String[][] profs = {
                {"Ben Ali", "Mohamed", "m.benali@edunova.tn", "20123456", "Mathématiques"},
                {"Trabelsi", "Sonia", "s.trabelsi@edunova.tn", "20234567", "Français"},
                {"Khémiri", "Samir", "s.khemiri@edunova.tn", "20345678", "Histoire-Géo"},
                {"Mansouri", "Lina", "l.mansouri@edunova.tn", "20456789", "Anglais"}
        };

        String requete = "INSERT INTO teacher (nom_t, prenom_t, email_t, telephone_t, specialite_t, actif_t) VALUES (?, ?, ?, ?, ?, 1)";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            for (String[] p : profs) {
                pst.setString(1, p[0]);
                pst.setString(2, p[1]);
                pst.setString(3, p[2]);
                pst.setString(4, p[3]);
                pst.setString(5, p[4]);
                pst.executeUpdate();
            }
            System.out.println("4 enseignants par défaut insérés.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
