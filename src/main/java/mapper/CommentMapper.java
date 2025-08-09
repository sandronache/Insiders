package main.java.mapper;

import main.java.dto.comment.CommentResponseDto;
import main.java.entity.Comment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CommentMapper {

    public CommentResponseDto toDto(
            Comment comment,
            int upVotes,
            int downVotes,
            String userVote,
            List<CommentResponseDto> replies
    ) {
        UUID parentId = comment.getParentComment() != null ? comment.getParentComment().getId() : null;
        String author = (comment.getUser() != null) ? comment.getUser().getUsername() : "[deleted]";
        String content = comment.getContent();
        int score = upVotes - downVotes;

        return new CommentResponseDto(
                comment.getId(),
                comment.getPost().getId(),
                parentId,
                content,
                author,
                upVotes,
                downVotes,
                score,
                userVote,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                replies
        );
    }
}
