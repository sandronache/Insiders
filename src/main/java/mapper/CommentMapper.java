package main.java.mapper;

import main.java.dto.comment.CommentResponseDto;
import main.java.entity.Comment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentMapper {
    public CommentResponseDto toDto(Comment comment, List<Comment>allComments){
        List<CommentResponseDto> replies = allComments.stream()
                .filter(c -> comment.getId().equals(c.getParentComment()!=null ? c.getParentComment().getId():null))
                .map(c -> toDto(c, allComments))
                .toList();

        return new CommentResponseDto(
                comment.getId(),
                comment.getPost().getId(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                comment.getContent(),
                comment.getUser().getUsername(),
                0,
                0,
                0,
                null,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                replies
        );
    }
}
