package interfaces;

import java.sql.SQLException;

public interface ISignalementService {
    boolean signaler(int postId, int userId, String raison) throws SQLException;
    boolean aDejaSignale(int postId, int userId)             throws SQLException;
    int     compterSignalements(int postId)                  throws SQLException;
    void    supprimerSignalements(int postId)                throws SQLException;
}
