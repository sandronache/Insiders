package main.java.controller;

import main.java.dto.comment.CommentResponseDto;
import main.java.dto.comment.CommentUpdateRequestDto;
import main.java.dto.vote.VoteRequestDto;
import main.java.dto.vote.VoteResponseDto;
import main.java.service.CommentService;
import main.java.service.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/comments")
public class CommentController {
    public final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<ResponseApi<CommentResponseDto>> getCommentWithReplies(@PathVariable UUID commentId, @RequestParam(defaultValue = "andrei") String username) {
        CommentResponseDto response = commentService.getCommentWithReplies(commentId, username);
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ResponseApi<CommentResponseDto>> updateComment(@PathVariable UUID commentId, @RequestBody CommentUpdateRequestDto request, @RequestParam(defaultValue = "andrei") String username) {
        CommentResponseDto response = commentService.updateComment(commentId, request, username);
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseApi<String>> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(new ResponseApi<>(true, "Comentariul a fost sters cu succes"));
    }

    @PutMapping("/{commentId}/vote")
    public ResponseEntity<ResponseApi<VoteResponseDto>> voteComment(@PathVariable UUID commentId, @RequestBody VoteRequestDto request, @RequestParam(defaultValue = "andrei") String username) {
        VoteResponseDto response = commentService.voteComment(commentId, request.voteType(), username);
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }

}