package main.java.service;

import main.java.dto.vote.VoteResponseDto;
import main.java.entity.Comment;
import main.java.entity.User;
import main.java.exceptions.InvalidVoteTypeException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CommentVotingService {
    private final VotingService votingService;
    private final UserManagementService userManagementService;

    public CommentVotingService(VotingService votingService, UserManagementService userManagementService) {
        this.votingService = votingService;
        this.userManagementService = userManagementService;
    }

    public VoteResponseDto voteOnComment(UUID commentId, String voteType, String username, Comment comment) {
        User user = userManagementService.findByUsername(username);

        switch (voteType.toLowerCase()) {
            case "up" -> votingService.createVote(user.getId(), null, comment.getId(), true);
            case "down" -> votingService.createVote(user.getId(), null, comment.getId(), false);
            case "none" -> votingService.deleteVoteForComment(comment, user);
            default -> throw new InvalidVoteTypeException("Tipul de vot este invalid: " + voteType);
        }

        int upvotes = votingService.countUpvotesForComment(comment.getId());
        int downvotes = votingService.countDownvotesForComment(comment.getId());
        int score = upvotes - downvotes;
        String userVote = votingService.getVoteTypeForUser(user.getId(), null, commentId);

        return new VoteResponseDto(upvotes, downvotes, score, userVote);
    }
}
