package interfaces;

import entities.Comment;
import java.sql.SQLException;
import java.util.List;

public interface ICommentService {
    void          addComment(Comment comment)  throws SQLException;
    List<Comment> getByPost(int postId)        throws SQLException;
    int           countComments(int postId)    throws SQLException;
}
