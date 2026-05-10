package edu.edunova.services;

import edu.edunova.entities.Seance;
import edu.edunova.interfaces.ISeanceService;
import edu.edunova.utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class SeanceService implements ISeanceService {

    private final Connection cnx;


    public SeanceService() {
        cnx = MyConnection.getInstance().getCnx();
    }


    @Override
    public void addEntity(Seance seance) {
        String conflits = detecterConflits(seance);
        if (!conflits.isEmpty()) {
            System.out.println("Ajout bloqué — conflit détecté:\n" + conflits);
            return;
        }

        String requete = "INSERT INTO seance (jour_se, heure_debut_se, heure_fin_se, salle_se, " +
                "type_cours_se, annee_scolaire, classe_id, matiere_id, teacher_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setString(1, seance.getJour());
            pst.setTime(2, seance.getHeureDebut());
            pst.setTime(3, seance.getHeureFin());
            pst.setString(4, seance.getSalle());
            pst.setString(5, seance.getTypeCours());
            pst.setString(6, seance.getAnneeScolaire());
            pst.setInt(7, seance.getClasseId());
            pst.setInt(8, seance.getMatiereId());
            pst.setInt(9, seance.getTeacherId());
            pst.executeUpdate();
            System.out.println("Séance ajoutée!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteEntity(Seance seance) {
        String requete = "DELETE FROM seance WHERE id_se = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setInt(1, seance.getId());
            pst.executeUpdate();
            System.out.println("Séance supprimée!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public void updateEntity(int id, Seance seance) {
        String conflits = detecterConflitsHorsSoi(seance, id);
        if (!conflits.isEmpty()) {
            System.out.println("Modification bloquée — conflit détecté:\n" + conflits);
            return;
        }
        updateEntityNoCheck(id, seance);
    }

    /**
     * Direct UPDATE without conflict detection. Use only when the caller has
     * already validated conflicts (e.g. during a swap, when both rows must
     * temporarily occupy each other's slots).
     */
    public void updateEntityNoCheck(int id, Seance seance) {
        String requete = "UPDATE seance SET jour_se = ?, heure_debut_se = ?, heure_fin_se = ?, " +
                "salle_se = ?, type_cours_se = ?, annee_scolaire = ?, " +
                "classe_id = ?, matiere_id = ?, teacher_id = ? WHERE id_se = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setString(1, seance.getJour());
            pst.setTime(2, seance.getHeureDebut());
            pst.setTime(3, seance.getHeureFin());
            pst.setString(4, seance.getSalle());
            pst.setString(5, seance.getTypeCours());
            pst.setString(6, seance.getAnneeScolaire());
            pst.setInt(7, seance.getClasseId());
            pst.setInt(8, seance.getMatiereId());
            pst.setInt(9, seance.getTeacherId());
            pst.setInt(10, id);
            pst.executeUpdate();
            System.out.println("Séance modifiée!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public List<Seance> getData() {
        List<Seance> seances = new ArrayList<>();
        String requete = "SELECT se.*, " +
                "c.nom AS classe_nom, " +
                "m.nom_m AS matiere_nom, " +
                "CONCAT(t.prenom_t, ' ', t.nom_t) AS teacher_nom " +
                "FROM seance se " +
                "LEFT JOIN classe c ON se.classe_id = c.id " +
                "LEFT JOIN matiere m ON se.matiere_id = m.id_m " +
                "LEFT JOIN teacher t ON se.teacher_id = t.id_t " +
                "ORDER BY FIELD(se.jour_se, 'LUNDI','MARDI','MERCREDI','JEUDI','VENDREDI','SAMEDI'), " +
                "se.heure_debut_se";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                Seance s = new Seance(
                        rs.getInt("id_se"),
                        rs.getString("jour_se"),
                        rs.getTime("heure_debut_se"),
                        rs.getTime("heure_fin_se"),
                        rs.getString("salle_se"),
                        rs.getString("type_cours_se"),
                        rs.getString("annee_scolaire"),
                        rs.getInt("classe_id"),
                        rs.getInt("matiere_id"),
                        rs.getInt("teacher_id")
                );
                s.setClasseNom(rs.getString("classe_nom"));
                s.setMatiereNom(rs.getString("matiere_nom"));
                s.setTeacherNom(rs.getString("teacher_nom"));
                seances.add(s);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return seances;
    }

    @Override
    public Seance getById(int id) {
        String requete = "SELECT se.*, c.nom AS classe_nom, m.nom_m AS matiere_nom, " +
                "CONCAT(t.prenom_t, ' ', t.nom_t) AS teacher_nom " +
                "FROM seance se " +
                "LEFT JOIN classe c ON se.classe_id = c.id " +
                "LEFT JOIN matiere m ON se.matiere_id = m.id_m " +
                "LEFT JOIN teacher t ON se.teacher_id = t.id_t " +
                "WHERE se.id_se = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Seance s = new Seance(
                        rs.getInt("id_se"),
                        rs.getString("jour_se"),
                        rs.getTime("heure_debut_se"),
                        rs.getTime("heure_fin_se"),
                        rs.getString("salle_se"),
                        rs.getString("type_cours_se"),
                        rs.getString("annee_scolaire"),
                        rs.getInt("classe_id"),
                        rs.getInt("matiere_id"),
                        rs.getInt("teacher_id")
                );
                s.setClasseNom(rs.getString("classe_nom"));
                s.setMatiereNom(rs.getString("matiere_nom"));
                s.setTeacherNom(rs.getString("teacher_nom"));
                return s;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public List<Seance> getByClasse(int classeId) {
        List<Seance> result = new ArrayList<>();
        for (Seance s : getData()) {
            if (s.getClasseId() == classeId) result.add(s);
        }
        return result;
    }

    @Override
    public List<Seance> getByTeacher(int teacherId) {
        List<Seance> result = new ArrayList<>();
        for (Seance s : getData()) {
            if (s.getTeacherId() == teacherId) result.add(s);
        }
        return result;
    }

    @Override
    public String detecterConflits(Seance seance) {
        return detecterConflitsHorsSoi(seance, -1);
    }

    private String detecterConflitsHorsSoi(Seance seance, int excludeId) {
        StringBuilder sb = new StringBuilder();
        String requete = "SELECT se.*, " +
                "c.nom AS classe_nom, m.nom_m AS matiere_nom, " +
                "CONCAT(t.prenom_t, ' ', t.nom_t) AS teacher_nom " +
                "FROM seance se " +
                "LEFT JOIN classe c ON se.classe_id = c.id " +
                "LEFT JOIN matiere m ON se.matiere_id = m.id_m " +
                "LEFT JOIN teacher t ON se.teacher_id = t.id_t " +
                "WHERE se.jour_se = ? " +
                "  AND se.id_se <> ? " +
                "  AND se.heure_debut_se < ? " +
                "  AND se.heure_fin_se > ? " +
                "  AND (se.teacher_id = ? OR se.classe_id = ? OR se.salle_se = ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(requete);
            pst.setString(1, seance.getJour());
            pst.setInt(2, excludeId);
            pst.setTime(3, seance.getHeureFin());
            pst.setTime(4, seance.getHeureDebut());
            pst.setInt(5, seance.getTeacherId());
            pst.setInt(6, seance.getClasseId());
            pst.setString(7, seance.getSalle());
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String detail = "  • " + rs.getString("jour_se")
                        + " " + rs.getTime("heure_debut_se").toString().substring(0, 5)
                        + "-" + rs.getTime("heure_fin_se").toString().substring(0, 5);
                if (rs.getInt("teacher_id") == seance.getTeacherId()) {
                    detail += " | Prof " + rs.getString("teacher_nom") + " déjà occupé";
                } else if (rs.getInt("classe_id") == seance.getClasseId()) {
                    detail += " | Classe " + rs.getString("classe_nom") + " déjà occupée";
                } else if (rs.getString("salle_se").equals(seance.getSalle())) {
                    detail += " | Salle " + rs.getString("salle_se") + " déjà prise";
                }
                sb.append(detail).append("\n");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return sb.toString();
    }
}
