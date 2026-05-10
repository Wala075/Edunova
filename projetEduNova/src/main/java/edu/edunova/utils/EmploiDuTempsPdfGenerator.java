package edu.edunova.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import edu.edunova.entities.Classe;
import edu.edunova.entities.Seance;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Génère un PDF de l'emploi du temps sous forme de grille calendrier
 * identique à l'affichage à l'écran :
 *   - Lignes  = jours (Lundi → Samedi)
 *   - Colonnes = créneaux horaires (08:30-10:00 | 10:15-11:45 | PAUSE | 13:30-15:00 | 15:15-16:45)
 */
public class EmploiDuTempsPdfGenerator {

    // ── Créneaux identiques au controller ──────────────────────────────
    private static final String[][] SLOTS = {
            {"08:30", "10:00"},
            {"10:15", "11:45"},
            {"13:30", "15:00"},
            {"15:15", "16:45"}
    };

    private static final String[] JOURS = {
            "LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI"
    };

    // ── Palette couleurs ───────────────────────────────────────────────
    private static final BaseColor COL_HEADER_BG   = new BaseColor(44,  62, 110);   // bleu-violet foncé
    private static final BaseColor COL_DAY_BG      = new BaseColor(29,  42,  74);   // bleu nuit
    private static final BaseColor COL_PAUSE_BG    = new BaseColor(22,  33,  62);   // encore plus sombre
    private static final BaseColor COL_CELL_FILLED = new BaseColor(45,  27, 105);   // violet
    private static final BaseColor COL_CELL_EMPTY  = new BaseColor(22,  33,  62);   // bleu nuit clair
    private static final BaseColor COL_CELL_ALT    = new BaseColor(26,  34,  56);   // alternance légère
    private static final BaseColor COL_WHITE       = BaseColor.WHITE;
    private static final BaseColor COL_LIGHT_GRAY  = new BaseColor(180, 180, 210);
    private static final BaseColor COL_ACCENT      = new BaseColor(167, 139, 250);  // violet clair
    private static final BaseColor COL_TITLE_BG    = new BaseColor(15,  18,  36);   // fond titre

