package interfaces;

import java.sql.SQLException;

public interface ILikeService {
    boolean toggleLike(int postId, int userId) throws SQLException;
    void    like(int postId, int userId)        throws SQLException;
    void    unlike(int postId, int userId)      throws SQLException;
    int     countLikes(int postId)              throws SQLException;
    boolean hasLiked(int postId, int userId)    throws SQLException;
}
