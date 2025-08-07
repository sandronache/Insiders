package main.java.service;

import jakarta.transaction.Transactional;
import main.java.dto.comment.CommentCreateRequestDto;
import main.java.dto.comment.CommentResponseDto;
import main.java.dto.comment.CommentUpdateRequestDto;
import main.java.entity.Comment;
import main.java.entity.Post;
import main.java.entity.User;
import main.java.exceptions.NotFoundException;
import main.java.mapper.CommentMapper;
import main.java.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserManagementService userManagementService;

    public CommentService(CommentRepository commentRepository, CommentMapper commentMapper, UserManagementService userManagementService) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.userManagementService = userManagementService;
    }

    public Comment getCommentById(UUID id){
        return commentRepository.findById(id).orElseThrow(() -> new NotFoundException("Comentariul nu a fost gasit"));
    }

    @Transactional
    public List<CommentResponseDto> getCommentsForPost(UUID postId, String currentUsername) {
        List<Comment> allComments = commentRepository.findByPostId(postId);

        List<Comment> rootComments = allComments.stream()
                .filter(c -> c.getParentComment() == null)
                .toList();

        return rootComments.stream()
                .map(c -> commentMapper.toDto(c, allComments, currentUsername))
                .toList();
    }


    public CommentResponseDto createComment(Post post, CommentCreateRequestDto request) {
        User user = userManagementService.findByUsername(request.author());

        Comment parent = null;
        if (request.parentId() != null) {
            parent = getCommentById(request.parentId());
        }

        Comment comment = new Comment(post, parent, request.content(), user);
        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toDto(savedComment, List.of(), request.author());
    }


    @Transactional
    public CommentResponseDto getCommentWithReplies(UUID commentId, String currentUsername) {
        Comment mainComment = getCommentById(commentId);
        List<Comment> allComments = commentRepository.findByPostId(mainComment.getPost().getId());

        return commentMapper.toDto(mainComment, allComments, currentUsername);
    }

    @Transactional
    public CommentResponseDto updateComment(UUID commentId, CommentUpdateRequestDto request, String currentUsername) {
        Comment comment = getCommentById(commentId);

        comment.setContent(request.content());
        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toDto(updatedComment, List.of(), currentUsername);
    }

    @Transactional
    public void deleteComment(UUID commentId) {
        Comment comment = getCommentById(commentId);
        comment.setDeleted(true);
        comment.setContent("[comentariu sters]");
        commentRepository.save(comment);
    }


    public int countCommentsByPostId(UUID postId) {
        return commentRepository.countByPostId(postId);
    }
}
