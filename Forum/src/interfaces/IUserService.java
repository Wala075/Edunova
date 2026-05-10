package interfaces;

import entities.User;
import java.sql.SQLException;

public interface IUserService {
    User login(String email, String password) throws SQLException;
}
