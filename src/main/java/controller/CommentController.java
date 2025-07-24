package main.java.controller;

import main.java.model.Comment;
import main.java.service.CommentService;
import main.java.repository.CommentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService = CommentService.getInstance();
    private final CommentRepository commentRepository = new CommentRepository();

    // Get all top-level comments for a post
    @GetMapping("/post/{postId}")
    public List<Comment> getCommentsByPost(@PathVariable Integer postId) {
        return commentRepository.findByPostId(postId);
    }

    // Add a new top-level comment to a post
    @PostMapping("/post/{postId}")
    public Comment addComment(@PathVariable Integer postId, @RequestBody Comment comment) {
        // Save comment in DB
        Integer commentId = commentRepository.save(comment, postId, null);
        comment.setDatabaseId(commentId);
        return comment;
    }

    // Add a reply to a comment
    @PostMapping("/{parentCommentId}/reply")
    public Comment addReply(@PathVariable Integer parentCommentId, @RequestBody Comment reply) {
        // Save reply in DB
        Integer replyId = commentRepository.save(reply, null, parentCommentId);
        reply.setDatabaseId(replyId);
        return reply;
    }

    // Mark a comment as deleted
    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Integer commentId) {
        commentRepository.markAsDeleted(commentId);
    }
}