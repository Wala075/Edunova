package interfaces;

import entities.ForumPost;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface metier du service Forum.
 * Definit le contrat que toute implementation doit respecter.
 */
public interface IForumService {
    int    soumettre(ForumPost p)                               throws SQLException;
    int    publierAdmin(ForumPost p)                           throws SQLException;
    void   modifierPost(int id, String titre, String contenu, int userId) throws SQLException;
    void   supprimer(int id)                                   throws SQLException;
    void   acceptPost(int id)                                  throws SQLException;
    void   refusePost(int id)                                  throws SQLException;
    void   approuver(int id)                                   throws SQLException;
    void   rejeter(int id)                                     throws SQLException;

    List<ForumPost> getTous()                                  throws SQLException;
    List<ForumPost> getApprouves()                             throws SQLException;
    List<ForumPost> getParStatut(ForumPost.Statut statut)      throws SQLException;
    List<ForumPost> getMesPostsPar(int userId)                 throws SQLException;
    List<ForumPost> rechercher(String keyword)                 throws SQLException;
    List<ForumPost> rechercherApprouves(String keyword)        throws SQLException;

    ForumPost getById(int id);

    int compterTous()                                          throws SQLException;
    int compterParStatut(ForumPost.Statut statut)              throws SQLException;
}
