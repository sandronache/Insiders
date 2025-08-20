package org.insiders.backend.controller;

import org.insiders.backend.dto.comment.CommentResponseDto;
import org.insiders.backend.dto.comment.CommentUpdateRequestDto;
import org.insiders.backend.dto.vote.VoteRequestDto;
import org.insiders.backend.dto.vote.VoteResponseDto;
import org.insiders.backend.service.CommentService;
import org.insiders.backend.service.VotingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.insiders.backend.logger.AsyncLogManager;

import java.util.UUID;

@RestController
@RequestMapping("/comments")
public class CommentController {
    public final CommentService commentService;
    private final VotingService votingService;
    private final AsyncLogManager logger = AsyncLogManager.getInstance();

    public CommentController(CommentService commentService, VotingService votingService) {
        this.commentService = commentService;
        this.votingService = votingService;
        logger.log("INFO", "CommentController initialized with endpoints:");
        logger.log("INFO", "- GET /comments/{commentId}");
        logger.log("INFO", "- PUT /comments/{commentId}");
        logger.log("INFO", "- DELETE /comments/{commentId}");
        logger.log("INFO", "- PUT /comments/{commentId}/vote");
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<ResponseApi<CommentResponseDto>> getCommentWithReplies(@PathVariable UUID commentId,
                                                                                 @RequestParam(defaultValue = "current_user") String username) {
        logger.log("INFO", "GET request received for comment ID: " + commentId + " by user: " + username);
        try {
            CommentResponseDto response = commentService.getCommentWithReplies(commentId, username);
            logger.log("INFO", "Successfully retrieved comment with ID: " + commentId);
            return ResponseEntity.ok(new ResponseApi<>(true, response));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to retrieve comment: " + e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ResponseApi<CommentResponseDto>> updateComment(@PathVariable UUID commentId,
                                                                         @RequestBody CommentUpdateRequestDto request,
                                                                         @RequestParam(defaultValue = "current_user") String username) {
        logger.log("INFO", "PUT request received to update comment ID: " + commentId + " by user: " + username);
        try {
            CommentResponseDto response = commentService.updateComment(commentId, request, username);
            logger.log("INFO", "Successfully updated comment with ID: " + commentId);
            return ResponseEntity.ok(new ResponseApi<>(true, response));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to update comment: " + e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseApi<String>> deleteComment(@PathVariable UUID commentId) {
        logger.log("INFO", "DELETE request received for comment ID: " + commentId);
        try {
            commentService.deleteComment(commentId);
            logger.log("INFO", "Successfully deleted comment with ID: " + commentId);
            return ResponseEntity.ok(new ResponseApi<>(true, "Comentariul a fost sters cu succes"));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to delete comment: " + e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{commentId}/vote")
    public ResponseEntity<ResponseApi<VoteResponseDto>> voteComment(@PathVariable UUID commentId,
                                                                    @RequestBody VoteRequestDto request,
                                                                    @RequestParam(defaultValue = "current_user") String username) {
        // Remove the reference to vote type
        logger.log("INFO", "Vote request received for comment ID: " + commentId +
                " by user: " + username);
        try {
            VoteResponseDto response = votingService.voteComment(commentId, request, username);
            logger.log("INFO", "Successfully processed vote for comment ID: " + commentId);
            return ResponseEntity.ok(new ResponseApi<>(true, response));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to process vote: " + e.getMessage());
            throw e;
        }
    }
}