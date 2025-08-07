package main.java.service;

import jakarta.transaction.Transactional;
import main.java.dto.vote.VoteResponseDto;
import main.java.entity.Comment;
import main.java.entity.Post;
import main.java.entity.User;
import main.java.entity.Vote;
import main.java.exceptions.InvalidVoteTypeException;
import main.java.exceptions.NotFoundException;
import main.java.repository.CommentRepository;
import main.java.repository.PostRepository;
import main.java.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class VotingService {

    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final UserManagementService userManagementService;
    private final CommentRepository commentRepository;

    @Autowired
    public VotingService(VoteRepository voteRepository, PostRepository postRepository, UserManagementService userManagementService, CommentRepository commentRepository) {
        this.voteRepository = voteRepository;
        this.postRepository = postRepository;
        this.userManagementService = userManagementService;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public VoteResponseDto voteComment(UUID commentId, String voteType, String username) {
        User user = userManagementService.findByUsername(username);

        switch (voteType.toLowerCase()) {
            case "up" -> createVote(user.getId(), null, commentId, true);
            case "down" -> createVote(user.getId(), null, commentId, false);
            case "none" -> deleteVoteForComment(commentId, user);
            default -> throw new InvalidVoteTypeException("Tipul de vot este invalid: " + voteType);
        }

        int upvotes = countUpvotesForComment(commentId);
        int downvotes = countDownvotesForComment(commentId);
        int score = upvotes - downvotes;
        String userVote = getVoteTypeForUser(user.getId(), null, commentId);

        return new VoteResponseDto(upvotes, downvotes, score, userVote);
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
                Comment comment = commentRepository.findById(commentId)
                        .orElseThrow(() -> new NotFoundException("Comentariul nu a fost gasit"));

                Vote vote = new Vote(null, comment, user, isUpvote);
                voteRepository.save(vote);
            }
        } else {
            throw new IllegalArgumentException("Trebuie sa specifici fie postId, fie commentId.");
        }
    }


    public void deleteVoteForComment(UUID commentId, User user) {
        voteRepository.deleteByUserIdAndCommentId(user.getId(), commentId);
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
}
