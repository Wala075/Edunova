package entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {
    private int           id;
    private int           userId;
    private String        message;
    private String        type;   // ACCEPTE, REFUSE, COMMENTAIRE
    private boolean       lue;
    private LocalDateTime dateCreation;

    public Notification() {}

    public int           getId()                          { return id; }
    public void          setId(int v)                     { this.id = v; }
    public int           getUserId()                      { return userId; }
    public void          setUserId(int v)                 { this.userId = v; }
    public String        getMessage()                     { return message; }
    public void          setMessage(String v)             { this.message = v; }
    public String        getType()                        { return type; }
    public void          setType(String v)                { this.type = v; }
    public boolean       isLue()                          { return lue; }
    public void          setLue(boolean v)                { this.lue = v; }
    public LocalDateTime getDateCreation()                { return dateCreation; }
    public void          setDateCreation(LocalDateTime v) { this.dateCreation = v; }

    public String getIcone() {
        if (type == null) return "🔔";
        return switch (type) {
            case "ACCEPTE"     -> "✅";
            case "REFUSE"      -> "❌";
            case "COMMENTAIRE" -> "💬";
            case "LIKE"         -> "❤";
            default            -> "🔔";
        };
    }

    public String getDateFormatee() {
        if (dateCreation == null) return "";
        return dateCreation.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }
}
