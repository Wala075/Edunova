package edu.edunova.services;

import edu.edunova.entities.Bulletin;
import edu.edunova.entities.Bulletin.BulletinLigne;
import edu.edunova.utils.MyConnection;

import java.sql.*;

public class BulletinService {

    Connection cnx = MyConnection.getInstance().getCnx();

    /**
     * Génère le bulletin d'un élève pour un trimestre + année donnés.
     * Calcule pour chaque matière la moyenne pondérée des notes (par coefficient des notes),
     * puis la moyenne générale = moyenne des moyennes par matière.
     */
    public Bulletin genererBulletin(int studentId, int trimestre, String annee) {
        Bulletin b = new Bulletin();
        b.setStudent(new StudentService().findById(studentId));
        b.setTrimestre(trimestre);
        b.setAnnee(annee);

        String q = "SELECT m.id_m, m.nom_m, " +
                "       COUNT(n.id_n) AS nb, " +
                "       SUM(n.valeur * n.coefficient) / SUM(n.coefficient) AS moyenne " +
                "FROM note n " +
                "JOIN matiere m ON n.matiere_id = m.id_m " +
                "WHERE n.student_id = ? AND n.trimestre = ? AND n.annee_scolaire = ? " +
                "GROUP BY m.id_m, m.nom_m " +
                "ORDER BY m.nom_m";

        try {
            PreparedStatement pst = cnx.prepareStatement(q);
            pst.setInt(1, studentId);
            pst.setInt(2, trimestre);
            pst.setString(3, annee);
            ResultSet rs = pst.executeQuery();

            double sum = 0;
            int count = 0;
            while (rs.next()) {
                BulletinLigne l = new BulletinLigne(
                        rs.getInt("id_m"),
                        rs.getString("nom_m"),
                        rs.getInt("nb"),
                        rs.getDouble("moyenne"));
                b.addLigne(l);
                sum += l.getMoyenne();
                count++;
            }
            if (count > 0) {
                b.setMoyenneGenerale(sum / count);
            }
        } catch (SQLException e) {
            System.err.println("Erreur génération bulletin : " + e.getMessage());
        }
        return b;
    }
}
