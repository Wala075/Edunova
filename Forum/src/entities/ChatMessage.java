package entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private int           id;
    private int           senderId;
    private int           receiverId;
    private String        contenu;
    private String        statut;
    private boolean       lu;
    private LocalDateTime dateCreation;

    public ChatMessage() {}

    public int           getId()                          { return id; }
    public void          setId(int v)                     { this.id = v; }
    public int           getSenderId()                    { return senderId; }
    public void          setSenderId(int v)               { this.senderId = v; }
    public int           getReceiverId()                  { return receiverId; }
    public void          setReceiverId(int v)             { this.receiverId = v; }
    public String        getContenu()                     { return contenu; }
    public void          setContenu(String v)             { this.contenu = v; }
    public String        getStatut()                      { return statut; }
    public void          setStatut(String v)              { this.statut = v; }
    public boolean       isLu()                           { return lu; }
    public void          setLu(boolean v)                 { this.lu = v; }
    public LocalDateTime getDateCreation()                { return dateCreation; }
    public void          setDateCreation(LocalDateTime v) { this.dateCreation = v; }

    public String getHeureFormatee() {
        if (dateCreation == null) return "";
        return dateCreation.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
