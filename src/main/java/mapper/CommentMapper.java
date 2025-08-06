package main.java.mapper;

import main.java.dto.comment.CommentResponseDto;
import main.java.entity.Comment;
import main.java.service.VotingService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CommentMapper {
    private final VotingService votingService;

    public CommentMapper(VotingService votingService) {
        this.votingService = votingService;
    }

    public CommentResponseDto toDto(Comment comment, List<Comment> allComments, String currentUsername) {
        UUID parentId = comment.getParentComment() != null ? comment.getParentComment().getId() : null;
        String author = comment.isDeleted() ? "[deleted]" : comment.getUser().getUsername();

        String content = comment.isDeleted() ? "[comentariu sters]" : comment.getContent();
        int upVotes = votingService.countUpvotesForComment(comment.getId());
        int downVotes = votingService.countDownvotesForComment(comment.getId());
        int score = upVotes - downVotes;

        String userVote = null;
        if (currentUsername != null) {
            UUID userId = votingService.getUserIdByUsername(currentUsername);
            userVote = votingService.getVoteTypeForUser(userId, null, comment.getId());
        }


        List<CommentResponseDto> replies = allComments.stream()
                .filter(c -> comment.getId().equals(c.getParentComment() != null ? c.getParentComment().getId() : null))
                .map(c -> toDto(c, allComments, currentUsername))
                .toList();

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
