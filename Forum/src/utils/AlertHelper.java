package utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class AlertHelper {
    public static void info(String t, String m)   { show(Alert.AlertType.INFORMATION,t,m); }
    public static void erreur(String t, String m) { show(Alert.AlertType.ERROR,t,m); }
    public static boolean confirmer(String t, String m) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m);
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get()==ButtonType.OK;
    }
    private static void show(Alert.AlertType type, String t, String m) {
        Alert a = new Alert(type);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}
