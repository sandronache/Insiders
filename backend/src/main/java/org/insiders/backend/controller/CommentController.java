package org.insiders.backend.controller;

import org.insiders.backend.dto.comment.CommentResponseDto;
import org.insiders.backend.dto.comment.CommentUpdateRequestDto;
import org.insiders.backend.dto.vote.VoteRequestDto;
import org.insiders.backend.dto.vote.VoteResponseDto;
import org.insiders.backend.logger.LoggerFacade;
import org.insiders.backend.service.CommentService;
import org.insiders.backend.service.VotingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/comments")
public class CommentController {
    public final CommentService commentService;
    private final VotingService votingService;

    public CommentController(CommentService commentService, VotingService votingService) {
        this.commentService = commentService;
        this.votingService = votingService;
        LoggerFacade.info("CommentController initialized with endpoints:");
        LoggerFacade.info("- GET /comments/{commentId}");
        LoggerFacade.info("- PUT /comments/{commentId}");
        LoggerFacade.info("- DELETE /comments/{commentId}");
        LoggerFacade.info("- PUT /comments/{commentId}/vote");
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<ResponseApi<CommentResponseDto>> getCommentWithReplies(@PathVariable UUID commentId,
                                                                                 @RequestParam(defaultValue = "current_user") String username) {
        CommentResponseDto response = commentService.getCommentWithReplies(commentId, username);
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ResponseApi<CommentResponseDto>> updateComment(@PathVariable UUID commentId, @RequestBody CommentUpdateRequestDto request, @RequestParam(defaultValue = "current_user") String username) {
        CommentResponseDto response = commentService.updateComment(commentId, request, username);
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseApi<String>> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(new ResponseApi<>(true, "Comentariul a fost sters cu succes"));
    }

    @PutMapping("/{commentId}/vote")
    public ResponseEntity<ResponseApi<VoteResponseDto>> voteComment(@PathVariable UUID commentId, @RequestBody VoteRequestDto request, @RequestParam(defaultValue = "current_user") String username) {
        VoteResponseDto response = votingService.voteComment(commentId, request, username);
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }

}