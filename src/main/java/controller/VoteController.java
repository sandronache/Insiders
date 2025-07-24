package main.java.controller;

import main.java.model.Vote;
import main.java.repository.VoteRepository;
import main.java.service.VotingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votes")
public class VoteController {
    private final VotingService votingService = VotingService.getInstance();
    private final VoteRepository voteRepository = new VoteRepository();

    // Upvote a post
    @PostMapping("/post/{postId}/upvote")
    public void upvotePost(@PathVariable Integer postId, @RequestParam String username) {
        voteRepository.addPostUpvote(postId, username);
        Vote vote = new Vote();
        vote.getUpvote().addAll(voteRepository.getPostUpvotes(postId));
        vote.getDownvote().addAll(voteRepository.getPostDownvotes(postId));
        votingService.checkEmojiForPost(vote, postId);
    }

    // Downvote a post
    @PostMapping("/post/{postId}/downvote")
    public void downvotePost(@PathVariable Integer postId, @RequestParam String username) {
        voteRepository.addPostDownvote(postId, username);
        Vote vote = new Vote();
        vote.getUpvote().addAll(voteRepository.getPostUpvotes(postId));
        vote.getDownvote().addAll(voteRepository.getPostDownvotes(postId));
        votingService.checkEmojiForPost(vote, postId);
    }

    // Upvote a comment
    @PostMapping("/comment/{commentId}/upvote")
    public void upvoteComment(@PathVariable Integer commentId, @RequestParam String username) {
        voteRepository.addCommentUpvote(commentId, username);
        Vote vote = new Vote();
        vote.getUpvote().addAll(voteRepository.getCommentUpvotes(commentId));
        vote.getDownvote().addAll(voteRepository.getCommentDownvotes(commentId));
        votingService.checkEmojiForComment(vote, commentId);
    }

    // Downvote a comment
    @PostMapping("/comment/{commentId}/downvote")
    public void downvoteComment(@PathVariable Integer commentId, @RequestParam String username) {
        voteRepository.addCommentDownvote(commentId, username);
        Vote vote = new Vote();
        vote.getUpvote().addAll(voteRepository.getCommentUpvotes(commentId));
        vote.getDownvote().addAll(voteRepository.getCommentDownvotes(commentId));
        votingService.checkEmojiForComment(vote, commentId);
    }
}