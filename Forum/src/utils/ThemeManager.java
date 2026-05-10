package utils;

import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * ThemeManager — mode sombre / clair.
 *
 * FIX: l'ancienne implémentation stockait le style original dans
 * node.setUserData(), ce qui écrasait toute donnée déjà présente sur le
 * nœud. On utilise désormais une IdentityHashMap dédiée.
 *
 * FIX: les cartes de posts sont recréées à chaque chargerFil(). Appeler
 * ThemeManager.appliquer(scene) après chaque rechargement suffit car la
 * scène est parcourue depuis la racine.
 */
public class ThemeManager {

    private static final Preferences PREFS    = Preferences.userRoot().node("edunova/forum");
    private static final String      KEY      = "darkMode";
    private static boolean           darkMode = PREFS.getBoolean(KEY, false);

    /** Stores original inline styles keyed by node identity. */
    private static final Map<Node, String> originalStyles = new IdentityHashMap<>();

    public static void appliquer(Scene scene) {
        if (scene == null || scene.getRoot() == null) return;
        // Clear stale entries for nodes no longer in the scene
        originalStyles.entrySet().removeIf(e -> e.getKey().getScene() == null);
        parcourir(scene.getRoot());
    }

    public static boolean toggle(Scene scene) {
        darkMode = !darkMode;
        PREFS.putBoolean(KEY, darkMode);
        if (!darkMode) {
            // Restore all saved styles before re-traversing
            originalStyles.forEach(Node::setStyle);
            originalStyles.clear();
        }
        appliquer(scene);
        return darkMode;
    }

    public static boolean isDark() { return darkMode; }

    private static void parcourir(Node node) {
        if (node == null) return;

        String style = node.getStyle() == null ? "" : node.getStyle();

        if (darkMode) {
            boolean hasLightBg =
                style.contains("-fx-background-color:white") ||
                style.contains("-fx-background-color:#ffffff") ||
                style.contains("-fx-background-color:#f8f8ff") ||
                style.contains("-fx-background-color:#f9fafb") ||
                style.contains("-fx-background-color:#f3f4f6") ||
                style.contains("-fx-background-color:#f5f3ff") ||
                style.contains("-fx-background-color:#f0f9ff") ||
                style.contains("-fx-background-color:#fafafa") ||
                style.contains("-fx-background-color:#f0fdf4") ||
                style.contains("-fx-background-color:#fef2f2") ||
                style.contains("-fx-background-color:#fffbeb") ||
                style.contains("-fx-background-color:#e0e7ff") ||
                style.contains("-fx-background-color:#fce7f3") ||
                style.contains("-fx-background-color:#EAF3DE") ||
                style.contains("-fx-background-color:#FAEEDA") ||
                style.contains("-fx-background-color:#f0f2f5");

            if (hasLightBg && !originalStyles.containsKey(node)) {
                // Save original style using the dedicated map (not userData)
                originalStyles.put(node, style);
                style = style
                    .replace("-fx-background-color:white",    "-fx-background-color:#1e293b")
                    .replace("-fx-background-color:#ffffff",  "-fx-background-color:#1e293b")
                    .replace("-fx-background-color:#f8f8ff",  "-fx-background-color:#0f172a")
                    .replace("-fx-background-color:#f9fafb",  "-fx-background-color:#1e293b")
                    .replace("-fx-background-color:#f3f4f6",  "-fx-background-color:#334155")
                    .replace("-fx-background-color:#f5f3ff",  "-fx-background-color:#1e1b4b")
                    .replace("-fx-background-color:#f0f9ff",  "-fx-background-color:#0c1a2e")
                    .replace("-fx-background-color:#fafafa",  "-fx-background-color:#1e293b")
                    .replace("-fx-background-color:#f0fdf4",  "-fx-background-color:#052e16")
                    .replace("-fx-background-color:#fef2f2",  "-fx-background-color:#2d0a0a")
                    .replace("-fx-background-color:#fffbeb",  "-fx-background-color:#1c1300")
                    .replace("-fx-background-color:#e0e7ff",  "-fx-background-color:#1e1b4b")
                    .replace("-fx-background-color:#fce7f3",  "-fx-background-color:#2d0a1e")
                    .replace("-fx-background-color:#EAF3DE",  "-fx-background-color:#052e16")
                    .replace("-fx-background-color:#FAEEDA",  "-fx-background-color:#1c1300")
                    .replace("-fx-background-color:#f0f2f5",  "-fx-background-color:#0f172a");
                style = style
                    .replace("-fx-border-color:#e5e7eb", "-fx-border-color:#334155")
                    .replace("-fx-border-color:#e0e7ff", "-fx-border-color:#334155")
                    .replace("-fx-border-color:#bae6fd", "-fx-border-color:#1e3a5f");
                node.setStyle(style);
            }

            // Labels: darken text that isn't already a coloured/white fill
            if (node instanceof Label lbl) {
                String s = lbl.getStyle();
                if (!s.contains("-fx-text-fill:white") &&
                    !s.contains("-fx-text-fill:#7c3aed") &&
                    !s.contains("-fx-text-fill:#ef4444") &&
                    !s.contains("-fx-text-fill:#0369a1") &&
                    !s.contains("-fx-text-fill:#0c4a6e") &&
                    !s.contains("-fx-text-fill:#9ca3af") &&
                    !s.contains("-fx-text-fill:#16a34a") &&
                    !s.contains("-fx-text-fill:#166534") &&
                    !s.contains("-fx-text-fill:#059669") &&
                    !s.contains("dark_text")) {
                    lbl.setStyle(s + ";-fx-text-fill:#e2e8f0;dark_text:1;");
                }
            }

            if (node instanceof TextField tf) {
                String s = tf.getStyle();
                if (!s.contains("dark_input")) {
                    tf.setStyle(s + ";-fx-background-color:#1e293b;-fx-text-fill:#e2e8f0;" +
                                "-fx-border-color:#475569;dark_input:1;");
                }
            }
            if (node instanceof TextArea ta) {
                String s = ta.getStyle();
                if (!s.contains("dark_input")) {
                    ta.setStyle(s + ";-fx-background-color:#1e293b;-fx-text-fill:#e2e8f0;" +
                                "-fx-border-color:#475569;dark_input:1;");
                }
            }

        } else {
            // Light mode: restore from our dedicated map
            String saved = originalStyles.remove(node);
            if (saved != null) {
                node.setStyle(saved);
            }
            if (node instanceof Label lbl) {
                lbl.setStyle(lbl.getStyle()
                    .replace(";-fx-text-fill:#e2e8f0;dark_text:1;", ""));
            }
            if (node instanceof TextField tf) {
                tf.setStyle(tf.getStyle()
                    .replace(";-fx-background-color:#1e293b;-fx-text-fill:#e2e8f0;" +
                             "-fx-border-color:#475569;dark_input:1;", ""));
            }
            if (node instanceof TextArea ta) {
                ta.setStyle(ta.getStyle()
                    .replace(";-fx-background-color:#1e293b;-fx-text-fill:#e2e8f0;" +
                             "-fx-border-color:#475569;dark_input:1;", ""));
            }
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                parcourir(child);
            }
        }
    }
}
