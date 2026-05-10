package edu.edunova_a.services;

import edu.edunova_a.entities.Classe;
import edu.edunova_a.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClasseService {
    private final Connection cnx = MyConnection.getInstance().getCnx();

    public List<Classe> afficher() throws SQLException {
        List<Classe> list = new ArrayList<>();
        String sql = "SELECT id, nom, niveau, capacite FROM classe ORDER BY nom";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Classe(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("niveau"),
                        rs.getInt("capacite")
                ));
            }
        }
        return list;
    }

    public Classe getById(int id) throws SQLException {
        String sql = "SELECT id, nom, niveau, capacite FROM classe WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Classe(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("niveau"),
                            rs.getInt("capacite")
                    );
                }
            }
        }
        return null;
    }
}
