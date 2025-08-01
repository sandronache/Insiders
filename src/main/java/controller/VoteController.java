package main.java.controller;

import main.java.entity.Vote;
import main.java.logger.LoggerFacade;
import main.java.repository.VoteRepository;
import main.java.service.VotingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class VoteController {

    private final VoteRepository voteRepository;
    private final VotingService votingService;

    @Autowired
    public VoteController(VoteRepository voteRepository, VotingService votingService) {
        this.voteRepository = voteRepository;
        this.votingService = votingService;
        LoggerFacade.info("VoteController initialized with endpoints:");
        LoggerFacade.info("- POST /api/post/{postId}/upvote");
        LoggerFacade.info("- POST /api/post/{postId}/downvote");
        LoggerFacade.info("- POST /api/comment/{commentId}/upvote");
        LoggerFacade.info("- POST /api/comment/{commentId}/downvote");
    }

    // Upvote a post
    @PostMapping("/post/{postId}/upvote")
    public void upvotePost(@PathVariable UUID postId, @RequestParam String username) {
        LoggerFacade.info("POST /api/post/" + postId + "/upvote called for user: " + username);
        voteRepository.addPostUpvote(postId, username);
        Vote vote = new Vote();
        vote.getUpvote().addAll(voteRepository.getPostUpvotes(postId));
        vote.getDownvote().addAll(voteRepository.getPostDownvotes(postId));
        votingService.checkEmojiForPost(vote, postId);
        LoggerFacade.info("Post " + postId + " upvoted successfully by " + username);
    }

    // Downvote a post
    @PostMapping("/post/{postId}/downvote")
    public void downvotePost(@PathVariable UUID postId, @RequestParam String username) {
        LoggerFacade.info("POST /api/post/" + postId + "/downvote called for user: " + username);
        voteRepository.addPostDownvote(postId, username);
        Vote vote = new Vote();
        vote.getUpvote().addAll(voteRepository.getPostUpvotes(postId));
        vote.getDownvote().addAll(voteRepository.getPostDownvotes(postId));
        votingService.checkEmojiForPost(vote, postId);
        LoggerFacade.info("Post " + postId + " downvoted successfully by " + username);
    }

    // Upvote a comment
    @PostMapping("/comment/{commentId}/upvote")
    public void upvoteComment(@PathVariable Integer commentId, @RequestParam String username) {
        LoggerFacade.info("POST /api/comment/" + commentId + "/upvote called for user: " + username);
        voteRepository.addCommentUpvote(commentId, username);
        Vote vote = new Vote();
        vote.getUpvote().addAll(voteRepository.getCommentUpvotes(commentId));
        vote.getDownvote().addAll(voteRepository.getCommentDownvotes(commentId));
        votingService.checkEmojiForComment(vote, commentId);
        LoggerFacade.info("Comment " + commentId + " upvoted successfully by " + username);
    }

    // Downvote a comment
    @PostMapping("/comment/{commentId}/downvote")
    public void downvoteComment(@PathVariable Integer commentId, @RequestParam String username) {
        LoggerFacade.info("POST /api/comment/" + commentId + "/downvote called for user: " + username);
        voteRepository.addCommentDownvote(commentId, username);
        Vote vote = new Vote();
        vote.getUpvote().addAll(voteRepository.getCommentUpvotes(commentId));
        vote.getDownvote().addAll(voteRepository.getCommentDownvotes(commentId));
        votingService.checkEmojiForComment(vote, commentId);
        LoggerFacade.info("Comment " + commentId + " downvoted successfully by " + username);
    }
}