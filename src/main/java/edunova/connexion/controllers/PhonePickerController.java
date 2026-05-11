package edunova.connexion.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class PhonePickerController {

    // ── MAP des drapeaux par code de pays ────────────────────────
    private static final Map<String, String> DRAPEAUX = new HashMap<>();
    
    static {
        // Afrique du Nord & Moyen-Orient
        DRAPEAUX.put("Tunisie", "🇹🇳");
        DRAPEAUX.put("Maroc", "🇲🇦");
        DRAPEAUX.put("Algérie", "🇩🇿");
        DRAPEAUX.put("Libye", "🇱🇾");
        DRAPEAUX.put("Égypte", "🇪🇬");
        DRAPEAUX.put("Arabie Saoudite", "🇸🇦");
        DRAPEAUX.put("Émirats Arabes", "🇦🇪");
        DRAPEAUX.put("Qatar", "🇶🇦");
        DRAPEAUX.put("Koweït", "🇰🇼");
        DRAPEAUX.put("Jordanie", "🇯🇴");
        DRAPEAUX.put("Liban", "🇱🇧");
        DRAPEAUX.put("Syrie", "🇸🇾");
        DRAPEAUX.put("Irak", "🇮🇶");
        DRAPEAUX.put("Yémen", "🇾🇪");
        DRAPEAUX.put("Oman", "🇴🇲");
        DRAPEAUX.put("Bahreïn", "🇧🇭");
        DRAPEAUX.put("Soudan", "🇸🇩");
        DRAPEAUX.put("Mauritanie", "🇲🇷");
        // Europe
        DRAPEAUX.put("France", "🇫🇷");
        DRAPEAUX.put("Allemagne", "🇩🇪");
        DRAPEAUX.put("Royaume-Uni", "🇬🇧");
        DRAPEAUX.put("Italie", "🇮🇹");
        DRAPEAUX.put("Espagne", "🇪🇸");
        DRAPEAUX.put("Belgique", "🇧🇪");
        DRAPEAUX.put("Suisse", "🇨🇭");
        DRAPEAUX.put("Pays-Bas", "🇳🇱");
        DRAPEAUX.put("Portugal", "🇵🇹");
        DRAPEAUX.put("Suède", "🇸🇪");
        DRAPEAUX.put("Norvège", "🇳🇴");
        DRAPEAUX.put("Danemark", "🇩🇰");
        DRAPEAUX.put("Pologne", "🇵🇱");
        DRAPEAUX.put("Roumanie", "🇷🇴");
        DRAPEAUX.put("Grèce", "🇬🇷");
        DRAPEAUX.put("Turquie", "🇹🇷");
        DRAPEAUX.put("Russie", "🇷🇺");
        // Amériques
        DRAPEAUX.put("États-Unis", "🇺🇸");
        DRAPEAUX.put("Canada", "🇨🇦");
        DRAPEAUX.put("Brésil", "🇧🇷");
        DRAPEAUX.put("Mexique", "🇲🇽");
        DRAPEAUX.put("Argentine", "🇦🇷");
        DRAPEAUX.put("Chili", "🇨🇱");
        DRAPEAUX.put("Colombie", "🇨🇴");
        // Asie
        DRAPEAUX.put("Chine", "🇨🇳");
        DRAPEAUX.put("Japon", "🇯🇵");
        DRAPEAUX.put("Corée du Sud", "🇰🇷");
        DRAPEAUX.put("Inde", "🇮🇳");
        DRAPEAUX.put("Pakistan", "🇵🇰");
        DRAPEAUX.put("Indonésie", "🇮🇩");
        DRAPEAUX.put("Malaisie", "🇲🇾");
        DRAPEAUX.put("Philippines", "🇵🇭");
        DRAPEAUX.put("Vietnam", "🇻🇳");
        DRAPEAUX.put("Thaïlande", "🇹🇭");
        DRAPEAUX.put("Iran", "🇮🇷");
        // Afrique
        DRAPEAUX.put("Sénégal", "🇸🇳");
        DRAPEAUX.put("Côte d'Ivoire", "🇨🇮");
        DRAPEAUX.put("Cameroun", "🇨🇲");
        DRAPEAUX.put("Ghana", "🇬🇭");
        DRAPEAUX.put("Nigéria", "🇳🇬");
        DRAPEAUX.put("Kenya", "🇰🇪");
        DRAPEAUX.put("Afrique du Sud", "🇿🇦");
        DRAPEAUX.put("Éthiopie", "🇪🇹");
        // Océanie
        DRAPEAUX.put("Australie", "🇦🇺");
        DRAPEAUX.put("Nouvelle-Zélande", "🇳🇿");
    }

    // ── PAYS publique pour accès depuis autres controllers ────────
    public static final LinkedHashMap<String, String>
            PAYS = new LinkedHashMap<>();

    static {
        // Créer une liste temporaire avec tous les pays
        List<Map.Entry<String, String>> paysTemp = new ArrayList<>();
        
        // Afrique du Nord & Moyen-Orient
        paysTemp.add(new AbstractMap.SimpleEntry<>("Tunisie",          "+216"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Maroc",            "+212"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Algérie",          "+213"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Libye",            "+218"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Égypte",           "+20"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Arabie Saoudite",  "+966"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Émirats Arabes",   "+971"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Qatar",            "+974"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Koweït",           "+965"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Jordanie",         "+962"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Liban",            "+961"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Syrie",            "+963"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Irak",             "+964"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Yémen",            "+967"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Oman",             "+968"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Bahreïn",          "+973"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Soudan",           "+249"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Mauritanie",       "+222"));
        // Europe
        paysTemp.add(new AbstractMap.SimpleEntry<>("France",           "+33"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Allemagne",        "+49"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Royaume-Uni",      "+44"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Italie",           "+39"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Espagne",          "+34"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Belgique",         "+32"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Suisse",           "+41"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Pays-Bas",         "+31"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Portugal",         "+351"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Suède",            "+46"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Norvège",          "+47"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Danemark",         "+45"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Pologne",          "+48"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Roumanie",         "+40"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Grèce",            "+30"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Turquie",          "+90"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Russie",           "+7"));
        // Amériques
        paysTemp.add(new AbstractMap.SimpleEntry<>("États-Unis",       "+1"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Canada",           "+1"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Brésil",           "+55"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Mexique",          "+52"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Argentine",        "+54"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Chili",            "+56"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Colombie",         "+57"));
        // Asie
        paysTemp.add(new AbstractMap.SimpleEntry<>("Chine",            "+86"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Japon",            "+81"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Corée du Sud",     "+82"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Inde",             "+91"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Pakistan",         "+92"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Indonésie",        "+62"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Malaisie",         "+60"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Philippines",      "+63"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Vietnam",          "+84"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Thaïlande",        "+66"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Iran",             "+98"));
        // Afrique
        paysTemp.add(new AbstractMap.SimpleEntry<>("Sénégal",          "+221"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Côte d'Ivoire",    "+225"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Cameroun",         "+237"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Ghana",            "+233"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Nigéria",          "+234"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Kenya",            "+254"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Afrique du Sud",   "+27"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Éthiopie",         "+251"));
        // Océanie
        paysTemp.add(new AbstractMap.SimpleEntry<>("Australie",        "+61"));
        paysTemp.add(new AbstractMap.SimpleEntry<>("Nouvelle-Zélande", "+64"));
        
        // Trier par ordre alphabétique
        paysTemp.sort((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()));
        
        // Ajouter les pays triés au LinkedHashMap
        for (Map.Entry<String, String> entry : paysTemp) {
            PAYS.put(entry.getKey(), entry.getValue());
        }
    }

    // ── Extraire emoji, nom, code depuis une entrée ───────────────
    public static String getEmoji(String paysNom) {
        // Chercher le drapeau dans la map
        String drapeau = DRAPEAUX.get(paysNom);
        return drapeau != null ? drapeau : "🌍";
    }

    public static String getNomSansEmoji(String paysNom) {
        return paysNom;
    }

    // ── Créer un item dropdown professionnel ──────────────────────
    public static HBox creerItem(String pays, String code,
                                 boolean dark,
                                 Runnable onSelect) {

        String bgNormal  = dark ? "transparent" : "transparent";
        String bgHover   = dark ? "#2d1b69"     : "#f5f3ff";
        String textColor = dark ? "#e2e8f0"     : "#1e293b";
        String nomColor  = dark ? "#94a3b8"     : "#64748b";
        String codeColor = dark ? "#a78bfa"     : "#7c3aed";
        String codeBg    = dark ? "#2d1b69"     : "#ede9fe";

        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle(
                "-fx-padding: 9 12;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-color: " + bgNormal + ";");

        // ── Drapeau ───────────────────────────────────────────────
        Label lblEmoji = new Label(getEmoji(pays));
        lblEmoji.setStyle(
                "-fx-font-size: 24;" +
                "-fx-text-alignment: center;" +
                "-fx-padding: 0 5;");
        lblEmoji.setMinWidth(40);
        lblEmoji.setAlignment(Pos.CENTER);

        // ── Nom du pays ───────────────────────────────────────────
        Label lblNom = new Label(getNomSansEmoji(pays));
        lblNom.setStyle(
                "-fx-font-size: 12;" +
                        "-fx-text-fill: " + textColor + ";");
        HBox.setHgrow(lblNom, Priority.ALWAYS);

        // ── Badge code pays ───────────────────────────────────────
        Label lblCode = new Label(code);
        lblCode.setStyle(
                "-fx-font-size: 11;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + codeColor + ";" +
                        "-fx-background-color: " + codeBg + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 2 7;");

        item.getChildren().addAll(lblEmoji, lblNom, lblCode);

        // ── Hover ─────────────────────────────────────────────────
        item.setOnMouseEntered(e -> item.setStyle(
                "-fx-padding: 9 12;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-color: " + bgHover + ";"));
        item.setOnMouseExited(e -> item.setStyle(
                "-fx-padding: 9 12;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-color: " + bgNormal + ";"));

        item.setOnMouseClicked(e -> onSelect.run());

        return item;
    }
}