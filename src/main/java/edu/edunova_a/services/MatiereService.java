package edu.edunova_a.services;
import edu.edunova_a.entities.Matiere;
import edu.edunova_a.utils.MyConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MatiereService {

    private final Connection cnx = MyConnection.getInstance().getCnx();

    public List<Matiere> afficher() throws SQLException {
        List<Matiere> list = new ArrayList<>();
        String sql = "SELECT id_m, nom_m, coefficient_m FROM matiere ORDER BY nom_m";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int coef = rs.getObject("coefficient_m") != null
                        ? rs.getInt("coefficient_m") : 0;
                list.add(new Matiere(rs.getInt("id_m"), rs.getString("nom_m"), coef));
            }
        }
        return list;
    }
}