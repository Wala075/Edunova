package controllers;

import edu.edunova_a.entities.LiveSession;
import edu.edunova_a.entities.Presence;
import edu.edunova_a.entities.Seance;
import edu.edunova_a.services.*;
import edu.edunova_a.utils.MyConnection;
import edu.edunova_a.utils.StatusUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    /* ----- stat cards ----- */
    @FXML private Label lblTotalSeances;
    @FXML private Label lblTotalLive;
    @FXML private Label lblTotalPresences;
    @FXML private Label lblTotalAbsences;
    @FXML private Label lblTotalCours;
    @FXML private Label lblTauxPresence;

    /* ----- pie chart ----- */
    @FXML private PieChart piePresence;

    /* ----- listes ----- */
    @FXML private ListView<String> listUpcoming;
    @FXML private ListView<String> listRecentActivity;

    /* ----- labels info ----- */
    @FXML private Label lblPresentsToday;
    @FXML private Label lblAbsentsToday;
    @FXML private Label lblRetardsToday;

    /* ----- services ----- */
    private final CoursService       coursService   = new CoursService();
    private final SeanceService      seanceService  = new SeanceService();
    private final LiveSessionService liveService    = new LiveSessionService();
    private final PresenceService    presenceService = new PresenceService();

    @FXML
    public void initialize() {
        loadStatCards();
        loadPieChart();
        loadUpcoming();
        loadRecentActivity();
        loadTodayStats();
    }

    /* ===================== STAT CARDS ===================== */

    private void loadStatCards() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try (Statement st = cnx.createStatement()) {
            int seances    = count(st, "SELECT COUNT(*) FROM seance");
            int lives      = count(st, "SELECT COUNT(*) FROM live_session");
            int presences  = count(st, "SELECT COUNT(*) FROM presence");
            int absences   = count(st, "SELECT COUNT(*) FROM presence WHERE statut_pr = 'ABSENT'");
            int cours      = count(st, "SELECT COUNT(*) FROM cours");

            lblTotalSeances.setText(String.valueOf(seances));
            lblTotalLive.setText(String.valueOf(lives));
            lblTotalPresences.setText(String.valueOf(presences));
            lblTotalAbsences.setText(String.valueOf(absences));

            if (lblTotalCours != null)
                lblTotalCours.setText(String.valueOf(cours));

            // Taux de présence global
            if (lblTauxPresence != null && presences > 0) {
                int presents = count(st, "SELECT COUNT(*) FROM presence WHERE statut_pr = 'PRESENT'");
                int taux = (int) Math.round(100.0 * presents / presences);
                lblTauxPresence.setText(taux + "%");
            }
        } catch (Exception e) {
            System.err.println("[Dashboard] Erreur chargement stats : " + e.getMessage());
        }
    }

    /* ===================== PIE CHART ===================== */

    private void loadPieChart() {
        if (piePresence == null) return;
        Connection cnx = MyConnection.getInstance().getCnx();
        try (Statement st = cnx.createStatement()) {
            int presents  = count(st, "SELECT COUNT(*) FROM presence WHERE statut_pr = 'PRESENT'");
            int absents   = count(st, "SELECT COUNT(*) FROM presence WHERE statut_pr = 'ABSENT'");
            int retards   = count(st, "SELECT COUNT(*) FROM presence WHERE statut_pr = 'RETARD'");
            int justifies = count(st, "SELECT COUNT(*) FROM presence WHERE statut_pr = 'JUSTIFIE'");

            piePresence.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Présents (" + presents + ")", presents),
                    new PieChart.Data("Absents (" + absents + ")", absents),
                    new PieChart.Data("Retards (" + retards + ")", retards),
                    new PieChart.Data("Justifiés (" + justifies + ")", justifies)
            ));
            piePresence.setLabelsVisible(false);
            piePresence.setLegendVisible(true);
            piePresence.setTitle(null);

            // Appliquer les couleurs après le rendu
            javafx.application.Platform.runLater(() -> {
                String[] colors = {"#10b981", "#ef4444", "#f59e0b", "#6366f1"};
                int i = 0;
                for (PieChart.Data d : piePresence.getData()) {
                    if (d.getNode() != null) {
                        d.getNode().setStyle("-fx-pie-color: " + colors[i] + ";");
                    }
                    i++;
                }
            });
        } catch (Exception e) {
            System.err.println("[Dashboard] Erreur pie chart : " + e.getMessage());
        }
    }

    /* ===================== UPCOMING ===================== */

    private void loadUpcoming() {
        if (listUpcoming == null) return;
        try {
            listUpcoming.getItems().clear();

            // Séances à venir
            List<Seance> seances = seanceService.findUpcoming(5);
            for (Seance s : seances) {
                String label = "📅  " + (s.getDate_seance() != null ? s.getDate_seance().toString() : s.getJour_se())
                        + "  •  " + s.getHeure_debut_se() + " - " + s.getHeure_fin_se()
                        + "  •  " + (s.getMatiere_nom() != null ? s.getMatiere_nom() : "—")
                        + "  •  " + (s.getClasse_nom() != null ? s.getClasse_nom() : "—")
                        + "  [" + (s.getType_cours_se() != null ? s.getType_cours_se() : "") + "]";
                listUpcoming.getItems().add(label);
            }

            // Sessions live à venir
            List<LiveSession> lives = liveService.findUpcoming(5);
            for (LiveSession l : lives) {
                String label = "🎥  " + l.getDate_session_ls()
                        + "  •  " + l.getHeure_debut_ls() + " - " + l.getHeure_fin_ls()
                        + "  •  " + l.getTitre_ls()
                        + "  •  " + (l.getClasse_nom() != null ? l.getClasse_nom() : "—")
                        + "  [" + StatusUtils.libelleStatutLive(l.getStatut_ls()) + "]";
                listUpcoming.getItems().add(label);
            }

            if (listUpcoming.getItems().isEmpty()) {
                listUpcoming.getItems().add("Aucune séance ou session à venir.");
            }

            // Styling des cellules
            listUpcoming.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("-fx-background-color: transparent;");
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: #e6e6f0; -fx-font-size: 12; -fx-background-color: transparent; -fx-padding: 8 12;");
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("[Dashboard] Erreur upcoming : " + e.getMessage());
        }
    }

    /* ===================== RECENT ACTIVITY ===================== */

    private void loadRecentActivity() {
        if (listRecentActivity == null) return;
        try {
            listRecentActivity.getItems().clear();

            List<Presence> recent = presenceService.afficher();
            int max = Math.min(8, recent.size());
            for (int i = 0; i < max; i++) {
                Presence p = recent.get(i);
                String emoji = "PRESENT".equalsIgnoreCase(p.getStatut_pr()) ? "✅" :
                               "ABSENT".equalsIgnoreCase(p.getStatut_pr()) ? "❌" :
                               "RETARD".equalsIgnoreCase(p.getStatut_pr()) ? "⚠️" : "📋";
                String label = emoji + "  " + (p.getStudent_nom() != null ? p.getStudent_nom() : "?")
                        + "  —  " + StatusUtils.libelleStatutPresence(p.getStatut_pr())
                        + "  •  " + (p.getDate_presence_pr() != null ? p.getDate_presence_pr().toString() : "")
                        + (p.getSeance_label() != null && !p.getSeance_label().isBlank()
                            ? "  •  " + p.getSeance_label() : "")
                        + (p.getLive_titre() != null ? "  •  🎥 " + p.getLive_titre() : "");
                listRecentActivity.getItems().add(label);
            }

            if (listRecentActivity.getItems().isEmpty()) {
                listRecentActivity.getItems().add("Aucune activité récente.");
            }

            listRecentActivity.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("-fx-background-color: transparent;");
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: #e6e6f0; -fx-font-size: 12; -fx-background-color: transparent; -fx-padding: 8 12;");
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("[Dashboard] Erreur recent : " + e.getMessage());
        }
    }

    /* ===================== TODAY STATS ===================== */

    private void loadTodayStats() {
        Connection cnx = MyConnection.getInstance().getCnx();
        String today = LocalDate.now().toString();
        try (Statement st = cnx.createStatement()) {
            if (lblPresentsToday != null)
                lblPresentsToday.setText(String.valueOf(count(st,
                    "SELECT COUNT(*) FROM presence WHERE statut_pr='PRESENT' AND date_presence_pr='" + today + "'")));
            if (lblAbsentsToday != null)
                lblAbsentsToday.setText(String.valueOf(count(st,
                    "SELECT COUNT(*) FROM presence WHERE statut_pr='ABSENT' AND date_presence_pr='" + today + "'")));
            if (lblRetardsToday != null)
                lblRetardsToday.setText(String.valueOf(count(st,
                    "SELECT COUNT(*) FROM presence WHERE statut_pr='RETARD' AND date_presence_pr='" + today + "'")));
        } catch (Exception e) {
            System.err.println("[Dashboard] Erreur today : " + e.getMessage());
        }
    }

    /* ===================== HELPER ===================== */

    private int count(Statement st, String sql) {
        try (ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("[Dashboard] Erreur count : " + e.getMessage());
        }
        return 0;
    }
}
