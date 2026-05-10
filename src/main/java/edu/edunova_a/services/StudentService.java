package edu.edunova_a.services;

import edu.edunova_a.entities.Student;
import edu.edunova_a.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentService {

    private final Connection cnx = MyConnection.getInstance().getCnx();

    private static final String SELECT_FIELDS =
            "id_s, nom_s, prenom_s, date_naissance_s, classe_id, tel_parent, nom_parent ";

    public List<Student> afficher() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT " + SELECT_FIELDS + "FROM student ORDER BY nom_s, prenom_s";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** Récupère un étudiant complet (avec contact parent) par son matricule. */
    public Student findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_FIELDS + "FROM student WHERE id_s = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    private Student map(ResultSet rs) throws SQLException {
        Date d = rs.getDate("date_naissance_s");
        int classeId = rs.getObject("classe_id") != null ? rs.getInt("classe_id") : 0;
        Student s = new Student(rs.getInt("id_s"), rs.getString("nom_s"),
                rs.getString("prenom_s"), classeId);
        s.setDate_naissance_s(d != null ? d.toString() : null);
        s.setTel_parent(rs.getString("tel_parent"));
        s.setNom_parent(rs.getString("nom_parent"));
        return s;
    }
}