    // ── Polices ────────────────────────────────────────────────────────
    private static Font fTitle()    { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,   16, COL_WHITE); }
    private static Font fSub()      { return FontFactory.getFont(FontFactory.HELVETICA,        9,  COL_LIGHT_GRAY); }
    private static Font fSlotHdr()  { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,   8,  COL_ACCENT); }
    private static Font fDayName()  { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9,  COL_WHITE); }
    private static Font fDayDate()  { return FontFactory.getFont(FontFactory.HELVETICA,        7,  COL_LIGHT_GRAY); }
    private static Font fPause()    { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,   7,  COL_LIGHT_GRAY); }
    private static Font fMatiere()  { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,   8,  COL_WHITE); }
    private static Font fMeta()     { return FontFactory.getFont(FontFactory.HELVETICA,        7,  COL_LIGHT_GRAY); }
    private static Font fEmpty()    { return FontFactory.getFont(FontFactory.HELVETICA,        8,  new BaseColor(60, 60, 90)); }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Point d'entrée principal ───────────────────────────────────────
    public static void genererPourClasse(Classe classe, List<Seance> seances, File fichier) throws Exception {
        genererPourClasse(classe, seances, fichier, null);
    }

    public static void genererPourClasse(Classe classe, List<Seance> seances, File fichier, LocalDate weekStart) throws Exception {
        Document doc = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(fichier));
        doc.open();

        // ── Fond de page sombre ──────────────────────────────────────
        PdfContentByte canvas = writer.getDirectContentUnder();
        Rectangle page = doc.getPageSize();
        canvas.setColorFill(COL_TITLE_BG);
        canvas.rectangle(0, 0, page.getWidth(), page.getHeight());
        canvas.fill();

        // ── Titre ────────────────────────────────────────────────────
        String titreTxt = (classe != null)
                ? "Emploi du temps  —  " + classe.getNom() + "  (" + classe.getNiveau() + ")"
                : "Emploi du temps";

        Paragraph title = new Paragraph(titreTxt, fTitle());
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(2);
        doc.add(title);

        Paragraph sub = new Paragraph(
                "Année scolaire 2024-2025   •   Généré le " + java.time.LocalDate.now()
                + "   •   " + seances.size() + " séance(s)",
                fSub());
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(14);
        doc.add(sub);

        // ── Grille ───────────────────────────────────────────────────
        // Colonnes : [jour] [slot0] [slot1] [PAUSE] [slot2] [slot3]
        PdfPTable grid = new PdfPTable(6);
        grid.setWidthPercentage(100);
        grid.setWidths(new float[]{1.4f, 2.2f, 2.2f, 0.35f, 2.2f, 2.2f});
        grid.setSpacingBefore(0);

        // ── Ligne d'en-tête ──────────────────────────────────────────
        grid.addCell(headerCornerCell());
        grid.addCell(slotHeaderCell(SLOTS[0][0], SLOTS[0][1]));
        grid.addCell(slotHeaderCell(SLOTS[1][0], SLOTS[1][1]));
        grid.addCell(pauseHeaderCell());
        grid.addCell(slotHeaderCell(SLOTS[2][0], SLOTS[2][1]));
        grid.addCell(slotHeaderCell(SLOTS[3][0], SLOTS[3][1]));

        // ── Lignes des jours ─────────────────────────────────────────
        for (int d = 0; d < JOURS.length; d++) {
            String jour = JOURS[d];
            BaseColor rowBg = (d % 2 == 0) ? COL_CELL_EMPTY : COL_CELL_ALT;

            // Cellule jour avec date si weekStart fourni
            LocalDate date = (weekStart != null) ? weekStart.plusDays(d) : null;
            grid.addCell(dayCell(jour, date));

            // 4 créneaux (avec colonne PAUSE entre slot 1 et slot 2)
            for (int slot = 0; slot < SLOTS.length; slot++) {
                if (slot == 2) {
                    // Insérer la colonne PAUSE avant le 3e créneau
                    grid.addCell(pauseBodyCell());
                }
                Seance s = trouverSeance(seances, jour, SLOTS[slot][0]);
                grid.addCell(s != null ? seanceCell(s) : emptyCell(rowBg));
            }
        }

        doc.add(grid);
        doc.close();
    }

    // ── Cellules d'en-tête ─────────────────────────────────────────────

    private static PdfPCell headerCornerCell() {
        PdfPCell c = new PdfPCell(new Phrase(""));
        c.setBackgroundColor(COL_TITLE_BG);
        c.setBorderColor(new BaseColor(40, 40, 70));
        c.setPadding(6);
        c.setMinimumHeight(36);
        return c;
    }

    private static PdfPCell slotHeaderCell(String h1, String h2) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(h1 + " — " + h2, fSlotHdr()));
        PdfPCell c = new PdfPCell(p);
        c.setBackgroundColor(COL_HEADER_BG);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setBorderColor(new BaseColor(40, 40, 70));
        c.setPadding(7);
        c.setMinimumHeight(36);
        return c;
    }

    private static PdfPCell pauseHeaderCell() {
        PdfPCell c = new PdfPCell(new Phrase(""));
        c.setBackgroundColor(COL_PAUSE_BG);
        c.setBorderColor(new BaseColor(40, 40, 70));
        c.setPadding(4);
        c.setMinimumHeight(36);
        return c;
    }

    // ── Cellule jour (colonne gauche) ──────────────────────────────────

    private static PdfPCell dayCell(String jour, LocalDate date) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(capitalize(jour) + "\n", fDayName()));
        if (date != null) {
            p.add(new Chunk(date.format(DATE_FMT), fDayDate()));
        }
        PdfPCell c = new PdfPCell(p);
        c.setBackgroundColor(COL_DAY_BG);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setBorderColor(new BaseColor(40, 40, 70));
        c.setPadding(8);
        c.setMinimumHeight(70);
        return c;
    }

    // ── Cellule PAUSE (corps) ──────────────────────────────────────────

    private static PdfPCell pauseBodyCell() {
        Paragraph p = new Paragraph("P\nA\nU\nS\nE", fPause());
        p.setAlignment(Element.ALIGN_CENTER);
        PdfPCell c = new PdfPCell(p);
        c.setBackgroundColor(COL_PAUSE_BG);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setBorderColor(new BaseColor(40, 40, 70));
        c.setPadding(4);
        c.setMinimumHeight(70);
        return c;
    }

    // ── Cellule séance remplie ─────────────────────────────────────────

    private static PdfPCell seanceCell(Seance s) {
        Paragraph p = new Paragraph();

        // Matière
        String matiere = s.getMatiereNom() != null ? s.getMatiereNom() : "—";
        p.add(new Chunk(matiere + "\n", fMatiere()));

        // Salle
        if (s.getSalle() != null && !s.getSalle().isEmpty()) {
            p.add(new Chunk("Salle : " + s.getSalle() + "\n", fMeta()));
        }

        // Enseignant
        if (s.getTeacherNom() != null && !s.getTeacherNom().trim().isEmpty()) {
            p.add(new Chunk(s.getTeacherNom() + "\n", fMeta()));
        }

        // Type de cours
        if (s.getTypeCours() != null && !s.getTypeCours().isEmpty()) {
            p.add(new Chunk(s.getTypeCours(), fMeta()));
        }

        PdfPCell c = new PdfPCell(p);
        c.setBackgroundColor(COL_CELL_FILLED);
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setBorderColor(new BaseColor(80, 50, 160));
        c.setBorderWidth(1.2f);
        c.setPadding(7);
        c.setMinimumHeight(70);
        return c;
    }

    // ── Cellule vide ──────────────────────────────────────────────────

    private static PdfPCell emptyCell(BaseColor bg) {
        PdfPCell c = new PdfPCell(new Phrase("", fEmpty()));
        c.setBackgroundColor(bg);
        c.setBorderColor(new BaseColor(35, 42, 74));
        c.setPadding(6);
        c.setMinimumHeight(70);
        return c;
    }

    // ── Utilitaires ───────────────────────────────────────────────────

    /**
     * Trouve la séance correspondant à un jour et un créneau de début.
     */
    private static Seance trouverSeance(List<Seance> seances, String jour, String heureDebut) {
        for (Seance s : seances) {
            if (s.getJour() == null || s.getHeureDebut() == null) continue;
            if (!s.getJour().equalsIgnoreCase(jour)) continue;
            String hm = s.getHeureDebut().toString().substring(0, 5);
            if (hm.equals(heureDebut)) return s;
        }
        return null;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
