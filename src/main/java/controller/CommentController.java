package main.java.controller;

import main.java.model.Comment;
import main.java.service.CommentService;
import main.java.repository.CommentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService = CommentService.getInstance();
    private final CommentRepository commentRepository = new CommentRepository();

    // Get all top-level comments for a post
    @GetMapping("/post/{postId}")
    public List<Comment> getCommentsByPost(@PathVariable UUID postId) {
        return commentRepository.findByPostId(postId);
    }

    // Add a new top-level comment to a post
    @PostMapping("/post/{postId}")
    public Comment addComment(@PathVariable UUID postId, @RequestBody Comment comment) {
        // Save comment in DB
        Integer commentId = commentRepository.save(comment, postId, null);
        comment.setDatabaseId(commentId);
        return comment;
    }

    // Add a reply to a comment
    @PostMapping("/{parentCommentId}/reply")
    public Comment addReply(@PathVariable Integer parentCommentId, @RequestBody Comment reply) {
        // Get the post ID from the parent comment
        UUID postId = commentRepository.getPostIdByCommentId(parentCommentId);

        // Save reply in DB
        Integer replyId = commentRepository.save(reply, postId, parentCommentId);
        reply.setDatabaseId(replyId);
        return reply;
    }

    // Mark a comment as deleted
    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Integer commentId) {
        commentRepository.markAsDeleted(commentId);
    }
}