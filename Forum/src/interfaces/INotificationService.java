package interfaces;

import entities.Notification;
import java.sql.SQLException;
import java.util.List;

public interface INotificationService {
    void                creer(int userId, String message, String type);
    List<Notification>  getByUser(int userId)         throws SQLException;
    int                 compterNonLues(int userId)    throws SQLException;
    void                marquerToutesLues(int userId) throws SQLException;
}
