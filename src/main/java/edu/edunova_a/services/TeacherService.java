package edu.edunova_a.services;

import edu.edunova_a.entities.Teacher;
import edu.edunova_a.utils.MyConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TeacherService {

    private final Connection cnx = MyConnection.getInstance().getCnx();

    public List<Teacher> afficher() throws SQLException {
        List<Teacher> list = new ArrayList<>();
        String sql = "SELECT id_t, nom_t, prenom_t, email_t, specialite_t " +
                "FROM teacher ORDER BY nom_t, prenom_t";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Teacher(
                        rs.getInt("id_t"),
                        rs.getString("nom_t"),
                        rs.getString("prenom_t"),
                        rs.getString("email_t"),
                        rs.getString("specialite_t")
                ));
            }
        }
        return list;
    }
}
