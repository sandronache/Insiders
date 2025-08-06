package main.java.service;

import main.java.entity.Comment;
import main.java.entity.Post;
import main.java.entity.User;
import main.java.entity.Vote;
import main.java.exceptions.NotFoundException;
import main.java.logger.LoggerFacade;
import main.java.repository.CommentRepository;
import main.java.repository.PostRepository;
import main.java.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class VotingService {

    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final UserManagementService userManagementService;
    private final CommentLookUpService commentLookUpService;

    @Autowired
    public VotingService(VoteRepository voteRepository, PostRepository postRepository, UserManagementService userManagementService, CommentLookUpService commentLookUpService) {
        this.voteRepository = voteRepository;
        this.postRepository = postRepository;
        this.userManagementService = userManagementService;
        this.commentLookUpService = commentLookUpService;
    }

    public void createVote(UUID userId, UUID postId, UUID commentId, boolean isUpvote) {
        User user = userManagementService.findById(userId);

        if (postId != null && commentId != null) {
            throw new IllegalArgumentException("Un vot nu poate fi aplicat simultan la o postare si un comentariu.");
        }

        if (postId != null) {
            Optional<Vote> existingVote = voteRepository.findByUserIdAndPostId(userId, postId);
            if (existingVote.isPresent()) {
                Vote vote = existingVote.get();
                vote.setUpvote(isUpvote);
                voteRepository.save(vote);
            } else {
                Post post = postRepository.getPostById(postId);
                Vote vote = new Vote(post, null, user, isUpvote);
                voteRepository.save(vote);
            }
        } else if (commentId != null) {
            Optional<Vote> existingVote = voteRepository.findByUserIdAndCommentId(userId, commentId);
            if (existingVote.isPresent()) {
                Vote vote = existingVote.get();
                vote.setUpvote(isUpvote);
                voteRepository.save(vote);
            } else {
                Comment comment = commentLookUpService.findById(commentId);
                Vote vote = new Vote(null, comment, user, isUpvote);
                voteRepository.save(vote);
            }
        } else {
            throw new IllegalArgumentException("Trebuie sa specifici fie postId, fie commentId.");
        }
    }


    public void deleteVoteForComment(Comment comment, User user) {
        voteRepository.deleteByUserIdAndCommentId(user.getId(), comment.getId());
    }

    public int countUpvotesForComment(UUID commentId) {
        return voteRepository.countByCommentIdAndUpvoteTrue(commentId);
    }

    public int countDownvotesForComment(UUID commentId) {
        return voteRepository.countByCommentIdAndUpvoteFalse(commentId);
    }

    public String getVoteTypeForUser(UUID userId, UUID postId, UUID commentId) {
        if (postId != null) {
            return voteRepository.findByUserIdAndPostId(userId, postId)
                    .map(v -> v.isUpvote() ? "up" : "down")
                    .orElse("none");
        } else if (commentId != null) {
            return voteRepository.findByUserIdAndCommentId(userId, commentId)
                    .map(v -> v.isUpvote() ? "up" : "down")
                    .orElse("none");
        }
        throw new IllegalArgumentException("Trebuie să specifici postId sau commentId");
    }

    public int countUpvotesForPost(UUID postId) {
        return voteRepository.countByPostIdAndUpvoteTrue(postId);
    }

    public int countDownvotesForPost(UUID postId) {
        return voteRepository.countByPostIdAndUpvoteFalse(postId);
    }

    public void deleteVoteForPost(Post post, User user) {
        voteRepository.deleteByUserIdAndPostId(user.getId(), post.getId());
    }

    public UUID getUserIdByUsername(String username) {
        return userManagementService.findByUsername(username).getId();
    }
}
