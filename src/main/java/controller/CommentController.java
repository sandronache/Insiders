package main.java.controller;

import main.java.dto.comment.CommentResponseDto;
import main.java.dto.comment.CommentUpdateRequestDto;
import main.java.dto.comment.VoteRequestDto;
import main.java.dto.comment.VoteResponseDto;
import main.java.entity.User;
import main.java.service.CommentService;
import main.java.service.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/comments")
public class CommentController {
    public final CommentService commentService;
    private final UserManagementService userService;

    public CommentController(CommentService commentService, UserManagementService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<ResponseApi<CommentResponseDto>> getCommentWithReplies(@PathVariable UUID commentId){
        CommentResponseDto response = commentService.getCommentWithReplies(commentId);
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ResponseApi<CommentResponseDto>> updateComment(@PathVariable UUID commentId, @RequestBody CommentUpdateRequestDto request){
        CommentResponseDto response = commentService.updateComment(commentId,request);
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseApi<String>> deleteComment(@PathVariable UUID commentId){
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(new ResponseApi<>(true,"Comentariul a fost sters cu succes"));
    }

    @PutMapping("/{commentId}/vote")
    public ResponseEntity<ResponseApi<VoteResponseDto>> voteComment(@PathVariable UUID commentId, @RequestBody VoteRequestDto request){
        User user = null; //!!! aici trebuie modificat dupa ce reorganizam UserService
        VoteResponseDto response = commentService.voteComment(commentId,request.voteType(),user.getUsername());
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }

}